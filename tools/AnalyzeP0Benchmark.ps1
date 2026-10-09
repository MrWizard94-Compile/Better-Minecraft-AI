[CmdletBinding()]
param(
    [Parameter()]
    [string] $ControlCsv = (Join-Path $PSScriptRoot '..\docs\release-evidence\p0\benchmark-data\p0-vanilla_control.csv'),

    [Parameter()]
    [string] $ObserveCsv = (Join-Path $PSScriptRoot '..\docs\release-evidence\p0\benchmark-data\p0-observe.csv'),

    [Parameter()]
    [string] $JsonOutput = (Join-Path $PSScriptRoot '..\docs\release-evidence\p0\benchmark-summary.json'),

    [Parameter()]
    [string] $MarkdownOutput = (Join-Path $PSScriptRoot '..\docs\release-evidence\p0\benchmark-summary.md')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$requiredColumns = @(
    'arm',
    'scenario',
    'replicate',
    'world_seed',
    'entity_count',
    'warmup_ticks',
    'duration_ticks',
    'success',
    'final_distance',
    'path_nodes',
    'stall_ticks',
    'observer_handler_nanos',
    'tick_median_ms',
    'tick_p95_ms',
    'tick_p99_ms',
    'exceptions'
)

$integerColumns = @(
    'replicate',
    'world_seed',
    'entity_count',
    'warmup_ticks',
    'duration_ticks',
    'path_nodes',
    'stall_ticks',
    'observer_handler_nanos',
    'exceptions'
)

$floatingColumns = @('final_distance', 'tick_median_ms', 'tick_p95_ms', 'tick_p99_ms')
$metricColumns = @(
    'duration_ticks',
    'final_distance',
    'path_nodes',
    'stall_ticks',
    'observer_handler_nanos',
    'tick_median_ms',
    'tick_p95_ms',
    'tick_p99_ms'
)

function Convert-ToInvariantDouble {
    param(
        [Parameter(Mandatory)]
        [string] $Value,

        [Parameter(Mandatory)]
        [string] $Context
    )

    $parsed = 0.0
    $valid = [double]::TryParse(
        $Value,
        [Globalization.NumberStyles]::Float,
        [Globalization.CultureInfo]::InvariantCulture,
        [ref] $parsed)
    if (-not $valid -or -not [double]::IsFinite($parsed)) {
        throw "Invalid finite number for ${Context}: '$Value'"
    }
    return $parsed
}

function Convert-ToInvariantInt64 {
    param(
        [Parameter(Mandatory)]
        [string] $Value,

        [Parameter(Mandatory)]
        [string] $Context
    )

    $parsed = [long] 0
    $valid = [long]::TryParse(
        $Value,
        [Globalization.NumberStyles]::Integer,
        [Globalization.CultureInfo]::InvariantCulture,
        [ref] $parsed)
    if (-not $valid) {
        throw "Invalid integer for ${Context}: '$Value'"
    }
    return $parsed
}

function Get-NearestRankPercentile {
    param(
        [Parameter(Mandatory)]
        [double[]] $Values,

        [Parameter(Mandatory)]
        [ValidateRange(0.0, 1.0)]
        [double] $Percentile
    )

    if ($Values.Count -eq 0) {
        throw 'Cannot calculate a percentile for an empty sample.'
    }
    $sorted = @($Values | Sort-Object)
    $index = [Math]::Max(0, [Math]::Ceiling($Percentile * $sorted.Count) - 1)
    return [double] $sorted[$index]
}

function Get-Median {
    param(
        [Parameter(Mandatory)]
        [double[]] $Values
    )

    if ($Values.Count -eq 0) {
        throw 'Cannot calculate a median for an empty sample.'
    }
    $sorted = @($Values | Sort-Object)
    $middle = [int] [Math]::Floor($sorted.Count / 2)
    if ($sorted.Count % 2 -eq 1) {
        return [double] $sorted[$middle]
    }
    return ([double] $sorted[$middle - 1] + [double] $sorted[$middle]) / 2.0
}

function Import-ValidatedArm {
    param(
        [Parameter(Mandatory)]
        [string] $Path,

        [Parameter(Mandatory)]
        [string] $ExpectedArm
    )

    $resolvedPath = (Resolve-Path -LiteralPath $Path).Path
    $rows = @(Import-Csv -LiteralPath $resolvedPath)
    if ($rows.Count -ne 5) {
        throw "Expected exactly 5 samples in '$resolvedPath'; found $($rows.Count)."
    }

    $actualColumns = @($rows[0].PSObject.Properties.Name)
    $missingColumns = @($requiredColumns | Where-Object { $_ -notin $actualColumns })
    $unexpectedColumns = @($actualColumns | Where-Object { $_ -notin $requiredColumns })
    if ($missingColumns.Count -gt 0 -or $unexpectedColumns.Count -gt 0) {
        throw "CSV schema mismatch in '$resolvedPath'. Missing: [$($missingColumns -join ', ')]; unexpected: [$($unexpectedColumns -join ', ')]."
    }

    $replicates = [Collections.Generic.HashSet[long]]::new()
    foreach ($row in $rows) {
        if ($row.arm -cne $ExpectedArm) {
            throw "Expected arm '$ExpectedArm' in '$resolvedPath'; found '$($row.arm)'."
        }
        if ($row.success -cnotin @('true', 'false')) {
            throw "Invalid success value in '$resolvedPath': '$($row.success)'."
        }
        foreach ($column in $integerColumns) {
            $value = Convert-ToInvariantInt64 -Value $row.$column -Context "$ExpectedArm.$column"
            if ($value -lt 0 -and $column -ne 'world_seed') {
                throw "Negative value for $ExpectedArm.${column}: $value"
            }
        }
        foreach ($column in $floatingColumns) {
            $value = Convert-ToInvariantDouble -Value $row.$column -Context "$ExpectedArm.$column"
            if ($value -lt 0.0) {
                throw "Negative value for $ExpectedArm.${column}: $value"
            }
        }
        $replicate = Convert-ToInvariantInt64 -Value $row.replicate -Context "$ExpectedArm.replicate"
        if (-not $replicates.Add($replicate)) {
            throw "Duplicate replicate $replicate in '$resolvedPath'."
        }
    }

    $expectedReplicates = @(1L, 2L, 3L, 4L, 5L)
    $actualReplicates = @($replicates | Sort-Object)
    if (($actualReplicates -join ',') -cne ($expectedReplicates -join ',')) {
        throw "Replicates in '$resolvedPath' must be exactly 1 through 5."
    }
    return @($rows)
}

function Get-ArmSummary {
    param(
        [Parameter(Mandatory)]
        [object[]] $Rows
    )

    $metrics = [ordered] @{}
    foreach ($column in $metricColumns) {
        $values = [double[]] @($Rows | ForEach-Object {
            Convert-ToInvariantDouble -Value $_.$column -Context "$($_.arm).$column"
        })
        $metrics[$column] = [ordered] @{
            mean = [Math]::Round(($values | Measure-Object -Average).Average, 6)
            median = [Math]::Round((Get-Median -Values $values), 6)
            p95_nearest_rank = [Math]::Round((Get-NearestRankPercentile -Values $values -Percentile 0.95), 6)
            minimum = [Math]::Round(($values | Measure-Object -Minimum).Minimum, 6)
            maximum = [Math]::Round(($values | Measure-Object -Maximum).Maximum, 6)
        }
    }

    $successes = @($Rows | Where-Object { $_.success -ceq 'true' }).Count
    return [ordered] @{
        arm = $Rows[0].arm
        scenario = $Rows[0].scenario
        samples = $Rows.Count
        successes = $successes
        success_rate = [Math]::Round($successes / $Rows.Count, 6)
        exceptions = [long] (($Rows | Measure-Object -Property exceptions -Sum).Sum)
        metrics = $metrics
    }
}

function Get-MatchKey {
    param(
        [Parameter(Mandatory)]
        [object] $Row
    )

    return @($Row.scenario, $Row.replicate, $Row.world_seed, $Row.entity_count, $Row.warmup_ticks) -join '|'
}

$controlRows = Import-ValidatedArm -Path $ControlCsv -ExpectedArm 'vanilla_control'
$observeRows = Import-ValidatedArm -Path $ObserveCsv -ExpectedArm 'observe'

$controlByKey = @{}
foreach ($row in $controlRows) {
    $controlByKey[(Get-MatchKey -Row $row)] = $row
}

$paired = @()
foreach ($observeRow in $observeRows) {
    $key = Get-MatchKey -Row $observeRow
    if (-not $controlByKey.ContainsKey($key)) {
        throw "Observe sample has no matched vanilla-control sample: $key"
    }
    $controlRow = $controlByKey[$key]
    $paired += [pscustomobject] [ordered] @{
        key = $key
        replicate = [long] $observeRow.replicate
        success_matches = $observeRow.success -ceq $controlRow.success
        duration_delta_ticks = [long] $observeRow.duration_ticks - [long] $controlRow.duration_ticks
        path_nodes_delta = [long] $observeRow.path_nodes - [long] $controlRow.path_nodes
        stall_ticks_delta = [long] $observeRow.stall_ticks - [long] $controlRow.stall_ticks
        final_distance_delta = [Math]::Round(
            (Convert-ToInvariantDouble $observeRow.final_distance 'observe.final_distance') -
            (Convert-ToInvariantDouble $controlRow.final_distance 'vanilla_control.final_distance'),
            6)
    }
}

if ($paired.Count -ne $controlRows.Count) {
    throw 'The two arms do not contain the same matched sample keys.'
}
if (@($controlRows | Where-Object { $_.success -cne 'true' }).Count -ne 0 -or
    @($observeRows | Where-Object { $_.success -cne 'true' }).Count -ne 0) {
    throw 'At least one P0 navigation sample failed.'
}
if (@($controlRows | Where-Object { [long] $_.exceptions -ne 0 }).Count -ne 0 -or
    @($observeRows | Where-Object { [long] $_.exceptions -ne 0 }).Count -ne 0) {
    throw 'At least one P0 navigation sample recorded an exception.'
}
if (@($controlRows | Where-Object { [long] $_.observer_handler_nanos -ne 0 }).Count -ne 0) {
    throw 'The vanilla-control arm unexpectedly recorded observer handler work.'
}

$maxFinalDistanceDelta = [double] (($paired | ForEach-Object {
    [Math]::Abs($_.final_distance_delta)
} | Measure-Object -Maximum).Maximum)
$behaviorallyMatched = @($paired | Where-Object {
    -not $_.success_matches -or
    $_.duration_delta_ticks -ne 0 -or
    $_.path_nodes_delta -ne 0 -or
    $_.stall_ticks_delta -ne 0
}).Count -eq 0 -and $maxFinalDistanceDelta -le 0.001

$result = [ordered] @{
    schema_version = 1
    phase = 'P0'
    interpretation = 'Descriptive baseline only; five replicates are insufficient for inferential or release-performance claims.'
    protocol = [ordered] @{
        matched_fields = @('scenario', 'replicate', 'world_seed', 'entity_count', 'warmup_ticks')
        expected_replicates_per_arm = 5
        final_distance_equivalence_tolerance_blocks = 0.001
        percentile_method = 'nearest-rank'
    }
    vanilla_control = Get-ArmSummary -Rows $controlRows
    observe = Get-ArmSummary -Rows $observeRows
    paired_behavior = [ordered] @{
        behaviorally_matched = $behaviorallyMatched
        maximum_absolute_final_distance_delta_blocks = [Math]::Round($maxFinalDistanceDelta, 6)
        samples = @($paired | Sort-Object replicate)
    }
}

$jsonDirectory = Split-Path -Parent ([IO.Path]::GetFullPath($JsonOutput))
$markdownDirectory = Split-Path -Parent ([IO.Path]::GetFullPath($MarkdownOutput))
[IO.Directory]::CreateDirectory($jsonDirectory) | Out-Null
[IO.Directory]::CreateDirectory($markdownDirectory) | Out-Null
$utf8NoBom = [Text.UTF8Encoding]::new($false)
$json = (($result | ConvertTo-Json -Depth 10) -replace "`r`n", "`n" -replace "`r", "`n")
[IO.File]::WriteAllText([IO.Path]::GetFullPath($JsonOutput), $json + "`n", $utf8NoBom)

$controlSummary = $result.vanilla_control
$observeSummary = $result.observe
$markdown = @"
# P0 benchmark summary

This is a descriptive Phase P0 baseline, not a claim that Better Minecraft AI improves navigation. Both arms use the same vanilla PathNavigation route. The vanilla-control arm forces the BMAI observer off; the OBSERVE arm enables read-only sampling.

| Result | Vanilla control | OBSERVE |
|---|---:|---:|
| Samples | $($controlSummary.samples) | $($observeSummary.samples) |
| Successful arrivals | $($controlSummary.successes) / $($controlSummary.samples) | $($observeSummary.successes) / $($observeSummary.samples) |
| Exceptions | $($controlSummary.exceptions) | $($observeSummary.exceptions) |
| Duration median (ticks) | $($controlSummary.metrics.duration_ticks.median) | $($observeSummary.metrics.duration_ticks.median) |
| Duration p95, nearest rank (ticks) | $($controlSummary.metrics.duration_ticks.p95_nearest_rank) | $($observeSummary.metrics.duration_ticks.p95_nearest_rank) |
| Path nodes median | $($controlSummary.metrics.path_nodes.median) | $($observeSummary.metrics.path_nodes.median) |
| Stall ticks median | $($controlSummary.metrics.stall_ticks.median) | $($observeSummary.metrics.stall_ticks.median) |
| Final distance median (blocks) | $($controlSummary.metrics.final_distance.median) | $($observeSummary.metrics.final_distance.median) |
| Tick p95 sample mean (ms) | $($controlSummary.metrics.tick_p95_ms.mean) | $($observeSummary.metrics.tick_p95_ms.mean) |
| Observer handler time mean (ns, overlapping window) | $($controlSummary.metrics.observer_handler_nanos.mean) | $($observeSummary.metrics.observer_handler_nanos.mean) |

## Paired interpretation

- Matched behavior: **$behaviorallyMatched**.
- Every replicate matched on scenario, seed, entity count, warmup and replicate ID.
- Success, duration, path-node count and stall count matched in every pair.
- Maximum absolute paired final-distance delta: $([Math]::Round($maxFinalDistanceDelta, 6)) blocks (P0 descriptive-analysis tolerance: 0.001 blocks; this was not preregistered as an A1 acceptance threshold).
- The control arm recorded zero observer handler time. The OBSERVE value is a process-global overlapping window and is retained as diagnostic instrumentation cost, not isolated per-entity CPU.
- Server tick percentiles are short-run rolling-server samples gathered while five isolated GameTests execute in one process. They are useful for detecting gross anomalies only; they are not a statistically powered performance comparison.

Raw data: [vanilla-control CSV](benchmark-data/p0-vanilla_control.csv) and [OBSERVE CSV](benchmark-data/p0-observe.csv). Machine-readable analysis: [benchmark-summary.json](benchmark-summary.json).
"@
[IO.File]::WriteAllText([IO.Path]::GetFullPath($MarkdownOutput), $markdown.TrimEnd() + "`n", $utf8NoBom)

Write-Output "Validated $($controlRows.Count) vanilla-control and $($observeRows.Count) OBSERVE samples."
Write-Output "Behaviorally matched: $behaviorallyMatched"
Write-Output "JSON: $([IO.Path]::GetFullPath($JsonOutput))"
Write-Output "Markdown: $([IO.Path]::GetFullPath($MarkdownOutput))"

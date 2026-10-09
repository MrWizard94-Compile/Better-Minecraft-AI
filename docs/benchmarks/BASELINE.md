# Phase P0 vanilla-control / OBSERVE baseline

## Purpose and interpretation boundary

This protocol establishes a reproducible in-game reference before Better Minecraft AI implements enhanced navigation. It measures a real vanilla 1.21.1 zombie using its existing `GroundPathNavigation` in two BMAI-hosted GameTest arms:

- `vanilla_control`: the BMAI artifact supplies the test harness, but observation is force-disabled before the mod entry point initializes; the summary must report zero joined, attached, sampled, and handler-time counts.
- `observe`: the same artifact, code, scene, zombie, goal stimulus, vanilla navigation, JVM, and host run with the read-only observer enabled.

This is not a mod-absent binary run, an enhanced arm, an algorithm comparison, or a statistically powered performance study. The harness's presence is an explicit P0 limitation; P1/A1 must use the frozen three-arm protocol required by the Definition of Done before claiming improvement.

## Frozen P0 scene: `open_plain_1`

| Parameter | Fixed value |
|---|---|
| GameTest template | `better_minecraft_ai:empty` |
| World seed reported by server | 0 |
| Time | Night, set by the test |
| Floor | 20 × 5 smooth-stone lane at relative Y=0 |
| Clearance | Air at relative Y=1 and Y=2 |
| Entity | One ordinary vanilla `EntityType.ZOMBIE` per isolated test scene |
| Start | Relative `(2, 1, 2)` |
| Target | Bottom center of relative `(16, 1, 2)` |
| Navigation | The zombie's existing vanilla `GroundPathNavigation` |
| Movement request | Existing `PathNavigation.moveTo`, speed modifier 1.0; base movement attribute is not changed |
| Goal arbitration | Test-only priority-0 goal declaring `Goal.Flag.MOVE` |
| Framework setup warmup | 20 ticks |
| Goal-install delay | 2 test ticks after entity spawn |
| Trial timeout | 140 measured ticks; the enclosing GameTest timeout is 160 ticks |
| Replicates | Five fixed GameTest methods per arm, IDs 1–5 |
| Cohort semantics | One measured zombie in each isolated scene; the five scenes may execute concurrently in one GameTest server process |

The scene is intentionally simple. Its P0 purpose is to detect instrumentation interference and retain a baseline plumbing/data contract, not to demonstrate difficult navigation.

## Outcomes and raw fields

An attempt succeeds only when all of these are true before timeout:

1. the test goal started;
2. vanilla `PathNavigation.moveTo` accepted the request;
3. the navigation reports done; and
4. the zombie finishes within 1.5 blocks of the target bottom-center.

A timeout is a failed GameTest and is never dropped. The analyzer also rejects a missing CSV row.

| CSV field | Definition |
|---|---|
| `arm` | `vanilla_control` or `observe` |
| `scenario` | Stable scene ID `open_plain_1` |
| `replicate` | Fixed ID 1–5 |
| `world_seed` | Server level seed reported at measurement time |
| `entity_count` | One measured entity per isolated scene |
| `warmup_ticks` | GameTest framework setup interval |
| `duration_ticks` | From scheduled goal-acquisition tick to accepted arrival |
| `success` | Arrival predicate result; false on timeout |
| `final_distance` | Euclidean blocks from zombie position to target bottom-center |
| `path_nodes` | Maximum node count observed on the active vanilla path |
| `stall_ticks` | Ticks where navigation is active and squared displacement is below `1.0E-6` |
| `observer_handler_nanos` | Delta of the process-global observer handler counter over the trial window |
| `tick_median_ms`, `tick_p95_ms`, `tick_p99_ms` | Nearest-rank percentiles over the server's non-zero rolling tick-time samples visible when the trial finishes |
| `exceptions` | Zero for a retained completed row; exceptions fail the GameTest and normally produce a missing/failed sample |

`observer_handler_nanos` windows overlap when tests run concurrently and therefore must not be summed or interpreted as isolated per-zombie CPU. Tick percentiles are also short, rolling, process-wide samples and can repeat across concurrent trials. Both are diagnostic P0 signals only. A later performance gate needs sequential controlled replicates and a profiler.

## Matched execution protocol

Both arms use the same:

- source tree and built classes;
- Java 21.0.10 runtime and 2 GiB Gradle JVM limit;
- Minecraft 1.21.1 / NeoForge 21.1.256 / Parchment 2024.11.17;
- scene geometry, seed, entity type/count, start/target, speed, warmup, timeout, and five replicate IDs;
- Windows host and no interactive players; and
- GameTest server task.

Only these properties differ:

| Property | Vanilla control | OBSERVE |
|---|---|---|
| `better_minecraft_ai.benchmark.arm` | `vanilla_control` | `observe` |
| `better_minecraft_ai.observation.enabled` | `false` | `true` |

Commands:

```powershell
.\gradlew.bat --no-daemon --warning-mode fail runGameTestServer `
  -PbmaiBenchmarkArm=vanilla_control `
  -PbmaiObservationEnabled=false `
  "-PbmaiBenchmarkOutputDir=$PWD\docs\release-evidence\p0\benchmark-data"

.\gradlew.bat --no-daemon --warning-mode fail runGameTestServer `
  -PbmaiBenchmarkArm=observe `
  -PbmaiObservationEnabled=true `
  "-PbmaiBenchmarkOutputDir=$PWD\docs\release-evidence\p0\benchmark-data"

.\tools\AnalyzeP0Benchmark.ps1
```

Every GameTest server invocation truncates its selected arm's CSV on the first row, so a run cannot accidentally append to stale evidence. The analyzer requires exactly five unique replicates numbered 1–5, exact schema, matched protocol keys, successful trials, zero exceptions, and zero observer work in the control arm.

## Retained P0 result

Both arms completed 5/5 arrivals with zero exceptions. Every matched pair used 113 or 114 ticks identically, reached a maximum path size of 14 nodes, and recorded zero stall ticks. The maximum paired final-distance difference was 0.000591 blocks in the retained run. The control summary logged all observation counters and handler time as zero; the OBSERVE run attached and sampled entities.

The generated [benchmark summary](../release-evidence/p0/benchmark-summary.md), [machine-readable analysis](../release-evidence/p0/benchmark-summary.json), [raw CSV](../release-evidence/p0/benchmark-data/), and [GameTest logs](../release-evidence/p0/runtime/) are authoritative for the exact retained values.

The behavior-match tolerance of 0.001 blocks in the analyzer is a descriptive P0 check written while packaging this baseline; it was not preregistered as an A1 threshold and must not be represented as one. More importantly, the categorical outcomes, duration, node count, and stalls matched exactly without a tolerance.

## Host and server settings

The run used the host and JVM recorded in [`docs/BUILD_ENVIRONMENT.md`](../BUILD_ENVIRONMENT.md): Intel Core i7-9700, approximately 48 GiB RAM, NVIDIA GTX 1660 Ti, Microsoft Windows build 26300.9457, and Temurin Java 21.0.10+7. GameTest uses ModDevGradle's generated server environment; the tracked dedicated smoke additionally fixes view/simulation distance to 4, but those dedicated settings are not silently attributed to the GameTest server.

No Spark/JFR profile or heap/allocation study was run for P0. That is not required to claim instrumentation non-interference in this simple baseline and is insufficient for an A1 performance claim. A1/P2 performance gates remain `NOT RUN`.

## Required next benchmark step

Before tuning a P1/P2 algorithm, freeze `docs/benchmarks/ACCEPTANCE_SCENES.md` with nontrivial reachable and unreachable scenes, at least 20 independent attempts per scene/mode over at least three predefined seeds, sequential performance replicates and load levels, invalidation/fallback metrics, and matched vanilla/OBSERVE/ENHANCED arms. Do not choose acceptance scenes after seeing enhanced results.

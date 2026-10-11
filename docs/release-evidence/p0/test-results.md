# Phase P0 test results

## Summary

| Layer | Executed | Passed | Failed | Errors | Skipped |
|---|---:|---:|---:|---:|---:|
| JUnit MC-free domain | 11 | 11 | 0 | 0 | 0 |
| GameTest vanilla-control | 7 | 7 | 0 | 0 | 0 |
| GameTest OBSERVE | 7 | 7 | 0 | 0 | 0 |
| Dedicated server smoke | 1 | 1 | 0 | 0 | 0 |
| Integrated client smoke | 1 | 1 | 0 | 0 | 0 |

No P0 test is skipped. A1 tests are outside this execution and remain `NOT RUN`, not skipped/passed.

## Unit behavior

`MotionStateTest` (7): idle without a vanilla path; stationary below epsilon; moving at epsilon; reject non-finite/negative distance; reject non-finite/non-positive threshold.

`ObservationOverrideTest` (4): absent override delegates to config; enabled parsing is case-insensitive; disabled parsing works; malformed values fail rather than silently changing behavior.

The explicit `--rerun-tasks` execution reports 11 tests, 0 failures/errors/skips. JaCoCo measures only `com.mrwizard94.betterminecraftai.domain`: 21 covered / 0 missed lines and 19 covered / 0 missed branches.

## GameTests

Both arms execute the same seven required tests with world seed 0:

1. `observeModeDoesNotMutateZombieAi` — defers attachment, checks server observation behavior based on the forced arm, and proves navigation/goal-selector/target-selector object identities do not change.
2. `zombieMoveOwnershipIsExclusiveAndReversible` — real vanilla zombie; exactly one running MOVE owner; existing navigation accepts route; exact-object removal; stop exactly once; route done; original goals restored; same navigation identity.
3. `p0VanillaNavigationBaseline1` — fixed replicate 1.
4. `p0VanillaNavigationBaseline2` — fixed replicate 2.
5. `p0VanillaNavigationBaseline3` — fixed replicate 3.
6. `p0VanillaNavigationBaseline4` — fixed replicate 4.
7. `p0VanillaNavigationBaseline5` — fixed replicate 5.

Accepted markers:

- vanilla-control log line 47: `All 7 required tests passed`; line 49: observer enabled false and every counter zero;
- OBSERVE log line 46: `All 7 required tests passed`; line 48: enabled true with 8 joined/attached mobs and 780 post-tick samples; and
- both logs end with all dimensions saved.

The enabled summary can show active/pending counts before cleanup because `onServerStopping` logs the pre-clear state and then immediately clears both collections. The shutdown event's cleanup behavior is code-audited; this marker is not a claim that entities survive shutdown.

## Baseline outcome

The analyzer accepted exactly 5 samples per arm and rejected no rows:

- 10/10 successful arrivals;
- zero exceptions;
- paired durations identical (113 or 114 ticks; median 113, p95 114);
- path nodes 14 in every sample;
- zero stall ticks in every sample;
- maximum paired final-distance delta 0.000591 blocks; and
- zero observer handler time in every vanilla-control sample.

Short-run rolling tick-time values differed and are deliberately not interpreted as a performance change. See [benchmark-summary.md](benchmark-summary.md) and [BASELINE.md](../../benchmarks/BASELINE.md).

## Remote clean-runner confirmation

GitHub Actions [run 38021120525](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38021120525) independently passed the Windows build, native Ubuntu build, paired P0 GameTests, and required-gates jobs for commit `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3`.

Its Linux artifact records 5/5 samples per arm, 10/10 successful arrivals, zero exceptions, median/p95 duration 113/114 ticks in both arms, 14 path nodes, zero stalls, a maximum paired final-distance delta of 0.000498 blocks, and zero observer handler time in the control arm. The small difference from the retained local maximum delta of 0.000591 blocks is within the frozen tolerance and is not treated as a performance result.

## Dedicated server

The retained run contains only Minecraft, NeoForge 21.1.256, and BMAI 0.1.0-p0. It binds `127.0.0.1:0`, reaches ready, receives the standard `stop` command, emits the OBSERVE summary, and saves all dimensions. Gradle exits 0.

## Integrated client

The retained quick-play run loads disposable local world `p0-smoke`, logs real local player `Dev` joining, waits for a ready integrated server for 100 active client ticks, emits `BMAI_P0_INTEGRATED_SMOKE_PASS`, calls normal `Minecraft.stop()`, emits the server summary, and saves all dimensions. Gradle exits 0.

This is unattended lifecycle evidence, not a human judgment of visuals, play feel, or accessibility. Native desktop automation was unavailable, so the development-only in-client hook supplied deterministic readiness/uptime evidence.

## Warning/error result

Accepted runtime logs contain no `ERROR` or `FATAL` entry and no BMAI warning. Inherited development-runtime warnings are retained and classified in [`docs/KNOWN_UPSTREAM_WARNINGS.md`](../../KNOWN_UPSTREAM_WARNINGS.md); none is suppressed.

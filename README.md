# Better Minecraft AI

Better Minecraft AI is an evidence-first NeoForge 1.21.1 research project for safer entity-navigation improvements. The current `0.1.0-p0` tree is **Phase P0 foundation work only**. It does not ship better pathfinding, replace a vanilla entity's AI, or claim an A1 release.

P0 provides:

- a pinned Java 21 / NeoForge / ModDevGradle build with dependency locking and checksum verification;
- an OBSERVE-only server-side service that records mob lifecycle and motion facts without writing goals, targets, navigation, attributes, spawn state, or world state;
- deterministic MC-free domain tests with a 100% line-and-branch coverage gate;
- seven in-game GameTests, including a test-only vanilla-zombie MOVE ownership and rollback proof;
- matched vanilla-control and OBSERVE baseline data;
- dedicated-server and integrated-client smoke evidence; and
- exact-source hook and external-framework decision records.

The committed and cross-platform-verified Phase P0 candidate is awaiting explicit director acceptance. Phase P1 and all major AI replacement work are deliberately out of scope until that approval is recorded.

## Supported environment

| Component | Pin |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.256 |
| Java | 21 |
| Gradle wrapper | 9.2.1 |
| ModDevGradle | 2.0.148 |
| Parchment | 1.21.1 / 2024.11.17 |

The complete pins, hashes, test tools, host facts, and provenance are in [BUILD_ENVIRONMENT.md](docs/BUILD_ENVIRONMENT.md). The project metadata temporarily declares `All Rights Reserved`; no public project license or release permission has been selected by the director.

## Build and test

Use the checked-in wrapper. Do not substitute a system Gradle installation.

Windows PowerShell:

```powershell
.\gradlew.bat --version
.\gradlew.bat --no-daemon --warning-mode fail clean build
```

Linux or macOS:

```bash
chmod +x ./gradlew
./gradlew --version
./gradlew --no-daemon --warning-mode fail clean build
```

`build` compiles with `-Xlint:all -Werror`, runs JUnit, Checkstyle with zero permitted errors or warnings, and verifies 100% line and branch coverage for the Minecraft-independent `domain` package. That percentage is intentionally not a claim of total-mod coverage; NeoForge glue is exercised through GameTests and runtime smoke tests.

Run the in-game P0 suite in both matched arms:

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

The analyzer fails closed on schema drift, missing or duplicate replicates, unmatched arms, failed navigation, exceptions, or observer work in the control arm.

## Runtime smoke tests

The dedicated-server task installs the tracked loopback-only P0 server configuration. It uses online authentication, disables query and RCON, and requests an ephemeral port.

```powershell
.\gradlew.bat --no-daemon --warning-mode fail runServer
```

Wait for the `Done` line, then enter `stop` in the same terminal. Never expose this disposable development run directory as a public server.

The integrated-client smoke requires a disposable local world named `p0-smoke` beneath `run\saves`. The retained P0 run used a copy of the dedicated smoke world; never point this test at a valued save. The development-only hook waits for a real local player and a ready integrated server, runs for 100 active client ticks, logs `BMAI_P0_INTEGRATED_SMOKE_PASS`, and requests a normal client shutdown:

```powershell
.\gradlew.bat --no-daemon --warning-mode fail runClient `
  -PbmaiQuickPlayWorld=p0-smoke `
  -PbmaiIntegratedSmoke=true
```

The hook is gated by both an explicit property and NeoForge's non-production environment flag; it is inert in production.

## OBSERVE behavior and rollback

`observation_enabled = true` attaches only after a mob's first completed server tick. It stores identity and already-computed position/navigation facts, releases per-entity state on leave, and clears all state on server stop. The join event deliberately does not read world or chunk data.

To disable all P0 instrumentation, copy [the example](config/better_minecraft_ai-common.toml.example) to the NeoForge common-config location or edit the generated `better_minecraft_ai-common.toml`, set `observation_enabled = false`, and restart at a normal server lifecycle boundary. P0 holds no movement lease and registers no persistent game content, so disabling or removing it leaves vanilla navigation ownership intact. Back up any valued world before changing a mod set even though P0 writes no save data.

## Ownership proof boundary

The test-only zombie proof does not replace `PathNavigation`. It adds one exact priority-0 `Goal` declaring vanilla's `MOVE` control flag, verifies that the `GoalSelector` grants exactly one running MOVE owner, submits a route through the zombie's existing navigation object, then disables and removes that exact goal. The test requires `stop()` exactly once, a stopped route, the original goal set restored, and the same navigation instance after fallback.

That proves a narrow, reversible lifecycle mechanism suitable for a future C0 adapter. It does **not** authorize shipping the adapter in P0. The exact source audit and remaining P1 conflict checks are in [HOOK_AUDIT.md](docs/compat/HOOK_AUDIT.md).

## Evidence and current limits

Start with the [P0 gate table](docs/release-evidence/p0/gate-status.md) and [benchmark baseline](docs/benchmarks/BASELINE.md). Accepted runtime logs are retained under `docs/release-evidence/p0/runtime/`; raw data and the machine-readable analysis are beside them.

Known boundaries:

- no enhanced navigation or performance-improvement claim;
- no global mixins, blanket goal clearing, entity-tick cancellation, forced chunk loading, background world access, GOAP, LLM, or asynchronous A*;
- unknown modded mobs, Brain-based entities, bosses, and specialist navigators are untouched;
- AzureCortex, SmartBrainLib, and Sable Pathfinder are research/interoperability subjects only, not dependencies;
- Windows and native Ubuntu GitHub CI are green for the implementation head in [run 38021120525](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38021120525); and
- upstream development-runtime warnings are catalogued in [KNOWN_UPSTREAM_WARNINGS.md](docs/KNOWN_UPSTREAM_WARNINGS.md), not suppressed.

Do not begin P1 until the director explicitly accepts this P0 candidate.

# Phase P0 evidence manifest

## Candidate identity

| Field | Value |
|---|---|
| Project | Better Minecraft AI |
| Candidate version | `0.1.0-p0` |
| Evidence date | 2026-10-11 |
| Git branch | `main` |
| Foundation commit | `9434a5606456b02ed6e81e01963dc1ffa9e03459` |
| Verified implementation head | `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3` |
| Accepted candidate commit | `5848143e16a48db2da38cd859f663b845cc7e62b` |
| Acceptance-record commit | The commit containing this file; exact SHA is reported in the handoff because a commit cannot embed its own SHA |
| Tag | None |
| Remote | `origin = https://github.com/MrWizard94-Compile/Better-Minecraft-AI.git` |
| Remote state observed | Initially empty; authorized `main` history is now pushed |
| Director acceptance | 2026-10-11; exact statement: `I accept P0` |
| Current release status | `P0 DONE — DIRECTOR ACCEPTED`; A1 remains entirely `NOT RUN` |

The immutable implementation chain is:

| Commit | Purpose |
|---|---|
| `9434a5606456b02ed6e81e01963dc1ffa9e03459` | P0 foundation, ownership proof, baseline, smoke evidence, and initial evidence package |
| `41386c1ee5e1cce667bd2999a598ae96feb49fd2` | Complete clean-runner dependency verification metadata and Node 24 action updates |
| `bdee303b896be5db20cb84705206da611fc948f8` | Make the OS-conditional Netty lock treatment portable while retaining exact version and artifact verification |
| `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3` | Add the Linux-selected LWJGL native checksums; first fully green cross-platform implementation run |

Commit `5848143e16a48db2da38cd859f663b845cc7e62b` finalized the candidate evidence and passed GitHub Actions run `38157185309` before director acceptance. The acceptance-record commit changes documentation/evolution evidence and corrects only the Unix wrapper's Git mode from `100644` to `100755`; implementation bytes and accepted measurements are unchanged. Its exact SHA and CI result are reported in the final handoff rather than self-referentially encoded here.

## Direct build pins

| Component | Pin |
|---|---|
| Java toolchain / executed JDK | 21 / Temurin 21.0.10+7 LTS |
| Gradle | 9.2.1 |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.256 |
| ModDevGradle | 2.0.148 |
| Parchment | 1.21.1 / 2024.11.17 |
| Checkstyle | 10.26.1 |
| JaCoCo | 0.8.15 |
| JUnit Jupiter / Platform | 5.14.4 / 1.14.4 |
| JSpecify | 1.0.1 |

Full transitive locks and checksums are in `gradle.lockfile` and `gradle/verification-metadata.xml`. See [`docs/BUILD_ENVIRONMENT.md`](../../BUILD_ENVIRONMENT.md) for host/provenance detail.

## Input and generated artifact hashes

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| `gradle/wrapper/gradle-wrapper.jar` | 43,764 | `7D3A4AC4DE1C32B59BC6A4EB8ECB8E612CCD0CF1AE1E99F66902DA64DF296172` |
| Gradle 9.2.1 binary distribution | — | `72F44C9F8EBCB1AF43838F45EE5C4AA9C5444898B3468AB3F4AF7B6076C5BC3F` |
| NeoForge 21.1.256 sources JAR | — | `428CB928348747DE535D8CC59B134C04CA6DB6B55B6AF6BE2477CD966721E800` |
| `src/main/resources/data/better_minecraft_ai/structure/empty.nbt` | 113 | `DFC0B3121FFF129E4150686FDCB680134B3EA66E763EE139292BF65CFF34E685` |
| `build/libs/better_minecraft_ai-0.1.0-p0.jar` | 27,061 | `392EFB9F3A7AC14AC3E6E03D1B578AB14B592279126EF54EC849C8394A603043` |
| `build/libs/better_minecraft_ai-0.1.0-p0-sources.jar` | 12,757 | `D923FC4551ABB5367671EA9426C442ABEA3FC6635E24929B3D56651C6AD47B3A` |

The two project JARs were rebuilt with `jar sourcesJar --rerun-tasks`; before/after SHA-256 values were identical. Build outputs are intentionally ignored and are not represented as distributed releases.

## Retained raw-evidence hashes

| File | Bytes | SHA-256 |
|---|---:|---|
| `benchmark-data/p0-vanilla_control.csv` | 654 | `69AE6C89A0A33D4C03801D24CA8CB3F4AAA6F9F6297A098E9840AB2F3097C5C2` |
| `benchmark-data/p0-observe.csv` | 644 | `0C641ED8521FD69002F72571972BAAF813B70392EC17915791D20933F275B7A8` |
| `runtime/gametest-vanilla-control.log` | 8,613 | `601E2DD880A8D813A6D953430D29F68C2DF9D1029A9B7A01100B10533D16F779` |
| `runtime/gametest-observe.log` | 8,487 | `02A9E5270D3DCC87B2D4661A6157E0ABBC9BED16DDD25BBBA728B703AB21262E` |
| `runtime/dedicated-server.log` | 7,659 | `4C9FF1EF7E947AFA2D33FB823F57E8587C097D9F155B2CFCDA19C847BDCD6A4C` |
| `runtime/integrated-client.log` | 12,900 | `F4627C5A2F9FE2343168513FEA965984A2D1B0338D9FF5A727BBDBE8307012AA` |

Raw text evidence is normalized to LF by repository policy; its content and timestamps remain the captured runtime output.

## Remote CI evidence

GitHub Actions [run 38021120525](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38021120525) is the first fully green implementation run:

| Job / artifact | Identity | Result |
|---|---|---|
| Paired P0 GameTests | job `114122114622` | SUCCESS |
| Ubuntu build | job `114122114688` | SUCCESS |
| Windows build | job `114122114730` | SUCCESS |
| Required gates | job `114122471112` | SUCCESS |
| `p0-gametest-evidence` artifact | ID `11658831409`; 19,503 bytes; SHA-256 `605BC9D375D8669C968B6251252F92F8CF50002C53F9FE0781051737789C3BB8` | Retained by GitHub through 2026-10-24 |

The downloaded CI artifact records commit `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3` on a Linux x64 runner, 5/5 samples in each arm, zero exceptions, median/p95 duration 113/114 ticks in both arms, 14 nodes, zero stalls, maximum paired final-distance delta 0.000498 blocks, and zero observer control-arm time.

Accepted candidate commit `5848143e16a48db2da38cd859f663b845cc7e62b` then passed [run 38157185309](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38157185309) with the same four required jobs green and zero annotations. Its `p0-gametest-evidence` artifact is ID `11709680150`, 19,593 bytes, SHA-256 `8DB66BEACB8C026E331522584FF529330C69A989F75B266CFE9A9480F929B385`, retained through 2026-10-25; the maximum paired final-distance delta was 0.000456 blocks and all categorical outcomes remained matched.

## Source/evidence inventory

The exact candidate file inventory is obtained with `rg --files`. Major groups are:

- supplied authority: `Codex_P0_Kickoff_Better_Minecraft_AI.md`, engineering blueprint, and Definition of Done;
- build/tooling: Gradle build/settings/properties, complete wrapper, lockfile, verification metadata, Checkstyle, CI;
- implementation: mod entry point, config, observer, integrated smoke hook, GameTests, domain types and unit tests;
- deterministic fixture/tooling: empty structure and generator, benchmark analyzer, local smoke configs;
- documentation: README, build environment, ADR, hook audit, baseline, warning ledger, P0 checklist, changelog, and notices; and
- required evidence package: gate/build/test/compatibility/benchmark/profiling/runtime/risk/release records.

No AzureCortex, SmartBrainLib, Sable, or Sable Pathfinder class/dependency is present in the built JAR or resolved project dependencies.

## Exact file-change inventory

The three supplied authority documents were preserved with unchanged textual content:

```text
Better_Minecraft_AI_Definition_of_Done.md
Better_Minecraft_AI_Engineering_Blueprint.md
Codex_P0_Kickoff_Better_Minecraft_AI.md
```

P0 added these exact 60 paths:

```text
.gitattributes
.github/workflows/ci.yml
.gitignore
build.gradle
CHANGELOG.md
config/better_minecraft_ai-common.toml.example
config/checkstyle/checkstyle.xml
config/p0-client-options.txt
config/p0-server.properties
docs/adr/ADR-001-ai-framework-selection.md
docs/benchmarks/BASELINE.md
docs/BUILD_ENVIRONMENT.md
docs/compat/HOOK_AUDIT.md
docs/KNOWN_UPSTREAM_WARNINGS.md
docs/P0_CHECKLIST.md
docs/release-evidence/p0/benchmark-data/p0-observe.csv
docs/release-evidence/p0/benchmark-data/p0-vanilla_control.csv
docs/release-evidence/p0/benchmark-summary.json
docs/release-evidence/p0/benchmark-summary.md
docs/release-evidence/p0/build-and-ci.md
docs/release-evidence/p0/compatibility-matrix.md
docs/release-evidence/p0/constitution-skill-evolution.md
docs/release-evidence/p0/constitution-skill-evolution.patch
docs/release-evidence/p0/gate-status.md
docs/release-evidence/p0/manifest.md
docs/release-evidence/p0/profiling/hardware-and-jvm.md
docs/release-evidence/p0/release-decision.md
docs/release-evidence/p0/risks-and-limitations.md
docs/release-evidence/p0/section-0-self-audit.md
docs/release-evidence/p0/runtime/dedicated-server.log
docs/release-evidence/p0/runtime/gametest-observe.log
docs/release-evidence/p0/runtime/gametest-vanilla-control.log
docs/release-evidence/p0/runtime/integrated-client.log
docs/release-evidence/p0/test-results.md
gradle.lockfile
gradle.properties
gradle/verification-metadata.xml
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
gradlew
gradlew.bat
LICENSES/NeoForge-MDK-MIT.txt
README.md
settings.gradle
src/main/java/com/mrwizard94/betterminecraftai/BetterAiConfig.java
src/main/java/com/mrwizard94/betterminecraftai/BetterMinecraftAi.java
src/main/java/com/mrwizard94/betterminecraftai/BetterMinecraftAiGameTests.java
src/main/java/com/mrwizard94/betterminecraftai/domain/MotionState.java
src/main/java/com/mrwizard94/betterminecraftai/domain/ObservationOverride.java
src/main/java/com/mrwizard94/betterminecraftai/domain/package-info.java
src/main/java/com/mrwizard94/betterminecraftai/ObservationService.java
src/main/java/com/mrwizard94/betterminecraftai/P0IntegratedClientSmoke.java
src/main/java/com/mrwizard94/betterminecraftai/package-info.java
src/main/resources/data/better_minecraft_ai/structure/empty.nbt
src/main/templates/META-INF/neoforge.mods.toml
src/test/java/com/mrwizard94/betterminecraftai/domain/MotionStateTest.java
src/test/java/com/mrwizard94/betterminecraftai/domain/ObservationOverrideTest.java
THIRD_PARTY_NOTICES.md
tools/AnalyzeP0Benchmark.ps1
tools/GenerateEmptyGameTestStructure.java
```

Repository initialization also created `.git/` metadata, configured branch `main`, and added the requested `origin`; internal Git database files are not project deliverables and are not enumerated above.

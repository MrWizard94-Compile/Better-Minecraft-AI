# Phase P0 build and CI evidence

Local commands ran from the repository root on 2026-10-09 through 2026-10-11. Remote clean-runner evidence is from GitHub Actions on 2026-10-11. Exit status `0` means the process completed successfully; runtime proof is linked separately rather than inferred from Gradle compilation.

## Accepted commands

| Command | Exit | Observed result |
|---|---:|---|
| `git init -b main` | 0 | Nested project repository initialized without touching the parent WPAI repository. |
| `git remote add origin https://github.com/MrWizard94-Compile/Better-Minecraft-AI.git` | 0 | Exact requested remote configured. |
| `git ls-remote --symref origin HEAD` / `git ls-remote origin` | 0 | Remote reachable and initially empty before the authorized history was pushed. |
| Official MDK `gradlew.bat --version` before import | 0 | Official scaffold wrapper launched. |
| `.\gradlew.bat --no-daemon --warning-mode fail dependencies --write-locks --write-verification-metadata sha256` | 0 | Lockfile and component SHA-256 verification metadata generated. |
| `.\gradlew.bat --version` | 0 | Gradle 9.2.1, Temurin 21.0.10; Windows launcher works. |
| `C:\Program Files\Git\bin\bash.exe ./gradlew --version` | 0 | POSIX wrapper script launches through Git Bash and reports the same Gradle/JDK; native Ubuntu execution is independently green in CI. |
| `.\gradlew.bat --no-daemon --warning-mode fail clean build printBuildEnvironment dependencies` | 0 | `BUILD SUCCESSFUL` in 13 s; 15 tasks executed/from cache; direct pins printed; dependency graph resolved under locks/verification. |
| `.\gradlew.bat --no-daemon --warning-mode fail test checkstyleMain checkstyleTest jacocoTestCoverageVerification jacocoTestReport --rerun-tasks` | 0 | `BUILD SUCCESSFUL` in 18 s; all 10 tasks executed rather than served from Gradle task cache. |
| `.\gradlew.bat --no-daemon --warning-mode fail runGameTestServer ... vanilla_control ...` | 0 | Seven required GameTests passed; accepted log/raw CSV copied. |
| `.\gradlew.bat --no-daemon --warning-mode fail runGameTestServer ... observe ...` | 0 | Seven required GameTests passed; accepted log/raw CSV copied. |
| `.\tools\AnalyzeP0Benchmark.ps1` | 0 | Exactly five samples per arm validated; matched behavior true; JSON/Markdown summaries generated. |
| `.\gradlew.bat --no-daemon --warning-mode fail runServer`, then console `stop` | 0 | Server bound to `127.0.0.1:0`, reached ready, handled stop, saved all dimensions; `BUILD SUCCESSFUL` in 33 s. |
| `.\gradlew.bat --no-daemon --warning-mode fail runClient -PbmaiQuickPlayWorld=p0-smoke -PbmaiIntegratedSmoke=true` | 0 | Real local player/ready integrated server ran for 100 active ticks; normal client/server shutdown and dimension saves. |
| `.\gradlew.bat --no-daemon --warning-mode fail jar sourcesJar --rerun-tasks` | 0 | Both runtime and source JAR hashes were byte-identical before/after rebuild. |
| Python/PyYAML read-only parse of `.github/workflows/ci.yml` | 0 | YAML parsed with exactly `build`, `gametest`, and `required-gates` jobs. |
| PowerShell AST parse of `tools/AnalyzeP0Benchmark.ps1` | 0 | Zero syntax errors. |
| Per-file `git diff --no-index --check` over 43 non-log text files | 0 (custom aggregate) | Zero authored whitespace errors; supplied Markdown hard breaks are covered by explicit attributes and raw runtime logs are retained as evidence. |
| GitHub Actions run `38021120525` | 0 | Windows build, native Ubuntu build, paired P0 GameTests, and required-gates jobs all succeeded for implementation head `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3`. |

## Build results

- Java uses `-Xlint:all -Werror`.
- Checkstyle 10.26.1 reports 0 errors across 9 main and 2 test Java files; configured maximum errors/warnings are both zero.
- Unit tests: 11 passed, 0 failed, 0 errors, 0 skipped.
- MC-free domain JaCoCo: 21/21 lines and 19/19 branches, both 100%.
- Runtime JAR: 27,061 bytes, SHA-256 `392EFB9F3A7AC14AC3E6E03D1B578AB14B592279126EF54EC849C8394A603043`.
- Source JAR: 12,757 bytes, SHA-256 `D923FC4551ABB5367671EA9426C442ABEA3FC6635E24929B3D56651C6AD47B3A`.
- Built JAR contains no AzureCortex, SmartBrainLib, Sable, or Sable Pathfinder class-name match.
- `gradlew` is tracked with Git executable mode `100755`; CI retains a defensive `chmod +x` step for archive/filesystem transports that may not preserve mode metadata.

Build reports are generated under ignored `build/reports/` and can be reproduced with the commands above. The retained gate facts are recorded here so the evidence package does not depend on a local ignored directory.

## CI workflow status

`.github/workflows/ci.yml` is present and defines:

- SHA-pinned checkout, wrapper validation, Java setup, Gradle setup, and artifact upload actions;
- Windows and Ubuntu clean build/test/Checkstyle/coverage/dependency jobs;
- both GameTest arms plus fail-closed benchmark analysis on Ubuntu;
- retained success/failure GameTest data/log artifacts; and
- one final required-gates job that fails unless the build matrix and GameTests succeed.

Automatic release/publish behavior is absent.

**GitHub Actions: PASS for the implementation head.** [Run 38021120525](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38021120525) completed with no GitHub annotations:

| Job | Job ID | Result |
|---|---:|---|
| Paired P0 GameTests | `114122114622` | SUCCESS |
| Ubuntu build | `114122114688` | SUCCESS |
| Windows build | `114122114730` | SUCCESS |
| Required gates | `114122471112` | SUCCESS |

The retained `p0-gametest-evidence` artifact is ID `11658831409`, 19,503 bytes, with GitHub digest `sha256:605bc9d375d8669c968b6251252f92f8cf50002c53f9fe0781051737789c3bb8`. The downloaded bundle identifies Linux x64 and commit `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3`; it contains five successful samples per arm, zero exceptions, median/p95 duration 113/114 ticks in both arms, 14 path nodes, zero stalls, maximum paired final-distance delta 0.000498 blocks, and zero observer time in the control arm.

The later accepted candidate `5848143e16a48db2da38cd859f663b845cc7e62b` independently repeated every required job in [run 38157185309](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38157185309): Ubuntu job `114522474492`, Windows job `114522474661`, paired GameTests job `114522474665`, and required-gates job `114522878971` all succeeded with zero annotations. Artifact ID `11709680150` is 19,593 bytes with digest `sha256:8db66beacb8c026e331522584ff529330c69a989f75b266cfe9a9480f929b385`; its final-distance delta is 0.000456 blocks and all other categorical/duration/node/stall outcomes remain matched.

## Repair history retained for auditability

Early diagnostic runs were not accepted as evidence:

- the initial ownership/benchmark goal was registered too early, before the mob completed initialization; activation was deferred two ticks;
- vanilla navigation settles roughly 1.29 blocks from the target under its completion semantics, so the intended arrival rule was corrected to require goal start, accepted movement, navigation done, and a fixed 1.5-block tolerance—not weakened to “path exists”;
- callbacks were initially registered from a delayed callback, violating GameTest scheduler expectations; all callbacks are now registered during setup;
- GameTest retry/attempt metadata produced one retained row, so five explicit fixed test methods now guarantee five raw samples; and
- the first dedicated smoke inherited a broad generated bind and was interrupted; it was rejected and replaced with the tracked loopback-only configuration plus a normal `stop` shutdown.

The benchmark analyzer's first packaging run exposed a PowerShell join-precedence defect and exited nonzero; the expression was fixed, syntax-checked, and the accepted analyzer run exits 0. No failed or insecure run is used as PASS evidence.

Clean-runner CI also found issues that the warmed Windows development cache could not expose:

- run `37991429529` rejected incomplete verification metadata for JUnit and Guava metadata files;
- run `38020508666` passed Windows but Linux selected `io.netty:netty-transport-native-epoll:4.1.97.Final`, which cannot be represented portably in a lock produced by the Windows resolution graph;
- run `38020958427` passed Windows and the lock treatment, then rejected eight Linux-selected LWJGL native artifacts whose checksums were absent; and
- run `38021120525` passed after the missing metadata/native checksums were corroborated against authoritative repositories and recorded.

The repair does not disable verification. Only the OS-conditional Netty native module is omitted from the shared lock state; its version remains fixed by strict Minecraft metadata and its artifacts remain checksum-verified. Linux and Windows remain required matrix jobs. Checkout, Java setup, Gradle setup, and artifact upload actions were also moved to SHA-pinned Node 24-capable revisions; Gradle Actions v5 was deliberately retained because v6 introduces additional proprietary caching terms.

Two diagnostic command failures are retained as non-evidence: one analyzer invocation used obsolete parameters before being corrected to the documented interface, and one isolated metadata experiment used the wrong working directory/Gradle user-home placement and was rejected by `clean`. Neither result is counted as an accepted gate.

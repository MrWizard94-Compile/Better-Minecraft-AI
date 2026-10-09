# Phase P0 build and CI evidence

All commands ran from the repository root on 2026-10-09. Exit status `0` means the process completed successfully; runtime proof is linked separately rather than inferred from Gradle compilation.

## Accepted commands

| Command | Exit | Observed result |
|---|---:|---|
| `git init -b main` | 0 | Nested project repository initialized without touching the parent WPAI repository. |
| `git remote add origin https://github.com/MrWizard94-Compile/Better-Minecraft-AI.git` | 0 | Exact requested remote configured. |
| `git ls-remote --symref origin HEAD` / `git ls-remote origin` | 0 | Remote reachable; no remote refs returned. |
| Official MDK `gradlew.bat --version` before import | 0 | Official scaffold wrapper launched. |
| `.\gradlew.bat --no-daemon --warning-mode fail dependencies --write-locks --write-verification-metadata sha256` | 0 | Lockfile and component SHA-256 verification metadata generated. |
| `.\gradlew.bat --version` | 0 | Gradle 9.2.1, Temurin 21.0.10; Windows launcher works. |
| `C:\Program Files\Git\bin\bash.exe ./gradlew --version` | 0 | POSIX wrapper script launches through Git Bash and reports the same Gradle/JDK. Actual Ubuntu execution is delegated to CI and remains unobserved until an authorized push. |
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

## Build results

- Java uses `-Xlint:all -Werror`.
- Checkstyle 10.26.1 reports 0 errors across 9 main and 2 test Java files; configured maximum errors/warnings are both zero.
- Unit tests: 11 passed, 0 failed, 0 errors, 0 skipped.
- MC-free domain JaCoCo: 21/21 lines and 19/19 branches, both 100%.
- Runtime JAR: 27,061 bytes, SHA-256 `392EFB9F3A7AC14AC3E6E03D1B578AB14B592279126EF54EC849C8394A603043`.
- Source JAR: 12,757 bytes, SHA-256 `D923FC4551ABB5367671EA9426C442ABEA3FC6635E24929B3D56651C6AD47B3A`.
- Built JAR contains no AzureCortex, SmartBrainLib, Sable, or Sable Pathfinder class-name match.

Build reports are generated under ignored `build/reports/` and can be reproduced with the commands above. The retained gate facts are recorded here so the evidence package does not depend on a local ignored directory.

## CI workflow status

`.github/workflows/ci.yml` is present and defines:

- SHA-pinned checkout, wrapper validation, Java setup, Gradle setup, and artifact upload actions;
- Windows and Ubuntu clean build/test/Checkstyle/coverage/dependency jobs;
- both GameTest arms plus fail-closed benchmark analysis on Ubuntu;
- retained success/failure GameTest data/log artifacts; and
- one final required-gates job that fails unless the build matrix and GameTests succeed.

Automatic release/publish behavior is absent.

**GitHub Actions: NOT RUN.** The remote is empty, and neither a commit nor push is authorized. Workflow syntax and local constituent commands are green; that is not represented as remote CI success.

## Repair history retained for auditability

Early diagnostic runs were not accepted as evidence:

- the initial ownership/benchmark goal was registered too early, before the mob completed initialization; activation was deferred two ticks;
- vanilla navigation settles roughly 1.29 blocks from the target under its completion semantics, so the intended arrival rule was corrected to require goal start, accepted movement, navigation done, and a fixed 1.5-block tolerance—not weakened to “path exists”;
- callbacks were initially registered from a delayed callback, violating GameTest scheduler expectations; all callbacks are now registered during setup;
- GameTest retry/attempt metadata produced one retained row, so five explicit fixed test methods now guarantee five raw samples; and
- the first dedicated smoke inherited a broad generated bind and was interrupted; it was rejected and replaced with the tracked loopback-only configuration plus a normal `stop` shutdown.

The benchmark analyzer's first packaging run exposed a PowerShell join-precedence defect and exited nonzero; the expression was fixed, syntax-checked, and the accepted analyzer run exits 0. No failed or insecure run is used as PASS evidence.

# Codex Kickoff — Better Minecraft AI — Phase P0 Only

**Read and follow `Better_Minecraft_AI_Engineering_Blueprint.md` and `Better_Minecraft_AI_Definition_of_Done.md`.** The blueprint specifies implementation; the DoD specifies exactly which evidence and approvals are required before P0 or A1 can be called done. Any missing mandatory result is BLOCKED/NOT RUN, not PASS. It defines quality gates, scope, failure conditions, and the staged roadmap. Work on **Phase P0 only**; do not pretend subsequent phases are finished.

## Mission

Establish a reliable Minecraft **NeoForge 1.21.1 / Java 21** project, a minimal observation-only AI integration, benchmark methodology, and an evidence-based compatibility audit. This project will later improve navigation and behavior for vanilla entities while providing reusable APIs to Golem Guards and other mods. Do not develop a universal AI replacement on this first pass.

## Mandatory sequence

1. Inspect the current repository and its Git status. Preserve all existing work. If empty, bootstrap using the official **1.21.1 ModDevGradle MDK**. Confirm the exact accessible versions rather than guessing artifacts.
2. Verify that both Windows PowerShell (`.\gradlew.bat`) and Linux/macOS (`./gradlew`) launch the Gradle wrapper. Commit the complete Gradle wrapper and Java 21 toolchain settings.
3. Install Checkstyle **10.x** and JaCoCo verification with a **100% line and branch coverage requirement for Minecraft-independent deterministic domain code only**; use GameTests and manual runtime tests for NeoForge-specific glue. No raw generics; apply a clear nullness annotation policy.
4. Implement a safe baseline `OBSERVE` mode that instruments relevant server-side entities **without changing decisions, targets, goals, navigation, tick rates or world state**. Do not read the world during `EntityJoinLevelEvent` if chunk readiness is uncertain; defer work safely.
5. Establish the first real unit test, the first NeoForge GameTest, basic dedicated-server startup, and honest output artifacts/logs. `build` alone is not evidence of working AI.
6. Audit exact Minecraft 1.21.1 source and document viable integration points for `Mob`, `GoalSelector`, `PathNavigation`, `NodeEvaluator`, `Brain`, relevant NeoForge events, and fallback/lifecycle handling. **Explain how movement ownership would be transferred safely for a single vanilla zombie; do not assume a generic tick event can take over navigation.**
7. Evaluate AzureCortex (MIT), SmartBrainLib (MPL-2.0), and Sable Pathfinder (marked ARR) as research/integration subjects. Write `ADR-001-ai-framework-selection.md`; do not add one as a required runtime dependency without measurements and justification. Do not copy ARR code.
8. Define paired baseline scenarios with same seeds, mob cohorts, JVM settings, warmup periods, and raw metrics (per-entity navigation progress, AI CPU, p95/p99 server tick time, exceptions). Run what this environment permits; clearly mark anything not run.
9. Create `README.md`, `docs/compat/HOOK_AUDIT.md`, `docs/benchmarks/BASELINE.md`, `docs/adr/ADR-001-ai-framework-selection.md`, and CI scaffolding, plus a checklist of exactly what Phase P0 requires for approval.

## Hard constraints

- Never overwrite, reset, publish or push repository content without instruction.
- Do not claim build/test/performance success without executing and retaining evidence.
- Do not implement global mixin `@Overwrite`, clear unknown mobs' goals, cancel whole entity ticks, access `Level` on background workers, or force chunk loads.
- Unknown modded mobs and Brain-specific entities remain unchanged.
- No new client requirements, Sable integration, GOAP, LLMs, complex combat behavior or asynchronous A* yet.
- Keep changes in small coherent increments; resolve legitimate test failures by fixing code, not weakening assertions.

## Final response contract

Return: (1) exact files changed; (2) Gradle dependency pins; (3) commands run and exit statuses; (4) unit/GameTest/smoke outcomes, including skips; (5) baseline data or reason unavailable; (6) architecture hook audit and decision; (7) outstanding risks; (8) next smallest task for P1. **Do not proceed to P1 as though P0 is approved.**

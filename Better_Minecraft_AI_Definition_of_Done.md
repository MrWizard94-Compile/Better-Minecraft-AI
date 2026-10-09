# Better Minecraft AI — Definition of Done (DoD)

**Document ID:** BMAI-DoD-001  
**Status:** Binding acceptance contract for Codex; proposed numeric release targets pending initial P0 baseline and director ratification  
**Applies to:** Minecraft Java 1.21.1 · NeoForge 21.1.x · Java 21  
**Companion specification:** `Better_Minecraft_AI_Engineering_Blueprint.md`  
**Initial delivery:** P0 = research/observability foundation; A1 = first stable navigation release  
**Out of scope for A1:** A2 perception, A3 tactics, A4 group coordination, A5 Sable/dynamic worlds, Track B Living AI

> **CODEX EXECUTION CONTRACT**
>
> A task, milestone, or release is **DONE only when every applicable mandatory gate below is PASS, supported by verifiable artifacts, and the director has accepted the milestone**. A successful Gradle build is not proof of working gameplay. Unrun checks are `NOT RUN`, not `PASS`; missing tools/data are `BLOCKED`, not a waiver. Fix product defects rather than weakening legitimate tests. Do not proceed from P0 to A1 without approval.

---

## 1. What the word DONE means

There are **four distinct scopes**. Never use an unqualified “done” that blurs them:

| Scope | Meaning | Acceptance authority |
|---|---|---|
| `SLICE DONE` | One limited task satisfies its own tests and evidence contract. | Codex may self-report evidence; director may review. |
| `P0 DONE` | Reproducible engineering foundation, truthful baseline and validated hook strategy; **no improved AI promised**. | Director approves P0 gate. |
| `A1 DONE` | A shippable, validated, demonstrably better navigation system for the explicit C0 zombie cohort with reliable fallback. | Director approves A1 release. |
| `TRACK A COMPLETE` | Subsequent separately approved A2–A5 capabilities, including supported perception, tactics, cooperation and dynamic-world adapters, each with its own acceptance criteria. | Separate future acceptance; **not** equal to A1 DONE. |

Track B (“Living AI,” persistent autonomous intelligence) is a **future project track** and cannot be represented as part of A1 or Track A by implication.

Use exactly one status per criterion: `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`, or `NOT APPLICABLE` (with rationale). A mandatory `FAIL`, `BLOCKED` or `NOT RUN` prevents the milestone from being DONE; `NOT APPLICABLE` needs written justification and director approval for any previously mandatory requirement.

### Gate precedence

1. Safety/data integrity and behavioral correctness outrank performance or delivery deadlines.
2. This DoD controls **what acceptance evidence is required**; the blueprint controls architecture, implementation policy, and scope.
3. If the documents conflict materially, stop and record a change proposal/ADR; do not choose the easier rule silently.
4. Threshold changes require a documented reason, baseline evidence and **director approval before the next scored evaluation**. Never move goalposts after seeing a failing result.

---

## 2. Universal Definition of Done for every code slice

A code change is `SLICE DONE` only if all are true:

- [ ] The acceptance intent and touched module boundaries are documented in a PR/commit note or slice report.
- [ ] The code targets the **exact pinned** Minecraft 1.21.1 / NeoForge / Java 21 dependencies; any API change is verified against checked-out sources, not recalled signatures.
- [ ] No raw generic types, unsafe null handling, unbounded collection/work loop, unreviewed reflection hack, silent catch-and-ignore, or unauthorized global AI overwrite was introduced.
- [ ] Server owns AI, combat, targeting and world mutations. Background workers (if later introduced) never read/mutate live `Level`/entity objects.
- [ ] Relevant deterministic MC-independent tests exist and pass; Minecraft-bound changes have focused GameTests and/or documented runtime tests.
- [ ] Checkstyle 10.x and the applicable JaCoCo checks pass. MC-independent deterministic domain code meets **100% line and branch coverage**, with no coverage-gaming exclusions. Minecraft glue is tested via GameTests/smoke tests.
- [ ] Changes to configuration or persistence include malformed-input, default-value and upgrade/downgrade/disable scenarios as appropriate.
- [ ] The change does not unintentionally seize goal/navigation ownership from unsupported entities.
- [ ] The exact commands run, exit codes, logs and any tests **not run** are reported. A test skipped because the environment lacks Minecraft or a GUI is not called passed.
- [ ] Documentation and debug traces explain the implemented behavior and its known limits.
- [ ] Existing passing tests were not deleted, relaxed or skipped to conceal a product defect; if a test was incorrect, the correction and reason are reviewed.

**Minimum slice report:** scope; files/classes changed; reason; commands/versions; observed PASS/FAIL/NOT RUN; functional proof; performance impact or `NOT MEASURED`; risks; next task.

---

## 3. P0 DONE — Foundation and compatibility research gate

P0 is not a better-pathfinding release. It proves the repo is valid and identifies *how* a safe enhancement could be built.

### P0 required evidence checklist

- [ ] A complete, committed Gradle wrapper exists (`gradlew`, `gradlew.bat`, wrapper JAR, properties); `clean build` succeeds from a clean checkout after dependencies are available.
- [ ] Exact Gradle, ModDevGradle, Minecraft, NeoForge, mappings, JDK and test-tool versions are pinned and printed in `docs/BUILD_ENVIRONMENT.md` or equivalent.
- [ ] Java 21, Checkstyle 10.x, JaCoCo and a runnable test suite are configured; MC-free domain example satisfies configured coverage gate.
- [ ] A dedicated server launches and shuts down cleanly with only NeoForge + BMAI; log is retained.
- [ ] An integrated/client test is run and recorded if the test environment supports it. If unavailable, P0 remains `BLOCKED` **unless the director explicitly re-scopes P0 to permit a provisional foundation**; it does not become a verified client pass.
- [ ] `OBSERVE` mode runs with **no changes** to entity goals, target selection, movement controller, path ownership, aggression, stats or spawning; runtime assertions/tests demonstrate non-interference.
- [ ] At least one MC-free unit test and one meaningful in-game GameTest pass, with actual execution logs.
- [ ] `docs/compat/HOOK_AUDIT.md` cites exact-version implementation points for `Mob`, `GoalSelector`, `PathNavigation`, node evaluators, `Brain` and relevant NeoForge events; explains ownership transfer, timing and fallback for a vanilla zombie.
- [ ] `docs/adr/ADR-001-ai-framework-selection.md` evaluates native hooks, AzureCortex and SmartBrainLib, including licensing, technical compatibility and lifecycle costs; Sable Pathfinder is assessed as an interoperability concern without copying restricted code.
- [ ] `docs/benchmarks/BASELINE.md` defines fixed scenes, seeds, warmup, cohorts, repeats, hardware/JVM/server settings, outcome definitions and logging. A real vanilla/OBSERVE baseline has been run and retained, **or P0 is BLOCKED for benchmarking**.
- [ ] No global `@Overwrite`, blanket goal clearing, whole-entity tick cancellation, forced chunk loads, or background access to live world data.
- [ ] Repository includes README, CI workflow, configuration example, test instructions, and machine-readable/test logs sufficient to reproduce the P0 result.
- [ ] Codex provides an explicit P0 gate table and stops, without treating unapproved P1/A1 work as authorized.

**P0 acceptance statement:** “The project builds, instruments without modifying AI, has repeatable tests and baseline, and has an evidence-backed zombie enhancement insertion strategy.” **Not:** “Minecraft AI is improved.”

---

## 4. A1 DONE — Minimum production release contract

A1 is the **first usable product**. It offers a verified enhancement for **vanilla zombies (C0)**; all other unsupported entities retain vanilla AI by default. The global default remains `OBSERVE` until stronger rollout evidence justifies changing it. A server operator can explicitly enable `ENHANCED` for the supported C0 profile.

### Gate A1-01 — Functional behavior (mandatory)

- [ ] A *real, ordinary vanilla zombie* in a NeoForge server uses the enhanced navigation pipeline, not merely a standalone pathfinding demo or a renamed vanilla call.
- [ ] Exactly one navigation controller owns each enhanced entity at any instant; goal changes and disabling the feature transfer control safely.
- [ ] For a reachable static goal, the zombie arrives within the scenario-defined tolerance and settles without indefinite oscillation.
- [ ] For an unreachable goal, the planner returns a bounded, actionable failure instead of attempting the same impossible path every tick forever.
- [ ] At least these deterministic GameTest scenes pass: straight corridor; 3D/stairs; narrow collision clearance; fence/wall obstacle; hazard with a safe alternative; newly opened/closed passage; interrupted goal/target change; target lost from view; unloaded-chunk boundary; unload/reload; fallback/disable mid-navigation; excluded boss/unknown mob.
- [ ] Route selection and execution honor entity hitbox, collision shapes, loaded chunks, allowed doors, water/lava costs, drops and available movement capabilities. Path validation cannot become teleport-based recovery.
- [ ] No accidental modifications to zombie base speed, attack damage, target eligibility, aggro range, loot, spawning, daylight/fire handling or difficulty mechanics.
- [ ] Stuck recovery respects world state: bounded retries, backoff, fresh path validation, and eventual vanilla-safe fallback; no permanent freeze or pathological retry storm.

### Gate A1-02 — Quantified improvement (mandatory)

**The evaluation protocol is part of acceptance, not optional marketing:**

1. Freeze challenge scenes and success/timing definitions **before implementation tuning** in `docs/benchmarks/ACCEPTANCE_SCENES.md`.
2. Test three matched modes: vanilla/NeoForge, BMAI `OBSERVE`, BMAI C0 `ENHANCED`, with matching world seed, scene, entity positions, server settings, JDK, hardware and warmup.
3. Run at least **20 independent path attempts per scene per mode across at least 3 predefined world seeds**. Capture raw results per attempt. For tick performance, use at least **5 measured replicates** per load level after warmup.
4. An attempt must have a clear timeout; a timed-out attempt is a failure, not silently omitted. Record median and p95 travel time for successful attempts, arrival rate, retry count, path nodes, stall count, hazard incidents and invalidation count.
5. **Proposed A1 acceptance targets** (ratify at the P0 exit using baseline measurements):
   - On curated *reachable static* scenes, `ENHANCED` arrival success **≥95%** within each scene's fixed timeout.
   - In at least **one predeclared nontrivial challenge**, enhanced navigation achieves **either** ≥15 percentage points higher arrival rate than vanilla **or** ≥20% lower median arrival time with no lower arrival success.
   - Across other common reachable scenes, no >5 percentage-point degradation in arrival rate and no material new collision/hazard regression.
   - `OBSERVE` results remain behaviorally equivalent to vanilla within defined trial noise; statistically surprising differences require root-cause investigation.
6. Raw CSV/JSON, reproducible scene definitions, runner scripts, profiler exports and a plain-English summary must be retained. An anecdotal “looks smarter” is **not** evidence.

These numbers are **targets, not claims of measured performance**. If P0 data shows a target is scientifically unsound or infeasible, the director may replace it with a documented, measurable threshold **before** A1 scoring starts. Zero comparative improvement means **NOT DONE**, even if all tests pass.

### Gate A1-03 — Tick cost, memory and load behavior (mandatory)

- [ ] Benchmark 1, 8, 32, 64 supported active zombies (or justify a more representative configuration with director approval) in comparable scenes; run vanilla, OBSERVE and ENHANCED arms.
- [ ] Proposed release budget: C0 ENHANCED p95 server tick cost must not exceed matched OBSERVE by **more than 5% or 1.0 ms**, whichever threshold is hit first, in the target load scenes. Report absolute and relative differences, including variance, not just a percentage.
- [ ] No unbounded per-entity path requests, retries, caches, memories, snapshots, listeners, or worker queues. Maximum one outstanding navigation job/entity; cancel and release on removal or supersession.
- [ ] Run a **60-minute dedicated-server soak** under a documented sustained cohort and repeat spawn/despawn and chunk load/unload cycles; check for upward-trending retained state, exceptions, stalled agents, and catastrophic TPS loss.
- [ ] If async navigation exists, prove worker isolation, immutable snapshots, stale-result rejection and shutdown behavior. If async is not implemented, mark it `NOT APPLICABLE` with reason.
- [ ] A deliberately overloaded server degrades safely (defers nonessential work or falls back) and never bypasses entity health/physics/status ticks to save CPU.

### Gate A1-04 — Compatibility and reversibility (mandatory)

- [ ] Dedicated server boots and serves an actual client; singleplayer/integrated server starts and plays. Capture both logs and screenshots/video or a written manual validation record where appropriate.
- [ ] `OBSERVE` and feature-disabled modes preserve vanilla navigation and targeting; toggling off does not leave stale goals, routes or repeated controller injections.
- [ ] Brain-controlled vanilla entities, bosses, flying/swimming/climbing specialists, and unknown modded creatures are **not overwritten** without separate certified adapters.
- [ ] A1 compatibility matrix includes: bare NeoForge; BMAI only; BMAI disabled; at least one common pathfinding optimizer; representative entity-behavior/animation mod when available; absent optional dependencies. List exact tested versions and PASS/FAIL/NOT RUN honestly.
- [ ] If another navigation mod claims the same entity, BMAI either negotiates proven ownership or disables its enhancement with an actionable diagnostic. Never silently dual-drive the entity.
- [ ] Normal save/reload, dimension travel, death, unload/reload, teleport, goal replacement, server stop/restart and config reload do not strand entities or corrupt stored state.
- [ ] Removing or disabling BMAI from an appropriately backed-up test world does not crash the world or leave mandatory registry dependencies. Document backup and recovery procedure; test a removal scenario.
- [ ] When Sable/Aeronautics is absent, startup and ordinary pathfinding work. **Cross-sub-level navigation is not an A1 gate.**

### Gate A1-05 — Reliability and operational safety (mandatory)

- [ ] Zero reproducible crashes, entity duplication, world corruption, invalid forced chunk loads, unauthorized teleportation or permanent AI deadlock in the required test matrix.
- [ ] Each enhanced navigation result is checked for entity identity, dimension/frame, generation, path validity and freshness before application.
- [ ] World reads and mutations occur on appropriate game threads; background workers never use live entity or Level references.
- [ ] Invalid config is rejected with a useful error; valid previous config stays active; changes are applied at a safe boundary.
- [ ] Commands/debug endpoints enforce server-side permission, input size, rate limits and target eligibility; sensitive diagnostics do not expose private state to unauthorized users.
- [ ] Every fallback has bounded frequency and a diagnostic reason. A recoverable planning failure does not create an infinite exception loop.

### Gate A1-06 — Tests, CI and packaging (mandatory)

- [ ] Clean checkout → dependency resolution → `clean build`, `test`, Checkstyle, JaCoCo verification and in-game GameTests all PASS on the pinned Java 21 toolchain.
- [ ] **100% MC-free deterministic domain line and branch coverage** (not a claim of 100% total-mod coverage). Minecraft integration behavior is independently covered by GameTests and runtime tests. Publish actual coverage reports.
- [ ] CI workflow runs required unattended gates and uploads failure evidence; required jobs are green without blanket skips or ignored errors.
- [ ] A versioned `-sources`/source archive (if distributed) contains complete Gradle wrapper and sources; distributable JAR and hash are recorded. Build from the release tag reproduces the expected artifact under documented reproducibility constraints.
- [ ] README, configuration guide, setup/upgrade instructions, cohort support list, known incompatibilities, rollback instructions, changelog, notices and approved license are present. Do not invent license clearance for third-party code.
- [ ] Release notes distinguish **verified implemented features** from planned A2/A3/A4/A5/B capabilities.

### Gate A1-07 — Director sign-off (mandatory)

- [ ] Codex produces the complete evidence package in §7 and reports `A1 CANDIDATE — AWAITING DIRECTOR APPROVAL`.
- [ ] Director explicitly accepts measured tradeoffs, known limitations, scope and version pinning.
- [ ] Only then may Codex report `A1 DONE / RELEASE APPROVED`. No publish/tag/push unless separately authorized.

---

## 5. Non-blocking future gates (NOT part of A1 DONE)

| Future milestone | Separate objective that must later earn its own tests |
|---|---|
| A2 — Perception | Sight, hearing sources, target memory and decay are bounded, line-of-sight correct, gameplay-fair, measurable, and entity-profile controlled. |
| A3 — Tactics | Skeleton positioning, line-of-fire selection, retreat/advance and safe target rules outperform baseline without altering base damage or cheating. |
| A4 — Coordination | Squad awareness and task assignment improve group outcomes under a strict CPU/memory budget, without accidental cross-team sharing. |
| A5 — Dynamic worlds | Station keeping, local/global coordinate transforms, moving-platform patrols and cross-frame combat each independently proven on pinned Sable/Aeronautics versions. |
| Track B — Living AI | Bounded persistent motives/memory/learning/social autonomy with their own safety, determinism, migration and gameplay acceptance contract. |

**Rule:** Features with `future`, `planned`, `experimental`, `prototype` or `unsupported` status must not appear as shipped features in release notes. A later feature can be implemented experimentally without moving its entire milestone to `DONE`.

---

## 6. Automatic NOT DONE conditions

Any of these blocks release regardless of demos or build success:

- A compile succeeds but GameTests or live Minecraft behavioral checks fail, are unrun, or lack logs.
- Navigation works only by overriding all mobs, clearing selectors indiscriminately, silently displacing original owners, or mutating world state off-thread.
- Measured A1 behavior is not better than vanilla on any frozen challenge scenario.
- The implementation reaches benchmark scores by modifying combat stats, hidden target sensing, mobs' move speed, world geometry, or benchmark definitions without approval.
- The AI can loop indefinitely on failures, load chunks unexpectedly, teleport through obstacles, lose goal ownership, or leave orphaned background tasks.
- The release has reproducible crashes, entity duplication, world corruption, or systemic performance regressions beyond approved budgets.
- Required 100% MC-independent coverage is bypassed by excluding tested modules or writing no-op tests.
- Unsupported modded mobs are globally rewritten without adapters; optional mod removal causes classloading failure.
- CI is red, any required gate is skipped, or artifacts are missing; errors are hidden behind “works on my machine.”
- A project milestone is unilaterally declared accepted by Codex without director sign-off.

A genuine technical limitation is an engineering **blocker** or a candidate for a director-approved change request, never a reason to falsely mark PASS.

---

## 7. Required evidence package and Codex report format

Store under `docs/release-evidence/<candidate-version>/` (or link immutable CI artifacts) without committing giant game logs into the source tree unnecessarily:

```text
docs/release-evidence/<candidate-version>/
  manifest.md                     # exact commit, tag candidate, hashes, pins
  gate-status.md                  # every gate: PASS/FAIL/BLOCKED/NOT RUN/N/A
  build-and-ci.md                 # commands, exit codes, CI runs and artifacts
  test-results.md                 # counts, scenarios, passed/failed/skipped
  compatibility-matrix.md        # exact mods/versions and observed outcomes
  benchmark-summary.md           # baseline/observe/enhanced comparisons
  benchmark-data/                # raw machine-readable samples
  profiling/                     # profiler exports / hardware / JVM settings
  runtime/                       # server, client, reload and soak evidence
  risks-and-limitations.md        # known defects, exclusions, rollback
  release-decision.md             # pending / approved / rejected by director
```

Use this gate report template **verbatim for all mandatory rows**:

```markdown
| Gate | Status | Evidence link | Blocker / deviation |
|---|---|---|---|
| P0-01 clean build and pinned wrapper | NOT RUN | — | ... |
| P0-02 OBSERVE truly non-invasive | NOT RUN | — | ... |
| P0-03 source hook/ownership audit | NOT RUN | — | ... |
| P0-04 baseline evidence | NOT RUN | — | ... |
| P0-05 GameTest + dedicated/integrated boot | NOT RUN | — | ... |
| A1-01 functional navigation | NOT RUN | — | ... |
| A1-02 measured improvement | NOT RUN | — | ... |
| A1-03 performance and soak | NOT RUN | — | ... |
| A1-04 compatibility and fallback | NOT RUN | — | ... |
| A1-05 reliability/security | NOT RUN | — | ... |
| A1-06 tests, CI, packaging | NOT RUN | — | ... |
| A1-07 director approval | NOT RUN | — | ... |
```

For *each* gate, include: exact evidence path; command and exit status where applicable; test count/seed; measured result; limitation and owner. Do not fill evidence with a claim and no file/log supporting it. Report observed test environment limitations honestly.

### Final reporting language

- **If P0 fully passed but is waiting for acceptance:** `P0 CANDIDATE — AWAITING DIRECTOR APPROVAL`.
- **If required checks cannot run:** `P0 BLOCKED` or `A1 BLOCKED`, with exact missing prerequisites and commands to reproduce locally.
- **If a gate failed:** `P0 FAILED` or `A1 FAILED`, with the smallest actionable repair and regression test.
- **If all A1 evidence is green but sign-off pending:** `A1 CANDIDATE — AWAITING DIRECTOR APPROVAL`.
- **Only after explicit acceptance:** `A1 DONE / RELEASE APPROVED`.

**No work is considered done by assertion. It is done by reproducible evidence and approval.**

---

## 8. Release acceptance record (to complete later)

```markdown
Project: Better Minecraft AI
Milestone: P0 / A1
Candidate version:
Commit SHA:
Minecraft / NeoForge / Java versions:
Hardware / server settings:
Evidence folder / CI URL:
Functional result:
Baseline comparison:
Performance result:
Compatibility result:
Required gates: PASS ___ / FAIL ___ / BLOCKED ___ / NOT RUN ___
Known accepted limitations:
Director decision: PENDING / APPROVED / REJECTED
Director approval date:
Release publication separately authorized? NO / YES
```

**Document preparation is not implementation:** this document defines the testable finishing line. It does not assert that any code, test, GameTest, benchmark, server smoke run or release has been completed.

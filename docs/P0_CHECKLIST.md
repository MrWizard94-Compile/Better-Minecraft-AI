# Phase P0 approval checklist

This checklist mirrors the P0 contract in `Codex_P0_Kickoff_Better_Minecraft_AI.md` and `Better_Minecraft_AI_Definition_of_Done.md`. A checked item means evidence exists in this working tree; it does not substitute for the mandatory gate table or director approval.

## Repository and toolchain

- [x] Repository initialized on `main` without overwriting the three supplied blueprint documents.
- [x] `origin` is exactly `https://github.com/MrWizard94-Compile/Better-Minecraft-AI.git`; the remote was initially empty and now contains the authorized `main` history.
- [x] Complete wrapper and Java 21 settings committed and pushed.
- [x] Windows wrapper launches.
- [x] Unix wrapper script retained with Git mode `100755`; launch verification is recorded in the evidence package.
- [x] Exact Gradle, ModDevGradle, Minecraft, NeoForge, mappings, Java, Checkstyle, JaCoCo, JUnit, and nullness pins documented.
- [x] Dependency locks and SHA-256 verification metadata generated.
- [x] `clean build` configured to fail on compiler warnings, Checkstyle findings, test failures, and domain coverage regression.

## P0 implementation

- [x] OBSERVE service is server-side, read-only, and deferred beyond unsafe join timing.
- [x] No P0 production code changes goals, targets, movement controls, navigation, attributes, aggression, spawn rules, or world state.
- [x] Entity observation state is released on leave and all state is cleared on server stop.
- [x] Configuration exposes only implemented behavior and has a documented false/off rollback.
- [x] MC-free deterministic domain code is JSpecify `@NullMarked` and has 100% line/branch coverage.
- [x] No global overwrite, blanket goal clearing, whole-tick cancellation, forced chunk loading, or background live-world access.
- [x] No required client feature and no AzureCortex, SmartBrainLib, Sable, GOAP, LLM, or async pathfinder dependency.

## Ownership and architecture proof

- [x] Exact 1.21.1/21.1.256 source audited for `Mob`, selectors/goals, navigation variants, pathfinder/evaluators, Brain/behavior/sensor, and relevant NeoForge events.
- [x] One real vanilla zombie acquires a single vanilla-arbitrated MOVE lease in a test-only GameTest.
- [x] The proof retains the original navigation object and never clears unknown goals.
- [x] Exact-object removal calls stop once, clears the route, restores the original goal set, and preserves navigation identity.
- [x] Acquisition, conflict refusal, lifecycle timing, and fallback design documented for the future C0 adapter.
- [x] ADR compares native hooks, AzureCortex, SmartBrainLib, and ARR Sable Pathfinder; no external framework is added.

## Executed validation

- [x] Eleven MC-free unit tests pass.
- [x] Checkstyle main/test pass with zero findings.
- [x] JaCoCo verifies 100% line and branch coverage for the MC-free domain package.
- [x] Seven GameTests pass with OBSERVE forced off.
- [x] Seven GameTests pass with OBSERVE enabled.
- [x] Dedicated server reaches ready state on loopback and shuts down through `stop`, saving all dimensions.
- [x] Integrated client loads a real local player and ready integrated server for 100 active ticks, then shuts down normally.
- [x] Project-owned runtime errors/fatals are absent; inherited development-runtime warnings are classified and unsuppressed.

## Baseline and evidence

- [x] Same scene, seed, entity count, warmup, timeout, JVM, host, and metrics used in both arms.
- [x] Five raw samples per arm retained in CSV.
- [x] Analyzer validates schema, matching, outcome completeness, exceptions, and control isolation.
- [x] Scope and measurement limitations prevent P0 data from being marketed as an improvement or A1 performance result.
- [x] README, config example, build environment, hook audit, ADR, baseline, warning ledger, notices, changelog, CI workflow, and evidence-package documents present.
- [x] GitHub Actions observed green on Windows and native Ubuntu for the accepted candidate, including both paired GameTest arms and the required-gates job ([run 38157185309](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38157185309)).
- [x] Director accepted the P0 candidate on 2026-10-11 with the exact statement `I accept P0`.

## Stop condition

P0 is accepted and complete. The next smallest P1 task, if separately authorized, is the MC-free profile/mode transition model and C0 allow-list contract—still without shipping enhanced navigation. No P1 work is part of this acceptance record.

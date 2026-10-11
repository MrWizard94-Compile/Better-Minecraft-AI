# Slice report — P1.1 profile, mode, and runtime core

**Goal:** Establish the deterministic dependencies required before Minecraft-side ownership can be productionized.

**Design deviation from blueprint:** None. The implementation follows the single-project, pure-domain-first architecture and exact C0 allow-list.

**Performance:** NOT MEASURED. The resolver is bounded constant work plus a short immutable trace; the registry is bounded by attached loaded entities. Runtime integration does not yet call either hot path.

## Implemented behavior

- Canonical `namespace:path` entity identifiers are validated at the MC-free boundary.
- Hard safety exclusions and administrator deny rules precede configuration requests; multi-exclusion provenance is deterministically ordered.
- Per-entity override precedes tag/data mode, which precedes the global conservative mode.
- Only `minecraft:zombie` may resolve ENHANCED; every other ENHANCED request records its provenance and fails closed to OBSERVE.
- Mode requests expose exact acquire/release actions and cannot overlap an incomplete transition.
- Fallback records a stable reason and exposes an exact restoration action; cleanup remains blocked until integration confirms vanilla ownership.
- UUID runtimes hold immutable profiles and no Minecraft entity/level reference.
- Runtimes cannot start with an unleased ENHANCED profile; duplicate attach and premature removal/clear are rejected; cleanup requires restored vanilla ownership.

## Commands and results

```text
.\gradlew.bat --no-daemon --warning-mode fail test checkstyleMain checkstyleTest jacocoTestCoverageVerification jacocoTestReport --rerun-tasks
first registry run: exit 1 — one unused test import reported by Checkstyle
root-cause correction: removed the unused import
final run: exit 0 — BUILD SUCCESSFUL, 10/10 requested tasks executed
.\gradlew.bat --no-daemon --warning-mode fail clean build
final source/evidence gate: exit 0 — BUILD SUCCESSFUL, 13/13 tasks executed
```

Final test count: 35 tests, 0 failures, 0 errors, 0 skipped. The MC-free domain JaCoCo CSV reports zero missed lines and zero missed branches for every included class. Checkstyle permits zero errors/warnings and reported none on the final run.

## Limits and next action

This slice does not modify live Minecraft AI. P1.2 must translate exact `ResourceLocation`/entity facts into these contracts, configure an OBSERVE-default policy, and prove attachment/cleanup through GameTests before P1.3 adds any production lease.

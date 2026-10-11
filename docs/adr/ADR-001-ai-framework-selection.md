# ADR-001: Phase P0/P1 AI integration foundation

- Status: Accepted for P0; director accepted P0 on 2026-10-11
- Date: 2026-10-09
- Decision scope: P0 research and the smallest C0 ownership foundation for P1
- Supersedes: none

## Context

Better Minecraft AI must eventually enhance an ordinary vanilla zombie without globally replacing entity AI, dual-driving navigation, or imposing an unnecessary runtime framework. Phase P0 first needs a reversible ownership mechanism and a baseline. The blueprint requires comparison of native hooks, AzureCortex, SmartBrainLib, and Sable Pathfinder before adding any required dependency.

Research was pinned to the following public source states on 2026-10-09:

| Subject | Audited state | License observed | Relevant shape |
|---|---|---|---|
| Native Minecraft / NeoForge | Minecraft 1.21.1, NeoForge 21.1.256 sources JAR SHA-256 `428CB928348747DE535D8CC59B134C04CA6DB6B55B6AF6BE2477CD966721E800` | Platform terms apply | Existing `GoalSelector`, `Goal.Flag.MOVE`, and per-mob `PathNavigation` lifecycle |
| AzureCortex | [`d350a61f7878e1c1555d06356d9920668cfbba5a`](https://github.com/AzureDoom/AzureCortex/tree/d350a61f7878e1c1555d06356d9920668cfbba5a), version 0.1.6; Minecraft 1.21.1; NeoForge 21.1.224 | [MIT](https://github.com/AzureDoom/AzureCortex/blob/d350a61f7878e1c1555d06356d9920668cfbba5a/LICENSE) | Framework for behavior trees, GOAP, blackboards, interruptible actions, sensing, and custom navigation; example entities demonstrate explicit adoption |
| SmartBrainLib | 1.21 branch [`1e091c69477f49972d8f434a595a3eb7d115346b`](https://github.com/tslat/SmartBrainLib/tree/1e091c69477f49972d8f434a595a3eb7d115346b), version 1.16.11; Minecraft 1.21.1; NeoForge 21.1.191 | [MPL-2.0](https://github.com/tslat/SmartBrainLib/blob/1e091c69477f49972d8f434a595a3eb7d115346b/LICENSE) | Library for Minecraft's Brain system; normal adoption is entity/brain integration rather than transparent retrofit of arbitrary vanilla goal-selector mobs |
| Sable Pathfinder | [`9333dc924fb0b3a0a826eed1c48d62770a3612f6`](https://github.com/B34tingH34rt/sable_pathfinder/tree/9333dc924fb0b3a0a826eed1c48d62770a3612f6); Modrinth release 1.4.0 for NeoForge 1.21.1 | [All Rights Reserved](https://modrinth.com/mod/sable-pathfinder) | Sable sub-level navigation implementation and a future interoperability/ownership concern |

The AzureCortex and SmartBrainLib pins target their Minecraft 1.21.1 branches, not their unrelated newest-Minecraft branches. Sable Pathfinder's public repository metadata/source version and its published 1.4.0 artifact metadata were not treated as interchangeable; no code was copied.

## Decision drivers

1. Safe transparent ownership of one existing vanilla zombie, with exact rollback.
2. No global entity rewrite and no behavior change in OBSERVE.
3. Small runtime and classloading surface for a foundation mod.
4. Exact compatibility with the pinned NeoForge/Minecraft lifecycle.
5. Ability to measure vanilla before selecting a replacement algorithm.
6. Clear licensing and the option to keep external frameworks optional later.
7. Long-term adapter/API potential without prematurely adopting GOAP or a universal Brain rewrite.

## Options considered

### Native hooks with a BMAI thin ownership adapter

Use the existing vanilla selector and navigator. A per-entity BMAI goal declares `MOVE`; vanilla performs preemption and calls lifecycle methods. The adapter retains its exact goal object, submits through the existing `PathNavigation`, and removes itself exactly on fallback.

Advantages:

- directly matches the ordinary zombie's current GoalSelector architecture;
- smallest ownership and compatibility surface;
- no required library or optional-mod classloading risk;
- keeps vanilla target/combat/lifecycle behaviors intact;
- permits direct A/B comparison before any algorithm choice; and
- proven in P0 by a real zombie GameTest.

Costs:

- BMAI owns adapter design, conflict detection, diagnostics, and tests;
- each specialist cohort needs its own certified adapter; and
- future advanced planning features would require new measured ADRs.

### AzureCortex as a required runtime

AzureCortex is a coherent MIT-licensed framework with behavior trees, GOAP, sensing, blackboards, actions, and custom navigation. Its current 1.21.1 codebase is a credible research source for explicit custom entities.

It is not selected for C0 because P0 needs narrow ownership of an already-existing vanilla zombie, not adoption of a full alternative agent stack. A required dependency would expand behavior, navigation, lifecycle, maintenance, and compatibility surfaces before a measured need exists. It would also make “framework installed” easy to confuse with “vanilla zombie safely retrofitted,” which it does not prove by itself.

Reconsideration evidence: an isolated adapter experiment must show a materially safer or more capable vanilla-entity lifecycle than the native lease, with lower or acceptable tick/memory cost, clean removal, exact dependency/license packaging, and no regression in the C0 matrix.

### SmartBrainLib as a required runtime

SmartBrainLib is a mature MPL-2.0 library that makes Minecraft's Brain behavior/sensor system easier to build. Its 1.21 branch pins Minecraft 1.21.1, Java 21, Parchment 2024.11.17, and NeoForge 21.1.191.

It is not selected for C0 because an ordinary vanilla zombie uses GoalSelector/navigation ownership, while SmartBrainLib's value is strongest for entities deliberately integrated with its Brain-owner lifecycle. Retrofitting an existing vanilla class would still require a separate injection/adapter boundary and lifecycle proof; the dependency does not remove the hard part. It would also blur BMAI's explicit exclusion of Brain-specific entities in the first cohort.

Reconsideration evidence: a later Brain-native cohort or external adapter demonstrates a clear API benefit, along with MPL notice/source obligations, optional-dependency isolation, performance measurements, and removal tests.

### Sable Pathfinder as implementation input or dependency

Sable Pathfinder is specifically aimed at movement across Sable sub-level boundaries. It is relevant to future A5 interoperability because it may claim or refresh paths in coordinate frames that vanilla navigation does not understand.

It is rejected as an implementation source and P0/P1 dependency. The published project is All Rights Reserved, so BMAI will not copy its code. Sable itself is absent from the P0 environment and cross-sub-level navigation is not an A1 gate. If both mods are present later, BMAI must negotiate or disable its enhancement rather than dual-drive a mob.

Reconsideration evidence: explicit permission or a documented public API, exact pinned Sable/Aeronautics versions, coordinate-frame validation, lifecycle/conflict tests, and a separate A5 decision.

## Decision

Select **native Minecraft/NeoForge hooks plus a BMAI-owned thin adapter** for P0 research and the first P1 C0 lifecycle. Add no AI framework and no Sable component as a required or optional runtime dependency in P0.

The selected ownership boundary is vanilla `GoalSelector` arbitration over `Goal.Flag.MOVE`, retaining the zombie's existing `GroundPathNavigation`. The detailed acquisition/fallback invariants and source references are in `docs/compat/HOOK_AUDIT.md`.

This decision chooses a lifecycle boundary, not a new path algorithm. Vanilla pathfinding remains the baseline. Any custom A*, async computation, GOAP, behavior tree, or Brain integration requires benchmark evidence and another ADR.

## Consequences

- P0 ships observation only; the adapter exists solely as an in-game proof.
- P1 may productionize the lease for an allow-listed ordinary zombie only after its missing lifecycle/config/conflict tests pass.
- Unsupported and unknown entities stay vanilla by default.
- External framework changes do not create a forced upgrade cadence for the foundation.
- BMAI must maintain high-quality exact-version hook audits at each Minecraft/NeoForge update.
- AzureCortex and SmartBrainLib remain legitimate future integration candidates when a concrete cohort proves their benefit.
- Sable Pathfinder remains an interoperability test subject; ARR code is never copied.

## Validation

The decision is supported by:

- seven passing GameTests in both vanilla-control and OBSERVE runs;
- a real vanilla-zombie MOVE lease and exact-object rollback test;
- identity assertions proving the existing navigation and selectors are not replaced by OBSERVE;
- matched baseline data showing the same success, duration, path nodes, and stall count in every paired P0 trial; and
- dependency output confirming no AzureCortex, SmartBrainLib, Sable, or Sable Pathfinder dependency is resolved.

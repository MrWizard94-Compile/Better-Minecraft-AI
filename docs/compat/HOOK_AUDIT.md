# Minecraft 1.21.1 / NeoForge 21.1.256 AI hook audit

## Audit identity

| Item | Audited value |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.256 |
| Mappings | Parchment 2024.11.17 for Minecraft 1.21.1 |
| Source artifact | `net.neoforged:neoforge:21.1.256:sources` |
| Source JAR SHA-256 | `428CB928348747DE535D8CC59B134C04CA6DB6B55B6AF6BE2477CD966721E800` |
| Audit date | 2026-10-09 |

Line references below are to the audited mapped source artifact, not to a different Minecraft or NeoForge version. A dependency update invalidates the line numbers and requires this audit plus the ownership GameTest to be rerun.

## Exact implementation points

### Mob lifecycle and tick order

| Source | Lines | Finding and consequence |
|---|---:|---|
| `net/minecraft/world/entity/Mob.java` | 113–114 | `goalSelector` and `targetSelector` are stable, final selector objects. P0 observes their identities but never mutates them outside the isolated ownership GameTest. |
| `Mob.java` | 138–153 | Constructor creates selectors, movement/look/jump controls and navigation before calling `registerGoals()` on the logical server. A retrofit must attach after construction and must not replace partially initialized state. |
| `Mob.java` | 163–165 | Default `createNavigation` returns `GroundPathNavigation`; specialist mobs override this. A generic replacement is unsafe. |
| `Mob.java` | 773–805 | Server AI order is sensing → target selector → goal selector → `navigation.tick()` → `customServerAiStep()` → controls. A generic entity-tick listener cannot pretend to own navigation without participating in selector arbitration. |

`Mob.serverAiStep()` is final. Better Minecraft AI therefore will not cancel a whole entity tick or overwrite this method. P1's C0 work must request movement through a scoped goal that declares the `MOVE` control flag, allowing the existing selector to perform normal preemption before the existing navigation and movement controls tick.

### Goal arbitration

| Source | Lines | Finding and consequence |
|---|---:|---|
| `net/minecraft/world/entity/ai/goal/Goal.java` | 54–59 | `Goal.Flag` includes the vanilla control domains; a movement owner must declare `MOVE`. |
| `GoalSelector.java` | 38–43 | Goals are registered with an explicit priority; lower numeric values have higher priority. |
| `GoalSelector.java` | 50–60 | Removal is identity-based and requires the exact goal object. Removing a running object invokes its `stop()` before removal. BMAI must retain its own goal instance per entity. |
| `GoalSelector.java` | 73–110 | Arbitration checks every claimed flag, interrupts only replaceable owners, updates the locked-flag map, then invokes the new goal's `start()`. This is the safe ownership boundary. |
| `GoalSelector.java` | 116–127 | Only running goals are ticked. P1 must not separately drive the same goal from an event callback. |
| `WrappedGoal.java` | 23–25 | Replacement requires the current owner to be interruptible and the new goal to have strictly higher priority. Equal-priority ownership must be treated as a conflict, not overwritten. |
| `WrappedGoal.java` | 43–55 | Start/stop bookkeeping is idempotent inside the wrapper. Adapter cleanup still must be exact-object and main-thread-only. |

The source itself documents exact-object removal. Blanket `removeAllGoals`, selector replacement, reflection, and blind field writes are rejected.

### Navigation and node evaluation

| Source | Lines | Finding and consequence |
|---|---:|---|
| `net/minecraft/world/entity/ai/navigation/PathNavigation.java` | 108–177 | `createPath` validates target and navigation readiness, creates a bounded `PathNavigationRegion`, calls the pathfinder, and updates target/stuck state on the server thread. |
| `PathNavigation.java` | 181–220 | `moveTo` is the supported route-application boundary. It rejects null/done paths and retains the active route in the existing navigation object. |
| `PathNavigation.java` | 229–257 | `tick` progresses an existing route and writes the next wanted position to the mob's `MoveControl`. Two independent callers would dual-drive that control. |
| `PathNavigation.java` | 307–347 | Vanilla already provides bounded stuck and node-timeout stopping. P1 must preserve these semantics or justify any bounded alternative with tests. |
| `PathNavigation.java` | 356–365 | `isDone` / `isInProgress` expose route state; `stop()` clears the path. Adapter `stop()` must call this before relinquishing MOVE ownership. |
| `GroundPathNavigation.java` | 19, 27, 34 | Ground navigation selects a `WalkNodeEvaluator` and defines its own readiness rule. This is the only navigator eligible for the first ordinary-zombie cohort. |
| `FlyingPathNavigation.java` | 14, 20, 35 | Flying mobs use a different evaluator/readiness contract and are excluded. |
| `WaterBoundPathNavigation.java` | 11, 19, 26 | Water mobs use a different evaluator/readiness contract and are excluded. |
| `AmphibiousPathNavigation.java` | 10, 16 | Amphibious mobs use a specialist pathfinder and are excluded. |
| `net/minecraft/world/level/pathfinder/PathFinder.java` | 20, 37–58 | Vanilla path search consumes a bounded region, mob, target set, range, accuracy, and visit multiplier. P0 does not replace it. |
| `NodeEvaluator.java` | 26–35, 51–53 | Evaluators are prepared with live navigation-region/mob state, torn down, and provide typed start/target nodes. They are not worker-safe snapshot APIs. |
| `WalkNodeEvaluator.java` | 31, 53, 121 | Ground-node start and neighbor expansion are evaluator-specific. A future replacement cannot assume all navigation is two-dimensional ground walking. |

P0 retains the vanilla `PathNavigation` object. No worker thread reads `Level`, `Mob`, `PathNavigationRegion`, or `NodeEvaluator`. Any future async design requires immutable snapshots, request generations, stale-result rejection, main-thread validation, and its own phase gate.

### Brain/behavior/sensor boundary

| Source | Lines | Finding and consequence |
|---|---:|---|
| `net/minecraft/world/entity/ai/Brain.java` | 45, 284–327, 402–409 | Brain activities and sensors have their own lifecycle and tick pipeline. GoalSelector injection is not a universal Brain takeover. |
| `net/minecraft/world/entity/ai/behavior/Behavior.java` | 10, 38–71 | Brain behaviors own explicit start, tick/stop, and stop transitions. |
| `net/minecraft/world/entity/ai/sensing/Sensor.java` | 10, 39 | Sensors tick through the Brain contract and can establish memories used by behaviors. |

P1 eligibility must be an allow-list for the ordinary vanilla zombie C0 profile. Brain-oriented mobs, bosses, flying/swimming/climbing specialists, and unknown modded entity classes remain OBSERVE/unchanged until a separate adapter proves their native lifecycle.

### NeoForge lifecycle events

| Source | Lines | Finding and consequence |
|---|---:|---|
| `net/neoforged/neoforge/event/entity/EntityJoinLevelEvent.java` | 17–30 | Fires on both logical sides and may occur before the underlying chunk reaches `FULL`; its Javadoc warns that immediate world interaction can deadlock chunk loading. P0 records only the UUID and defers attachment. |
| `net/neoforged/neoforge/event/tick/EntityTickEvent.java` | 23–51 | `Pre` can cancel the entire entity tick and suppress `Post`; BMAI does neither. P0 samples already-computed state in server-side `Post`. |
| `net/neoforged/neoforge/event/entity/EntityLeaveLevelEvent.java` | 22 | P0 removes pending and active state on leave. P1 must also release its exact goal/lease before discarding state. |
| `net/neoforged/neoforge/event/server/ServerStoppingEvent.java` | 15 | P0 emits one bounded summary and clears all process-held entity state. |

## Vanilla zombie ownership-transfer design

The first production-eligible mechanism is a **thin goal lease over the existing vanilla `GroundPathNavigation`**, not navigation-object replacement.

### Eligibility preconditions for P1

All checks occur on the logical server thread at a safe post-join boundary:

1. The entity is an explicitly supported ordinary vanilla `Zombie`; no subclass, boss, Brain-specific profile, passenger/rider special case, specialist navigator, or unknown modded type is accepted by default.
2. The entity is alive, attached to a server level, in a loaded tick context, and owns the expected `GroundPathNavigation` instance.
3. The configured profile explicitly enables C0 enhancement; global OBSERVE remains the default.
4. The selector does not contain a known foreign BMAI lease and conflict diagnostics reveal no unnegotiated navigation owner.
5. BMAI retains the exact adapter goal object and original navigation identity in a UUID-scoped lifecycle record. No selector snapshot is used as authority to rewrite other mods' registrations.

### Acquisition

1. Create one BMAI adapter goal that declares only the minimum required `MOVE` flag.
2. Register it once with an explicit priority. Let `GoalSelector.tick()` arbitrate; never call adapter `start()` manually.
3. Treat the lease as acquired only after the wrapped BMAI goal is running and is the sole running goal whose flags include `MOVE`.
4. In `start()`, submit a validated route through the entity's existing `PathNavigation.moveTo(...)`. If vanilla rejects the route, stop and relinquish without retrying every tick.
5. While running, the adapter is the only BMAI component allowed to submit movement commands. Target selection, combat, attributes, spawning, fire/daylight behavior, and other lifecycle rules stay vanilla.

The selector's normal priority/interruptibility rules stop a replaceable prior MOVE goal before starting the adapter. A non-interruptible or equal/higher-priority owner prevents acquisition; BMAI reports the conflict and remains/falls back to vanilla. It never fights by re-submitting a path each event tick.

### Release and fallback

Release is triggered by disable/config transition, ineligibility, target/goal generation change, dimension/level leave, death/removal, navigation identity change, conflict detection, path failure, or server stop.

1. Mark the adapter ineligible so `canContinueToUse()` is false.
2. On the main server thread, remove **that exact object** with `goalSelector.removeGoal(adapter)`.
3. Adapter `stop()` invokes `PathNavigation.stop()` and clears BMAI request/route state exactly once.
4. Remove the lifecycle record and assert no BMAI goal remains and no BMAI route/request survives.
5. Do not reconstruct or restore other goals. Because they were never deleted, the vanilla selector naturally re-arbitrates them on its next normal tick.

If any invariant cannot be proved, the safe result is OBSERVE with an actionable diagnostic—not partial ownership.

## Executed P0 proof

`BetterMinecraftAiGameTests.zombieMoveOwnershipIsExclusiveAndReversible` exercises the design's narrow lifecycle against a real vanilla zombie:

- captures the original goal set and navigation object;
- registers a test-only priority-0 MOVE goal after entity initialization;
- proves the wrapper is running, exactly one MOVE goal is running, start occurred once, and vanilla accepted the path;
- proves the navigation object was not replaced;
- disables and removes the exact object;
- proves stop occurred once, the path is done, the original goal set is restored, and navigation identity is unchanged.

The test passed in both vanilla-control and OBSERVE GameTest runs. The separate OBSERVE test proves controller identities remain unchanged when enabled and that no observer attaches when forced off. Evidence is in `docs/release-evidence/p0/runtime/`.

This is intentionally a P0 safety proof, not a shipped enhanced controller: the production P0 entry point registers only `ObservationService`, and the test-only goal class is reachable only through the GameTest holder.

## Rejected P0 mechanisms

- Global mixin `@Overwrite` of `Mob.serverAiStep`, `PathNavigation.tick`, or entity tick.
- Cancelling `EntityTickEvent.Pre` to suppress vanilla lifecycle work.
- Clearing or replacing an entity's whole goal selector.
- Replacing navigation on every entity or assuming `GroundPathNavigation` for specialist mobs.
- Issuing movement from a generic event listener while a GoalSelector owner also runs.
- Forced chunk loading, teleport recovery, or world access from worker threads.
- Copying or binding to All-Rights-Reserved Sable Pathfinder source.

## P1 obligations that remain open

Before the test lease may become a production C0 adapter, P1 must add the allow-listed profile resolver, lifecycle state map, conflict diagnostics, explicit mode transitions, unload/death/dimension/config fallback tests, save/reload coverage, and unknown-mob/boss exclusion tests. It must also freeze A1 challenge scenes before tuning. None of those future gates is claimed by P0.

# Better Minecraft AI
## Production Engineering Blueprint for Codex

**Working title:** Better Minecraft AI (BMAI; *temporary*, not a final public project name)  
**Target:** Minecraft Java Edition 1.21.1 · NeoForge 21.1.x · Java 21  
**Working mod ID:** `better_minecraft_ai` (freeze before first public save/release; changing registry IDs later requires migration)  
**Design owner:** Project director  
**Implementation owner:** Codex, operating under the acceptance gates below  
**Document status:** Implementation specification, **not** a claim of completed code or validated in-game performance  
**Research cutoff:** 2026-10-09  
**Scope:** Release track A, *Better Minecraft AI*, designed for a separate future track B, *Living Minecraft AI*.

> **PRIMARY DIRECTIVE TO CODEX**  
> Build an independent, reliable, high-performance Minecraft AI improvement mod. Do not attempt to replace every entity's AI at once. Preserve existing gameplay contracts, prove behavior with tests, and expand coverage only when evidence supports it. Do not claim successful tests that were not actually executed. Fix implementation defects rather than weakening legitimate assertions.

---

# 1. Executive decision and principles

## 1.1 Project mission

Improve the **competence** of Minecraft entities: movement, route selection, hazard response, pursuit, disengagement, perception, target assessment, combat positioning, and (later) limited cooperation. Improvements must be meaningfully measurable and compatible with modded Minecraft. The engine shall be usable by separate mods, including Buildable Golem Guards, Cageborn, AdaptiveBoss, and other future projects, without taking hard dependencies on any of them.

**Separate three meanings of “better”:**

1. **More capable:** the entity reaches appropriate destinations, avoids obvious mistakes, recovers when stuck, and makes context-sensitive decisions.
2. **More efficient:** the AI consumes an acceptable portion of server tick time, prioritizes urgent work, avoids repetitive expensive searches, and degrades gracefully under load.
3. **More compatible:** vanilla behavior and mod-defined invariants survive enhancement. Unknown or complex entities are not silently rewritten.

### Design rules (non-negotiable)

- **Server-authoritative** decisions and movement; no client-authoritative combat or entity targeting.
- **Fail open to vanilla** behavior when the new system fails, times out, encounters unknown movement semantics, or cannot prove compatibility.
- **Default conservative**, with opt-in enhanced modes and explicit entity-type coverage.
- **Evidence before algorithms:** benchmark vanilla and existing implementations before introducing replacement pathfinding or async computation.
- **No global Mob/Brain/GoalSelector reset.** Do not indiscriminately clear goal selectors, override controllers, or rewrite special mob tasks.
- **Single logical navigation owner:** never run two competing movement systems against the same entity within the same tick.
- **Separation of concerns:** sensors, working memory, goals, planning, navigation, movement execution, and scheduling are independent.
- **Bounded work:** every planner, request queue, cache, sensor, and history has explicit time/size budgets.
- **No unexplained gameplay escalation:** smarter does not automatically mean higher damage, faster movement, extra block-breaking, or perfect information.
- **No forced world loading:** AI may inspect loaded chunks only; routes entering unavailable world sections must wait or fail safely.
- **No premature learned/LLM runtime:** track A is deterministic, inspectable, and entirely offline. Track B is a future optional capability, not a hidden dependency.
- **All new code documented and testable:** no raw Java generics, no silently ignored nullability, reproducible test seeds, and clear compatibility boundaries.

## 1.2 Release tracks

| Track | Intent | Promise |
|---|---|---|
| A0 — Instrumentation | Establish baseline and safe feature hooks | Vanilla remains intact |
| A1 — Navigation | Reliable movement and stuck recovery for supported mobs | Opt-in improvements with rollback |
| A2 — Perception | Correct, bounded sensing and last-known-target memory | No omniscience or combat advantage without configuration |
| A3 — Tactical behavior | Context-sensitive target positioning, pursue/retreat | Entity-specific behavior profiles |
| A4 — Coordination | Small-group awareness and tactical roles | Bounded group state and messaging |
| A5 — Dynamic worlds | Moving platforms and special geometry adapters | Optional Sable/vehicle integration |
| B — Living AI | Needs, long-term memory, motivations, learning, social models | **Not committed for v1** |

**Initial release definition:** A1 plus required A0 observability, configuration, safety, and testing. A2/A3/A4/A5 are subsequent minor releases, not part of an unbounded v1 promise.

## 1.3 Non-goals for v1

No universal replacement of all vanilla/modded mob logic; no creative block-breaking, crafting, mining, or bridge building; no neural networks, LLM calls, persistent personality simulation, automatic taming, economy, or village governance; no forced entity ticking in unloaded chunks; no automatic conversion of other mods' `Brain` schedules; no guaranteed cross-sub-level movement; no client-only mod requirement unless the chosen networking strategy makes it necessary and that is explicitly documented.

---

# 2. Evidence base and dependencies

## 2.1 Verified upstream capabilities

**Minecraft/NeoForge 1.21.1:** Java 21 and the official NeoForge ModDevGradle MDK are suitable. The official MDK's 2026-09-27 snapshot advertises NeoForge `21.1.252`, ModDevGradle `2.0.147`, and Parchment `2024.11.17`; these are **reference pins**, not a claim they are the latest compatible versions when Codex begins. On setup, verify artifact availability, compile a clean MDK, and **commit the exact resolved dependency manifest**.

**Minecraft AI:** `Mob` exposes `goalSelector`, `targetSelector`, `navigation`, `createNavigation(Level)`, and `registerGoals()`. `PathNavigation` exposes path generation/following and status operations, but many existing AI calls originate inside entity-specific implementation. An event listener observing a mob is **not** equivalent to replacing or enhancing all of its internal pathfinding. Do not assume a universal safe public hook exists.

**NeoForge integration:** `EntityJoinLevelEvent` is available but may occur before the chunk reaches `FULL`; never perform world reads requiring a full chunk in that callback. Entity tick events exist on both logical sides; filter to server. Entity data attachments support persistence, but do not automatically sync to clients. GameTest supports reproducible in-world scenarios and the `runGameTestServer` Gradle task.

**Existing relevant software:** AzureCortex (MIT) provides reusable behavior-tree/GOAP/navigation abstractions for NeoForge 1.21.1. SmartBrainLib is MPL-2.0 and provides mod-author-facing AI infrastructure. Sable Pathfinder is a distinct cross-sub-level navigation mod for NeoForge 1.21.1; its distribution is marked ARR, so **study documented behavior and interoperability, not copy its implementation**. These are alternatives and integration subjects, not mandatory dependencies.

## 2.2 Required Phase-0 due diligence

Before writing any replacement or mixin:

1. Clone or generate a clean 1.21.1 MDK and record wrapper, NeoForge, mappings, Java, plugins and hashes.
2. Inspect decompiled/Mojang-mapped exact-version source for `Mob`, `PathNavigation`, `GroundPathNavigation`, `FlyingPathNavigation`, `WaterBoundPathNavigation`, `AmphibiousPathNavigation`, `PathFinder`, `NodeEvaluator` subclasses, `GoalSelector`, `Goal`, `Brain`, representative `Sensor`/`Behavior` classes, and `EntityJoinLevelEvent`/tick events.
3. Document actual insertion and replacement points, their access level, lifecycle and invariants in `docs/compat/HOOK_AUDIT.md`. Avoid guessing method signatures from another Minecraft version.
4. Test AzureCortex and SmartBrainLib in isolated proof-of-concept branches: APIs, license compatibility, maintenance status, supported 1.21.1 coordinates, server-only suitability, whether they enhance **existing** mobs or only custom entities, and conflict surface.
5. Test for conflicts with known pathfinding mods and Sable Pathfinder. Record precise versions, mod IDs, and observed stack traces. Do not bundle or directly depend on a competitor by default.
6. Make an Architecture Decision Record, `ADR-001-ai-framework-selection.md`: select native vanilla extension / own thin adapter / library integration based on **measured** benefit and release maintenance, not novelty.
7. Record unknown/blocked APIs openly. Do not invent integrations, write decompiled source into our repo, or copy ARR-licensed logic.

## 2.3 Dependency policy

| Dependency | Baseline | Policy |
|---|---|---|
| Minecraft | 1.21.1 | Exact target |
| NeoForge | compatible 21.1.x, pinned after successful clean build | Required runtime |
| JDK | 21 | Build/toolchain |
| ModDevGradle | official 1.21.1 MDK-compatible pinned version | Build only |
| Parchment | exact mappings compatible with Minecraft 1.21.1 | Dev only |
| JUnit 5 / AssertJ | choose stable tested versions | Test only |
| Checkstyle | 10.x | Required static validation |
| JaCoCo | stable pinned version | MC-independent modules |
| Sable/Companion | **none** until A5 | Optional integration only |
| AzureCortex / SmartBrainLib | **none** until ADR-001 approved | Evaluate, do not assume |
| Spark profiler | compatible 1.21.1 build | Benchmark/dev environment |

**License gate:** establish the new mod's own license with the director before public distribution. Preserve third-party notices and record licenses/versions in `THIRD_PARTY_NOTICES.md` if incorporating code or binary libraries. No fabricated compatibility guarantees.

## 2.4 Primary technical references

- NeoForge 1.21.1 Getting Started: https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- Official 1.21.1 ModDevGradle MDK: https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle
- Verified MDK Gradle properties: https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/blob/main/gradle.properties
- NeoForge GameTests: https://docs.neoforged.net/docs/1.21.1/misc/gametest/
- NeoForge attachments: https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- NeoForge payload networking: https://docs.neoforged.net/docs/1.21.1/networking/
- NeoForge data maps: https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/
- 1.21.1 `Mob` mappings: https://mappings.dev/1.21.1/net/minecraft/world/entity/Mob.html
- 1.21.1 `PathNavigation` mappings: https://mappings.dev/1.21.1/net/minecraft/world/entity/ai/navigation/PathNavigation.html
- AzureCortex 1.21.1 README: https://github.com/AzureDoom/AzureCortex/blob/1.21.1/README.md
- SmartBrainLib: https://github.com/Tslat/SmartBrainLib
- Sable Pathfinder: https://modrinth.com/mod/sable-pathfinder
- Spark profiling tools: https://spark.lucko.me/

---

# 3. Behavioral specification

## 3.1 Profiles and opt-in behavior

All entity types receive a resolved `BehaviorProfile`; **supported vanilla entities** may use enhanced features, **unknown modded entities** default to compatibility-only. Resolve configuration in this precedence order:

`hard safety exclusions > admin explicit denylist > per-entity override > entity-type tags/datapack data > built-in supported profile > global conservative default`.

`ProfileResolver` must produce a fully validated immutable profile and a provenance trace explaining why it was selected. Explicit entity IDs and tags should use `ResourceLocation` strings, not Java class-name matching as a public configuration interface.

### Three execution modes

- **OBSERVE:** collect metrics only. Do not change active AI. The initial global default and the baseline A0 mode.
- **ASSIST:** enable proven, non-competitive helpers at well-defined safe extension points; preserve vanilla goal owners. Example: limited stuck diagnosis and configurable avoidance *only where it can be safely applied without competing controllers*. Disable individual enhancements independently.
- **ENHANCED:** explicitly supported entity types use selected enhanced navigation/tactics through a verified replacement or ownership path. Must preserve their original lifecycle (despawn, swimming, fire response, combat cooldowns, leashing, riding, spellcasting, knockback, special boss states).

No blanket `FULL` mode on arbitrary mobs in v1. A future advanced mode can be added only after compatibility gates.

## 3.2 Supported entity cohort schedule

| Cohort | Representative entities | v1 behavior |
|---|---|---|
| C0 | Zombie (single land walker) | First enhanced navigation candidate |
| C1 | Husk, Drowned **on land only**, common land-path variants | After behavior-specific validation |
| C2 | Skeleton | Ranged tactical pilot after navigation |
| C3 | Spider | Wall-climb semantics separate test; no implied support from land-walker code |
| C4 | Passive animals | Fleeing and wandering; avoid changing breeding/herd semantics |
| C5 | Villager, Piglin, Warden | Brain-driven: observe only until separate adapter |
| C6 | Ender Dragon, Wither, custom bosses | Excluded unless explicitly integrated and tested |
| C7 | Unknown modded mobs | Observe only by default; integrator opt-in |

**C0 acceptance:** at least one ordinary zombie navigates better in benchmark scenarios without changing attack damage, base movement speed, hostile targeting eligibility, spawn rules, or player aggro range except where separately approved and tested.

## 3.3 Correctness contracts

- **Arrival:** on reachable targets, approach within tolerance and stop instead of oscillating.
- **Unreachable:** emit a bounded failure outcome; never retry identical impossible paths every tick.
- **Hazards:** account for lava, fire, magma, cactus, deep drops, drowning, berry bushes, powder snow, doors, fences, trapdoors and modded collision shapes according to agent capabilities. No entity teleporting to recover.
- **Collision geometry:** path validation uses actual entity dimensions and collision shapes, not only a single-block passability flag.
- **Moving targets:** honor target velocity with capped prediction and periodic verification; do not magically know an unseen target's position.
- **Unexpected state:** damage, knockback, mounting, dimension change, death, unload, teleport, target change, goal interruption and block edits invalidate or reconsider routes.
- **Vanilla constraints:** do not violate tame/owner/team logic, peaceful mode rules, spectator/creative targeting, leash restrictions or special AI flags.
- **Restoration:** disabling an enhancement returns the entity to a coherent vanilla-compatible state without duplicating goals or leaving stale movement orders.

---

# 4. Reference architecture

```text
Minecraft / NeoForge lifecycle
          |
     EntityEligibility
          |
     CompatibilityGate <--- profiles + allow/deny rules + mod presence
          |
     EntityAiRuntime (per eligible entity, lifecycle-scoped)
     |       |         |         |          |         |
  Sensors  Memory   Intent      Planner   Navigator   Metrics
     |       |         |         |          |         |
     +-------+---------+---------+----- Agent scheduler
                                         |
                              Navigation ownership adapter
                                         |
                         Vanilla PathNavigation / verified plugin
                                         |
                                   Server entity tick

Optional later: cross-world-frame navigation provider (Sable)
Public interface: capability-based adapters for golems / third parties
```

## 4.1 Modules/packages

Create **logical packages in a single NeoForge project initially**. Do not force Gradle multi-module split until there is a validated need. Pure algorithm code must compile without Minecraft classes for isolated testing.

```text
src/main/java/<group>/betterai/
  BetterAiMod.java
  bootstrap/                 # registrations and sides
  api/                       # stable externally visible contracts
    AiProvider.java
    EntityAiAdapter.java
    BehaviorProfile.java
    NavigationRequest.java
    NavigationResult.java
    WorldFrame.java
    NavigationFrameProvider.java
    PerceptionEvent.java
  compatibility/
    EligibilityService.java
    ModCompatibilityGate.java
    EntityLifecycleTracker.java
    FallbackController.java
  domain/                    # pure Java logic, no Minecraft imports
    geometry/
    navigation/
      grid/
      search/
      cost/
      routing/
    scheduling/
    decisions/
    memory/
  minecraft/
    snapshot/
    navigation/
    goals/
    brain/
    sensors/
    events/
  runtime/
    AgentRuntime.java
    AgentStateMachine.java
    AiWorkScheduler.java
    AiRequestCoordinator.java
    PathResultApplier.java
  profiles/
    ProfileResolver.java
    ProfileCodec.java
    BuiltinProfiles.java
  diagnostics/
    AiMetrics.java
    DiagnosticCommand.java
    DebugSnapshot.java
  config/
    ClientSettings.java
    CommonSettings.java
    ServerSettings.java
  integration/
    sable/                   # NOT loaded before A5
    golems/                  # compile-time-free API integration preferred
  net/                       # later optional debug UI
src/test/java/               # MC-free unit/property tests
src/gametest/java/           # or MDK-supported registered GameTests
src/main/resources/
  META-INF/neoforge.mods.toml
  data/better_minecraft_ai/
    tags/entity_type/...     # data-driven explicit inclusion/exclusion
    ...
docs/
  architecture/
  compat/
  benchmarks/
  adr/
  releases/
.github/workflows/
```

**Do not copy package paths literally if the repository's group ID differs.** Freeze the group from the actual repo setup. Use Java records only where clear and valid under Java 21.

## 4.2 Interfaces: language-neutral contracts, not drop-in code

The following signatures describe responsibilities, not guaranteed compilable Java. Codex shall first create model/domain types and validate exact NeoForge method signatures in the IDE.

```java
interface AiProvider {
    boolean supports(EntityDescriptor entity, EnvironmentDescriptor context);
    Optional<EntityAiAdapter> createAdapter(EntityHandle entity, BehaviorProfile profile);
}

interface EntityAiAdapter {
    AiMode mode();
    void onAttach();
    void onServerTick(TickContext context);
    void onDetach(DetachReason reason);
    void onProfileReload(BehaviorProfile next);
}

interface NavigationPlanner {
    NavigationResult plan(NavigationRequest request, NavigationSnapshot snapshot,
                          SearchBudget budget);
}

interface NavigationFrameProvider {
    Optional<WorldFrame> resolve(EntityHandle entity);
    FramePoint toWorld(FramePoint local);
    FramePoint toLocal(FramePoint world);
}
```

**Important modeling distinction:** `WorldFrame` identifies `(dimension, frame identity, revision/transform)` and expresses coordinates in a named frame. An `EntityHandle` for runtime use must not be retained in worker jobs; snapshot jobs contain only copied IDs/primitive data. Interface stability begins at `apiVersion=1` only after a published contract test suite.

## 4.3 Core data model

**Agent runtime (ephemeral):** entity UUID and weak/owned lifecycle handle on the server thread, active mode, current intent, currently owning behavior, outstanding request generation, last path status, cooldowns, telemetry counters.

**NavigationRequest (immutable):** entity UUID, dimension ID, source/target immutable coordinates, frame identity, body size, movement capabilities, cost-policy ID/version, loaded-region bounds, world snapshot revision, request generation, expiry tick, deterministic tie-break seed, max-node/time budget.

**NavigationResult:** status (`SUCCESS`, `PARTIAL`, `UNREACHABLE`, `CANCELLED`, `STALE`, `BUDGET_EXCEEDED`, `ERROR`), immutable waypoints, diagnostics, request generation and revision, total cost, expanded nodes, compute duration. `PARTIAL` is **not automatically** navigable; must pass validation and policy.

**PerceptionMemory (ephemeral v1):** last seen position and tick, bounded confidence, optional sound-source position, threat score, cooldown timestamps; never persist an unbounded history or target every loaded entity every tick.

**Persistent agent attachment:** only fields needed to restore externally meaningful state, profile override/consent, capability flags, version marker, and opt-in long-lived assignments. Routine computed paths, live entity references, caches and perception event history are not persisted.

## 4.4 Navigation ownership invariant

At every server tick, for each supported entity, exactly one system is authorized to submit movement commands:

- `VANILLA_OWNED`: our code observes and computes advisory metrics only.
- `ENHANCED_OWNED`: controlled integration adapter has legitimately taken over the chosen navigation behavior; original competing goal is disabled/replaced by a **documented, narrowly-scoped** mechanism.
- `TRANSITIONING`: cancel outstanding work, clear conflicting movement commands, revalidate state and transfer ownership.

Illegal double-ownership is an assertion and a diagnostic report; it must not be hidden by overwriting speed/path repeatedly. The mechanics by which an existing vanilla zombie moves to enhanced ownership are a **Phase-0 proof requirement**, not assumed guaranteed by `EntityTickEvent`.

---

# 5. Navigation engine

## 5.1 Conservative implementation first

Phase A1 step 1 should reuse vanilla navigation where feasible and add measured improvements around **retry policy, path invalidation, destination scoring and stuck recovery**. Do not implement an entire A* replacement as an automatic first step.

**Reconsider only after benchmark:** implement a deterministic, pure-Java A* core against a bounded immutable navigation snapshot, then compare it against vanilla for correctness, path quality, performance, interoperability and memory cost.

### Pathfinder design, if replacement is justified

- Movement capability describes walking, swimming, climbing, flying, step height, jump ability, fall tolerance, door interaction, entity dimensions and navigation frame.
- Node and edge evaluation must use collision-aware geometry and a clearly documented cost policy.
- Admissible heuristic for optimal A* when costs allow; otherwise explicitly characterize weighted A* quality tradeoffs.
- Deterministic stable priority-queue tie-breakers; avoid unordered iteration influencing route choice.
- Early, bounded termination with `UNREACHABLE`/`PARTIAL`/`BUDGET_EXCEEDED` and backoff.
- Prevent diagonal corner cutting through solid adjacent geometry.
- Distinguish static topology from dynamic obstructions (doors, temporary mobs, moving blocks).
- Path smoothing requires a swept-body collision clearance test, not a ray through blocks.
- Path execution uses the normal movement control/collision system; do not directly set position to waypoint as a shortcut.

## 5.2 Dynamic replanning and cache validity

Replan when materially necessary: obstacle change intersects path corridor, goal moved beyond hysteresis distance, path becomes blocked, movement frame transforms unexpectedly, or the agent's movement capabilities change. Debounce small changes and distribute requests across ticks.

Cache *unreachable* destinations by `(dimension, frame, agent capability class, source region, target region, relevant world revision)`. Enforce max entries and expiration. Distinguish “failed due to budget” from “physically unreachable”: do not cache them as equivalent. A global timeout without world revision can strand mobs permanently behind newly opened doors.

For every cache hit, record hit/miss and whether a subsequent real-world success contradicts the cache. Preserve reproducibility: expiry by server tick or monotonic limited counters, not wall-clock assumptions in tests.

## 5.3 Stuck detection and recovery

Classify rather than blindly retry:

1. Agent should be moving and has a valid target.
2. Compare expected progress toward next waypoint against actual displacement over a sliding tick window; ignore intentional pauses, combat actions, knockback and congestion where possible.
3. Check obstruction, collision, target change, missing chunk, navigation frame movement, and invalid goal.
4. Recovery ladder: wait briefly → re-evaluate local path → bounded full replan → abandon intent with cooldown → vanilla fallback and telemetry if systemic.
5. No teleporting, arbitrary block destruction, player trapping or endless tight loops.

## 5.4 Async search — intentionally gated

**Do not write vanilla world queries on worker threads.** `Level`, `Entity`, chunk providers, block entities, capabilities and most mod APIs are not thread-safe.

If and only if profiling proves useful:

1. **Main thread snapshot:** capture a strictly bounded loaded area into immutable arrays/structs, including collision/hazard facts required by the planner. No unloaded chunk access. Reserve memory before copying; abort if limits exceeded.
2. **Worker computation:** pure immutable snapshot + value-only request, bounded queue, bounded thread pool, bounded search depth/nodes/time/memory, cooperative cancellation. No level/entity/registry mutation and no accessing live global registries from workers.
3. **Main thread apply:** confirm entity exists in same dimension/frame, request ID/generation is current, snapshot revision/critical collision corridors remain valid, goal unchanged, movement ownership valid, and route is safe. If not, discard and retry according to budget.
4. **Shutdown:** drain/cancel jobs on world unload, server shutdown, disabling a feature, or data pack/profile reload. Never retain references to old server instances.
5. **Telemetry:** queue depth, time in queue, canceled/stale fraction, snapshot time, worker time, main-thread apply time, heap pressure and falls back.

**Alternative A1 implementation:** incremental main-thread search with a strict per-tick node budget. It may be simpler and more deterministic. Benchmark both before selecting.

## 5.5 Path failure taxonomy

Use stable codes to make telemetry and regression actionable:

`NO_TARGET`, `TARGET_GONE`, `TARGET_INVALID`, `NO_LOADED_REGION`, `COLLISION_BLOCKED`, `CAPABILITY_UNSUPPORTED`, `UNREACHABLE`, `TIME_BUDGET`, `NODE_BUDGET`, `STALE_SNAPSHOT`, `STALE_FRAME`, `SUPERSEDED`, `WORLD_UNLOADED`, `MOD_CONFLICT`, `UNEXPECTED_EXCEPTION`.

All failures must be bounded, logged at a sane level, and recover without entity corruption.

---

# 6. Perception, decisions and tactics (post-A1)

## 6.1 Perception is not omniscience

Inputs are real events and legitimate in-world queries: visibility, sound-trigger events with distance attenuation, taking damage, nearby interactions, and entity-specific senses. Avoid scanning all loaded entities for each mob every tick; use spatial indexing or NeoForge-supported bounded queries where safe.

**Visibility:** configurable FOV/occlusion, line-of-sight, target memory decay. **Hearing:** only from instrumented actual game events or approved inferred events; do not invent the ability to hear every packet or hidden player action. **Threats:** honor teams, friends, tamed entities and vanilla target validity.

## 6.2 Memory model

Working-memory entries carry `source`, `value`, `observedTick`, `expiryTick`, `confidence`, and optional target UUID. Every entry is bounded, expires, and invalidates when its target or dimension changes. Memory persistence and learning do **not** belong in A2 unless explicitly approved.

## 6.3 Behavior selection

Begin with small, explainable utility scoring / finite state transitions. Do not install GOAP and behavior trees merely because they are available.

States for a zombie pilot: `IDLE`, `ACQUIRE`, `APPROACH`, `ATTACK_READY`, `REPOSITION`, `SEARCH_LAST_SEEN`, `RECOVER`, `DISENGAGE`, `VANILLA_FALLBACK`. Transition priority: **hard safety/game constraints > urgent interrupt > active intent with hysteresis > opportunistic tactical change**. Stable cooldowns and hysteresis avoid thrashing.

Later support configurable behavior trees or GOAP through an interface, with identical budget, telemetry, cancellation and gameplay constraint contracts.

## 6.4 Tactical behavior profiles

- **Zombie:** choose physically valid approach routes, avoid needless falls, recover when blocked; never gain unexplained reach or speed.
- **Skeleton:** maintain an effective firing band, seek an actual line of sight, strafe appropriately, and avoid intentional friendly fire within configured policy; respect bow cooldowns, projectile mechanics and target rules.
- **Spider:** preserve climbing semantics, distinct 3D topology; do not reuse walker-only navigation.
- **Animals:** avoid hazards and flee intelligently without unfair perfect anticipation of predators.
- **Bosses:** their scripted action/phase owners win; no generic replacement by default.

Group intelligence later uses **bounded squads** with explicit membership, maximum broadcast radius, cooldowns, expiry and ownership. Shared observations are not perfect knowledge. Define friend/foe ambiguity as conservative non-attack by default.

---

# 7. Interoperability, Sable and the golem project

## 7.1 Compatibility strategy

Adapters are selected by **explicit entity type and capability**, not just `instanceof Mob`. A mod author can register an adapter and specify which behavior components BMAI owns. If an entity has custom locomotion or an unrecognized `Brain` controller, the default is `OBSERVE`.

Provide documented compatibility hooks for:

- External target validity/faction providers.
- Entity-specific movement capability providers.
- World-frame transformations for moving structures.
- Non-destructive registration of advisory sensors.
- Optional behavior ownership agreement/handshake.
- Per-mod/ID exclusions and compatibility diagnostics.

A mod's API is allowed to say **unsupported**. Never reinterpret an unknown boss as a generic zombie.

## 7.2 Moving reference frames

Model a path position as `{ dimension, frameId, localPosition }`. Represent a connection between frames as a **validated transition edge**, not as two unrelated blocks that happen to overlap in global world coordinates. Transition edges track physical adjacency, current transform, safety, velocity effects, and whether boarding is possible.

Phased moving-platform acceptance:

1. A5-P0: stationary platform awareness while moving; hold station.
2. A5-P1: navigate between local waypoints on one translating/rotating platform.
3. A5-P2: transition world ↔ platform with safety validation.
4. A5-P3: platform ↔ platform transitions if public APIs and stable IDs support them.
5. A5-P4: cross-frame combat/line-of-sight after separate verification.

Sable Pathfinder already advertises cross-sub-level routes. Treat **coexistence and delegation** as the preferred first experiment. Do not bundle, modify, or reproduce ARR code. Integrations should use a separate source set or reflective/service-facing adapter with optional-dependency classloading verified through tests.

## 7.3 Buildable Golem Guards contract

Golem Guards must be able to register a `GolemNavigationCapability` (size, mass-related movement constraints, step/jump, climb/fly/water flags, path costs), a `GolemTargetPolicy`, and AI behaviors via public `AiProvider`. Buildable Golems continues to own assembly, equipment, energy, damage and player orders. BMAI owns generic navigation/perception/planning **only when delegated**. Neither mod may require the other for basic startup.

## 7.4 Other named projects

- **Cageborn:** special brood intelligence, hive control and unusual locomotion stay in Cageborn, with optional shared perception/navigation primitives.
- **AdaptiveBoss:** preserve encounter sequencing, phase orchestration and event-specific attack logic. Avoid accidental goal/Brain replacement; expose advisory navigation and scheduler metrics only until specifically tested.

## 7.5 Compatibility matrix (maintain with exact versions)

| Environment | Required test |
|---|---|
| NeoForge + BMAI | Client, integrated server, dedicated server boot |
| NeoForge without BMAI | Baseline, no saved-world harm |
| BMAI disabled via config | Vanilla-equivalent functional behavior |
| With AI optimizer mods | No double injection, no corrupted route state |
| With common entity/animation mods | Preserve attack animation and controller ownership |
| With AzureCortex / SmartBrainLib | No implicit conflicts; only explicit tested adapters |
| With Sable / Sable Pathfinder | A5 only; no dual ownership |
| With Create/Aeronautics | A5 only; platform behavior isolated |
| Unknown modded bosses | Excluded by default |
| On server without optional client visuals | Boot, log in and play, or explicitly document a required client distribution |

---

# 8. Lifecycle, networking and persistence

## 8.1 Attach and detach

**Critical caveat:** `EntityJoinLevelEvent` can fire during chunk loading before full readiness. On join, record only eligibility and enqueue a deferred safe initialization; do **not** inspect neighbors or synchronously load chunks. On server tick when state is ready, attach the adapter. On removal/death/dimension travel, stop owned movement, cancel jobs, invalidate data and detach exactly once.

On `/reload` or server config reload, validate the new profiles first, then atomically apply them to eligible agents at a safe server-thread boundary. A failed reload retains the previous valid configuration. No mid-tick half migration.

## 8.2 Entity state

Persistent attachments: schema version, optional explicit profile override/consent, optional long-running externally visible assignment, never an expensive snapshot/cache. Entity attachment copying rules must be explicit; **no copy-on-death** unless the use case calls for it. Registry changes and mod removal require a safe migration/removal plan. Avoid registering items/blocks until needed to reduce save coupling.

## 8.3 Networking

A1 mechanics run entirely server-side. Client features are optional and limited to visual diagnostics or management UIs. If adding packets, use NeoForge 1.21.1 `RegisterPayloadHandlersEvent`, explicit versions/codecs, server-side permission checks, size/rate caps, and main-thread execution for world reads. A debug packet may only reveal info an authorized player can access.

## 8.4 Operators' commands

Prototype server-side command namespace: `/betterai` (final spelling follows frozen mod name):

```text
/betterai status
/betterai profile <entity-id|look-target>
/betterai debug <entity-id|look-target> [on|off]
/betterai metrics [summary|entity|reset]
/betterai config validate
/betterai compat report
/betterai mode <observe|assist|enhanced> [entity-type]
```

These are **specifications**, not implemented commands. Never expose runtime mode switching to ordinary players unless server operators grant permission. Capture a concise reason trace: eligibility → controller owner → chosen intent → path outcome/failure → fallback reason.

---

# 9. Scheduling and performance architecture

## 9.1 Resource constraints

Use a centralized `AiWorkScheduler` with fairness and bounded costs. Scheduler tracks request priority, deadline, freshness and age. High-priority immediate threats may jump ahead but cannot starve all normal agents. Agents far from players may be scheduled less often **only for tasks that can safely be deferred**. Do not indiscriminately skip `LivingEntity.tick()` or entity physics/health status.

**Important:** canceling `EntityTickEvent.Pre` to throttle AI is disallowed unless a much narrower well-proven use is approved; it can skip unrelated vanilla entity work.

Suggested initial **configuration defaults for experimental testing**, not empirically established production values:

- Maximum outstanding path job: 1/entity.
- Stale requests are superseded, not queued indefinitely.
- Max queued global requests: finite and configurable (candidate: 256).
- Per-entity backoff after failed retry: bounded exponential with cap and revision invalidation.
- Worker threads: 0 until async is shown advantageous; when enabled, bounded, defaults based on measured headroom, not all available CPUs.
- Per-agent memory and vision checks: configurable intervals based on entity relevance and actual profiling.

No magic universal values: record benchmark justification for final defaults in `docs/benchmarks/DEFAULTS.md`.

## 9.2 Observability requirements

Collect counters/histograms with negligible overhead when disabled:

- Active agents by profile/mode, controller ownership conflicts.
- Path requests, success/partial/unreachable/stale/cancel/timeout rates.
- Navigation progress/arrival and stuck event rates.
- Snapshot capture time, search time, apply validation time.
- Scheduler wait time, queue size, priority distribution, starvation age.
- AI main-thread tick cost percentiles (without expensive per-tick allocations).
- Memory/cache sizes, hit/miss/invalidation/eviction.
- Packet counts and bytes if debug sync exists.
- Fallback occurrences and reason codes.

Export human-readable reports to `logs/betterai-metrics/` on operator request; do not write a file every tick. Logs must avoid player private identifiers where possible.

## 9.3 Benchmark protocol

Use three arms: **vanilla/NeoForge baseline**, **mod installed in OBSERVE**, **feature enabled**. Same world seed, scene build, entity count, config, JVM/JDK, hardware, view/simulation distance, server startup/warmup, deterministic spawn timing and test duration. Repeat runs (initial target: 5 measured replicates per scenario; increase if noise is high) and record per-run results, not only averages.

Required scene families:

- Flat open plain; minimal-path baseline.
- 2D maze with dead ends.
- 3D stairs/corridors/tight passages and entity-sized clearance.
- Mixed hazards with alternative safe routes.
- Doors, gates, trapdoors, water and short drops.
- Dynamic opening/closing door and block placement/removal.
- Moving player pursuit and target disappearing from LOS.
- 1/16/64/128+ eligible agents with normal world entities present.
- Villagers/Brain-based entities (OBSERVE and no regressions).
- Dedicated-server world save, reload and dimension transition.
- Separate Sable moving-platform suite later.

Measure tick-time median/p95/p99, per-feature AI CPU, navigation success%, arrival time in ticks, route length/cost, stuck incidents, per-mob requests per minute, heap overhead, allocations/GC, exception count, and fallback incidence. Store raw data in machine-readable CSV/JSON alongside profile settings and run metadata.

### Proposed exit thresholds (engineering targets; re-baseline after first measurements)

- **Correctness:** 100% pass on deterministic structural invariants; 0 crashes, data loss, duplicate goals, infinite retry loops or unsupported classloading.
- **Behavior improvement:** statistically defensible gain in at least two difficult navigation scenarios; no significant regression in simple traversals. Report effect sizes and variability, not cherry-picked videos.
- **Performance:** feature-on median and p95 server tick time not materially worse than observe/baseline at matched loads; proposed alert if >5% p95 degradation or >1 ms additive p95 server tick cost in target scenes. Treat thresholds as release gates subject to documented director approval, never as achieved benchmarks.
- **Gameplay:** unchanged baseline combat damage/movement stats, target eligibility, spawning and loot where not explicitly in scope.
- **Fallback:** disable flag returns to normal compatible behavior immediately or at next safe lifecycle boundary; 0 lingering movement ownership after transition.

For large servers the 50 ms/tick target for 20 TPS is a hard performance context, **not** a promise that the mod itself can consume 50 ms. Set a per-feature budget from measured server headroom.

---

# 10. Configurations and data-driven interoperability

Use NeoForge-compatible `ModConfigSpec` for common/server/client configuration (verify exact 1.21.1 builder APIs), with central validation and commented documented defaults. Admin-facing TOML examples below are a **proposed schema**, not exact runnable generated TOML:

```toml
[general]
mode = "OBSERVE"  # OBSERVE, ASSIST, ENHANCED
allow_unknown_modded_entities = false
fail_open_to_vanilla = true

[navigation]
enabled = true
stuck_detection = true
unreachable_backoff = true
max_replans_per_entity_window = 4

[scheduling]
# Tune using benchmarks; values below are experimental starter caps
max_global_queue = 256
max_pending_per_entity = 1
async_enabled = false

[features]
perception = false
combat_positioning = false
group_coordination = false

[debug]
metrics_enabled = false
path_overlay_enabled = false
verbose_logging = false
```

**Do not implement config fields merely because they appear here:** each field requires a real behavior, validation, documentation, and tests. Use `ResourceLocation` parsing for entity-type overrides. Provide server-side denylist (entities and mod namespaces) with precedence over opt-in. For datapack profiles, use codecs/data maps/tags where they provide safe validated reload semantics; specify the exact NeoForge registry path once verified. Reject malformed overrides atomically with line-specific errors.

---

# 11. Quality engineering and CI

## 11.1 Coding standards

- Java 21, NeoForge 1.21.1, documented package naming, stable interfaces.
- No raw generic types; avoid unchecked casts and unchecked suppressions. Where unavoidable, write a narrow justification and unit tests.
- Use package-level nullness defaults plus explicit nullable annotations on boundary APIs, consistently with the chosen annotations available in the target toolchain.
- Checkstyle **10.x** in CI, including import ordering, whitespace, naming, Javadocs on public APIs and prevention of unused imports.
- JaCoCo **100% line and branch coverage for Minecraft-independent deterministic domain modules**; exclude Minecraft lifecycle/glue code from that *specific* threshold and test it with GameTests/functional tests. Do not silently reduce coverage thresholds or create superficial tests to reach percentages.
- Prefer explicit immutable value objects and defensive copies for async inputs; prevent unnecessary allocations inside entity tick hot paths.
- No reflective version spoofing, unsafe mixin `@Overwrite` except explicitly documented ADR after alternatives exhausted, blind field mutation, or broad catch-and-ignore.
- No `@SuppressWarnings` used to hide structural problems; document narrowly and gate exceptions.
- Fix code, not tests, when a valid regression test fails.

## 11.2 Testing pyramid

**MC-free unit/property tests:** graph search, heuristic admissibility where claimed, cost model, stable tie-break, snapshot revision validity, deadline/failure result, queue fairness, cancellation, cache invalidation, backoff, state transitions, profile precedence and malformed config rejection. Include random seeded maze generators and differential tests against a simple slow oracle.

**GameTests:** zombie arrives in valid maze; avoids impassable fence; safe route around lava; replans after door opening; target lost; unload/reload; disabled AI remains vanilla-compatible; no double owner; boss excluded; controlled skeleton behavior only when A3 enabled.

**Manual smoke matrix:** Windows PowerShell `./gradlew.bat ...` and Unix `./gradlew ...`, integrated client, dedicated server without display, runtime mod toggles, save/reload, mod removal, Sable absent, and intentionally unsupported modded mobs. Never claim visual/UI smoke tests passed on a headless-only build.

**Performance tests:** synthetic MC-free throughput plus in-game scene measurements; distinguish correctness unit tests from real server performance measurements.

**Security tests:** operator permission for commands, malformed config and packets, oversized or repeated debug requests, stale entity IDs, out-of-range coordinates, world transitions, and denial of service via mass path requests.

## 11.3 Required Gradle/CI jobs

Codex must create a working wrapper and verify tasks from the actual MDK. Representative commands (the exact task wiring is to be verified):

```powershell
# Windows PowerShell, from repo root:
.\gradlew.bat --version
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat checkstyleMain checkstyleTest
.\gradlew.bat jacocoTestReport jacocoTestCoverageVerification
.\gradlew.bat runGameTestServer

# Linux/macOS shell:
./gradlew clean build
./gradlew test
./gradlew runGameTestServer
```

**Note:** these commands are a requested CI target, not a report of execution. `runGameTestServer` may require configured test resources and correct MDK startup settings. Only run `checkstyle*`/`jacoco*` once those plugins and tasks are actually installed. Build pass is not game functionality proof.

Minimum `.github/workflows/ci.yml`: wrapper validation, Java 21 environment, dependency caching with integrity checks, compile/build, unit tests, Checkstyle, coverage verification, GameTestServer if supported in CI runner, artifact upload on failed test, dependency inventory, and a final pass/fail summary. Pin third-party GitHub actions to major/commit policy. Disable automatic releases until director approval.

## 11.4 Build integrity and release artifacts

No missing Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`), no phantom dependencies, no silent bypass due to offline environment. Include `README.md`, `CHANGELOG.md`, `LICENSE` only once chosen, `THIRD_PARTY_NOTICES.md`, sample config, compatibility table, manual test instructions, and a reproducible versioned jar. ZIP source distributions must include the wrapper and all source/resources required to run `clean build` offline **after legitimate dependencies are cached**, not by checking random caches into source.

---

# 12. Failure modes and required defenses

| Failure mode | Impact | Required defense / test |
|---|---|---|
| Joined before chunk FULL | deadlock / chunk-load side effects | deferred safe initialization, loaded-chunk guards |
| Two systems drive navigation | jitter, stuck, attacks interrupted | explicit ownership state + conflict detection |
| Worker touches `Level`/entity | crashes/corrupt state/races | immutable snapshots only; thread ownership tests |
| Result applied after movement/teleport | unsafe path / apparent teleport | request generation + dimension/frame/goal validation |
| Cache says unreachable after door opens | permanent mob failure | revision-aware invalidation + cooldown |
| Whole-entity tick skipped for CPU savings | status, physics, combat regressions | schedule only AI subwork; never cancel broad tick |
| Excessive replanning | TPS spikes | backoff, quotas, fairness, queue caps |
| Snapshot covers unloaded chunks | unexpected synchronous loads | loaded-region bounded snapshot, safe fail |
| Path clears for block but not mob hitbox | clipping, suffocation | swept-body/actual collision clearance |
| Modded scripted mob rewritten | broken phases / boss softlock | observe default, per-entity allowlist |
| Faction/team mismatch | friendly fire, attacking owners | authoritative target policy + tests |
| Unknown mod absent but classes loaded | NoClassDefFoundError on startup | optional integration classloader isolation |
| Entity unloaded while job outstanding | leaks, stale apply | cancel-on-detach, bounded job lifecycle |
| Reload mutates profiles mid-tick | corrupted state | validate then atomic swap at tick boundary |
| Endlessly growing memories/squads | heap leak | capacity, TTL, removal hooks |
| Async results vary nondeterministically | hard-to-reproduce bugs | deterministic request ID, stable tie-break and recorded seed |
| GUI/debug leaks state or floods packets | abuse/performance | permission + rate/size caps |
| AI becomes unfair | bad gameplay balance | preserve stats/target rules, opt-in tactics, configurable difficulty |
| Sable reference frame lost | entity stranded or misplaced | reject stale frame and fall back safely |
| Gradle build succeeds, gameplay broken | false release readiness | mandatory GameTest and real smoke evidence |

---

# 13. Implementation milestones, deliverables and exit gates

**Rule for every phase:** Codex opens with a short plan, identifies exact files to change, implements one vertical slice, runs its tests, documents observed results, reports known limitations, and waits for director approval before treating a new phase as complete. A failed command is a failure, not a green check. Continue within a phase to fix honest failures without prompting for every small correction.

## P0 — Bootstrap, audit and baseline (blocking)

**Build:** Generate repo from official 1.21.1 ModDevGradle MDK. Freeze working IDs; Java 21, Gradle wrapper, Checkstyle 10, tests, JaCoCo, CI. Add `OBSERVE` no-op instrumentation with correctly deferred lifecycle. Capture vanilla/observe benchmarks. Audit AI APIs and candidate frameworks. Create architecture ADRs and evidence links.

**Artifacts:** clean project, `docs/compat/HOOK_AUDIT.md`, `docs/adr/ADR-001-ai-framework-selection.md`, `docs/benchmarks/BASELINE.md` plus machine-readable baseline results, startup logs, one working GameTest, dependency lock/pins.

**Exit:** Windows and Linux-compatible Gradle instructions work; `clean build` and test suite actually pass; dedicated server boots and shuts down; observe mode changes no game behavior; initial baseline reproduced. If these fail, **do not progress**.

## P1 — Eligibility, profiles and safe ownership

**Build:** `ProfileResolver`, opt-outs, entity UUID runtime map with lifecycle cleanup, diagnostics and mode transitions. Implement at least one verified ownership mechanism for the **C0 zombie only**, with a working fallback. If safe replacement cannot be demonstrated, remain in OBSERVE/ASSIST and write a blocker.

**Artifacts:** agent lifecycle tests, command status/profile, fallback trace, compatibility gate docs.

**Exit:** No duplicate navigation/goal ownership; zombie can toggle OBSERVE ↔ supported enhanced behavior; save/load/dimension changes safe; unsupported entities remain untouched.

## P2 — Land navigation vertical slice (first usable alpha)

**Build:** scoped zombie path enhancement, path failure taxonomy, bounded replan, stuck recovery, collision checks, dynamic obstacle response. Prefer vanilla-based path improvements initially. Build pure-Java path algorithms only if justified.

**Artifacts:** nav GameTests, 2D/3D/hazard/dynamic scene data, paired A/B benchmark results, diagnostic reasons.

**Exit:** Repeated test runs show measurably better navigation in hard scenes without unacceptable tick-time regression, plus exact rollback behavior.

## P3 — Perception and tactical pilots (v0.2+)

**Build:** LOS and bounded last-seen memory, controlled zombie search behavior, skeleton ranged-positioning integration. All disabled unless profile enables. Preserve target legality and attack timings.

**Exit:** no omniscient targeting, no illegal attacks, no regression in existing combat abilities; combat scenes GameTest-covered.

## P4 — Scheduling and high-load optimization

**Build:** scheduler fairness, work quotas, cache instrumentation, benchmark-led adjustments. Add snapshot/async algorithm only if profile evidence establishes benefit and validation tests prove safety.

**Exit:** no starvation under load, bounded memory and tasks, reproducible results and no unacceptable p95 regression.

## P5 — API stabilization, third-party pilots

**Build:** versioned public API, contract test fixtures, example external adapter project. Golem guards advisory integration proof; modded boss defaults to excluded. Optional behavior tree/GOAP may be added under ADR, not mandatory.

**Exit:** external stub mod compiles against API and registers an opt-in entity without directly modifying internals; incompatible entity reports understandable reason.

## P6 — Optional Sable/vehicle research branch

**Build:** world-frame provider, Sable Companion API proof, coexistence with Sable Pathfinder; transform and lifecycle tests. Keep disabled unless exact dependency matrix passes.

**Exit:** stationary moving-platform acceptance first, then progressively cross-frame route tests; all runs safe without Sable installed. If unreliable, defer without delaying base release.

## P7 — Stable release candidate and hardening

**Build:** docs, migration and rollback tests, release CI, compatibility pass, profiling artifacts, security/config sanity, changelog and known limitations.

**Exit:** all supported features pass tests; no unresolved P0/P1 defects; release notes explicitly list supported entities and unsupported mods; operator can disable enhancements without save corruption.

---

# 14. Acceptance matrix (Codex must maintain live)

| ID | Requirement | Verification | Gate |
|---|---|---|---|
| R-001 | Clean Java 21 / NeoForge 1.21.1 build | `clean build` log, jar | P0 |
| R-002 | Valid wrapper on Windows/Unix | wrapper files and command results | P0 |
| R-003 | Observe mode behavior unchanged | A/B GameTest/smoke comparison | P0 |
| R-004 | No live world reads during unsafe join event | code audit and lifecycle GameTest | P0 |
| R-005 | Unknown modded mobs untouched | synthetic modded Mob GameTest | P1 |
| R-006 | One navigation owner | state machine unit/GameTest | P1 |
| R-007 | Toggle safely to vanilla fallback | GameTest and restart test | P1 |
| R-008 | Reachable maze route completion | repeated deterministic scene | P2 |
| R-009 | Unreachable target bounded retries | diagnostics + time-budget tests | P2 |
| R-010 | Dynamic path obstruction recovery | block-edit GameTest | P2 |
| R-011 | Entity geometry respected | hitbox/collision tests | P2 |
| R-012 | No chunk loads initiated for AI snapshots | instrumented loaded-bound test | P2/4 |
| R-013 | Performance benchmark reproducible | raw JSON/CSV and Spark profile | P2/4 |
| R-014 | No illegal target acquisition | team/creative/spectator tests | P3 |
| R-015 | No target omniscience | LOS/memory decay tests | P3 |
| R-016 | Scheduler bounded and fair | property/stress tests | P4 |
| R-017 | Async never reads live world | architectural test and review | P4 if async |
| R-018 | API works for external adapter | separate sample mod compile/integration | P5 |
| R-019 | Missing optional mod never crashes | classloader/dedicated-server smoke | P5/P6 |
| R-020 | Sable frame state validated | transform and unload tests | P6 |
| R-021 | No data loss across save/reload | persistence tests | P1/P7 |
| R-022 | No raw generics; nullness policy applied | Checkstyle/analysis review | Every phase |
| R-023 | 100% MC-free module line/branch coverage | JaCoCo report | Every phase after domain code |
| R-024 | Real release notes and truthful validation | CI + manual checklist | P7 |

Every acceptance entry must contain: implementation commit SHA, test evidence path, actual result, environment/build IDs, and unresolved limitation if present. **Never mark PASS from an expectation or mock alone where an in-game test is specified.**

---

# 15. Codex work protocol and reporting contract

## 15.1 Repository protection

1. Detect whether repository already exists and is non-empty. Never overwrite user files or reset branches without approval.
2. Inventory project files before editing. If preexisting code exists, inspect before designing around it.
3. Use feature branches, small atomic commits, and `git diff --check`.
4. Do not add unknown remote, publish packages, push to external repositories, or create releases without explicit permission.
5. If internet/dependency resolution is unavailable, do not fabricate a successful build. Record blocker and provide exact retry steps.
6. Do not replace failed tests with weaker versions merely to satisfy a metric; fix production defects.
7. Do not mass-apply version-guessing code. Validate imports, generics and signatures against checked-out 1.21.1 sources.

## 15.2 Mandatory report after each work slice

```markdown
### Slice report — <phase / task / commit>
**Goal:**
**What changed:** [files/classes and specific behavior]
**Design deviation from blueprint:** [none or justified ADR]
**Commands executed:** [exact commands and return values]
**Tests:** [passed / failed / not run, counts and evidence]
**Performance:** [measured comparison or NOT MEASURED]
**Known limitations / incompatibilities:**
**Risks:**
**Next smallest useful task:**
```

Any unsupported claim of “fully complete,” “100% compatible,” or “production ready” without the gate evidence is a specification violation.

## 15.3 First exact instructions for Codex

> **Execute Phase P0 only.** Do not develop an entire AI rewrite or implement speculative async pathfinding. Start by examining the repository and verifying the precise Minecraft 1.21.1 / NeoForge toolchain. Bootstrap a valid ModDevGradle project if empty; otherwise preserve the existing structure. Add Java 21, Checkstyle 10, JaCoCo policies, a small MC-free testable domain module, a vanilla-compatible `OBSERVE` mode, a GameTest scaffold and initial diagnostics. Inspect the mapped `Mob`, `PathNavigation`, `GoalSelector`, `Brain`, and NeoForge lifecycle entry points and create `HOOK_AUDIT.md` with exact source references and risks. Evaluate AzureCortex/SmartBrainLib/Sable Pathfinder without adding required dependencies. Produce the initial baseline methodology and actual logs, run real Gradle commands, and report failures honestly. Stop at the P0 gate with a work summary and any blockers.

---

# 16. Deferred track B — Living Minecraft AI

This is **architectural preparation only**. No B components should be required, initialized, or bundled for A1.

Potential future components:

- Long-term bounded episodic memory with explicit persistence scope and expiry.
- Creature needs/motivations and scheduled activities.
- Individual temperament/personality trait profiles and controlled variation.
- Multi-agent relationships, settlements, factions and communication rules.
- Offline planning *only* as explicitly designed world simulation, not hidden forced chunk loading.
- Action outcomes and experience-derived policy adaptation with deterministic replay and bounded data.
- Optional model-assisted planning after privacy, resource, security and reproducibility research; never silently network player/world data.

**Forward compatibility now:** stable agent identifiers, versioned saved schema, separable working memory, human-inspectable goals, interruptible actions, observable decision provenance, provider/plugin registry. **Do not** overgeneralize A1 into a simulated civilization engine prematurely.

---

# 17. Decision log / open questions that must be closed with experiments

| Decision | Default stance | Evidence required to change |
|---|---|---|
| Public name/license | Temporary name; license undecided | Director sign-off before release |
| Core AI library | No mandatory dependency | ADR comparison and clean integration proof |
| Vanilla AI takeover mechanism | C0 targeted only | Hook audit, game tests, conflict matrix |
| Path algorithm | Vanilla-based improvement first | A/B evidence supporting replacement |
| Async engine | Disabled | Measured server-thread savings net of snapshot cost |
| Modded mobs | Observe-only | Explicit adapter/opt-in test suite |
| Brain entities | Observe-only | Separate safe schedule/behavior adapter proof |
| Moving ships | Future optional integration | Sable identity/frames, platform navigation test |
| Public API stable version | Experimental until P5 | External sample and contract test |
| Release performance thresholds | Proposed, not factual | Baseline data and approved targets |

---

# 18. Definition of done for the overall first stable A1 release

**Binding acceptance addendum:** `Better_Minecraft_AI_Definition_of_Done.md` defines the measurable P0/A1 gate contract, evidence package, milestone status language, and director approval. Use it alongside this section; neither document permits bypassing a test or silently re-scoping a release.

A1 is release-ready **only when all** are true:

- The documented 1.21.1 NeoForge Java 21 build is reproducible from a clean checkout.
- At least one supported vanilla land entity has genuinely improved navigation in a controlled benchmark, with behavior proven by GameTests.
- Disabled and observe modes retain compatibility, and unsupported entities are not silently modified.
- Fallback cannot strand, duplicate, teleport or corrupt entities or cause uncontrolled server errors.
- No main-thread or memory regression above approved budget in the target benchmark suite.
- No global AI overwrite, no broad tick cancellation, no unbounded queues/caches/memories.
- Persistent data survives load/unload/restart, and optional dependency removal does not crash startup.
- All required Checkstyle, MC-free JaCoCo and GameTest gates pass, with actual CI artifacts.
- README identifies enabled/disabled entities, performance considerations, interoperability exclusions, and rollback instructions.
- The director approves the milestone and its documented limitations.

**What success does NOT mean:** every Minecraft mob becomes self-aware; every entity can path on Sable ships; every mod is compatible; future autonomous behavior has been implemented. These claims require separate evidence and later releases.

---

# 19. Research source notes and evidence limitations

- The 1.21.1 MDK source lists a concrete tested-looking dependency *template*, not a guarantee of local resolution or compilation in our workspace. Codex must test it.
- API class/method names in the official docs and mappings establish potential extension points. Their interactions with specific mobs, vanilla schedules and third-party coremods remain **unverified in our project** until Phase P0.
- AzureCortex is a developer framework, not an out-of-the-box global vanilla AI replacement. SmartBrainLib similarly does not make all vanilla AI replaceable by mere dependency installation.
- Sable Pathfinder's advertised cross-frame behavior must be verified for actual version combinations; its ARR license restricts copying code independent of any source-visibility assumptions.
- No source code, GameTests, Gradle build or live Minecraft benchmark has been executed as part of preparing **this blueprint**. Only the engineering plan and publicly documented API/ecosystem claims are established.

## Research links (retrieved 2026-10-09)

1. NeoForge 1.21.1 Getting Started — https://docs.neoforged.net/docs/1.21.1/gettingstarted/
2. Official MDK Gradle properties — https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/blob/main/gradle.properties
3. Official MDK build script — https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle/blob/main/build.gradle
4. 1.21.1 `Mob` mappings — https://mappings.dev/1.21.1/net/minecraft/world/entity/Mob.html
5. 1.21.1 `PathNavigation` mappings — https://mappings.dev/1.21.1/net/minecraft/world/entity/ai/navigation/PathNavigation.html
6. NeoForge 1.21.1 EntityJoinLevelEvent Javadoc — https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/neoforged/neoforge/event/entity/entityjoinlevelevent
7. NeoForge 1.21.1 GameTests — https://docs.neoforged.net/docs/1.21.1/misc/gametest/
8. NeoForge 1.21.1 attachments — https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
9. NeoForge 1.21.1 networking — https://docs.neoforged.net/docs/1.21.1/networking/
10. NeoForge 1.21.1 data maps — https://docs.neoforged.net/docs/1.21.1/resources/server/datamaps/
11. AzureCortex source — https://github.com/AzureDoom/AzureCortex/tree/1.21.1
12. SmartBrainLib source and license — https://github.com/Tslat/SmartBrainLib
13. Sable Pathfinder landing page — https://modrinth.com/mod/sable-pathfinder
14. Spark profiling — https://spark.lucko.me/
15. NeoForge 1.21.1 modded path optimizer (comparison) — https://www.curseforge.com/minecraft/mc-mods/path-finder

---

**End of Codex engineering blueprint.**

# Phase P1 execution plan — eligibility, profiles, and safe ownership

**Status:** IN PROGRESS

**Accepted prerequisite:** P0 director acceptance at commit `5848143e16a48db2da38cd859f663b845cc7e62b`

**Working branch:** `codex/p1-eligibility-safe-ownership`

**Runtime release boundary:** the built artifact remains `0.1.0-p0` and OBSERVE-only until Minecraft-side P1 gates pass

## Objective

Execute blueprint P1 without starting P2 path enhancement: resolve safe profiles, attach bounded UUID-only runtime state, transfer one ordinary vanilla zombie's MOVE ownership through an exact lease, restore vanilla state on every exit, and expose actionable diagnostics. Unsupported entities must remain untouched.

## Frozen invariants

1. The only built-in entity eligible for ENHANCED navigation in P1 is exact type `minecraft:zombie`.
2. Resolution precedence is `hard safety exclusion > admin denylist > per-entity override > tag/data profile > global mode`, with built-in C0 capability gating every ENHANCED request.
3. The global default remains OBSERVE. ASSIST does not acquire MOVE ownership.
4. Navigation ownership is always one of `VANILLA_OWNED`, `TRANSITIONING`, or `ENHANCED_OWNED`; cleanup is illegal until vanilla ownership is restored.
5. P1 retains the existing `PathNavigation`. It may add and remove only its exact priority-0 MOVE lease; it never clears unknown goals or replaces selectors/navigation.
6. Runtime state is keyed by entity UUID, bounded by loaded attached entities, and retains no live entity or level reference in the Minecraft-independent domain.
7. Leave, death, unload, dimension transition, config disable/reload, server stop, and detected owner conflict must release the exact lease and clear movement before runtime removal.
8. No A*, async worker, perception, tactics, group behavior, Sable integration, or performance-improvement claim belongs to P1.

## Vertical slices

| Slice | Deliverable | Required proof | Status |
|---|---|---|---|
| P1.1 | MC-free profile resolver, C0 allow-list, mode/ownership transition model, UUID runtime registry | Unit tests, Checkstyle, 100% MC-free line/branch coverage | PASS |
| P1.2 | NeoForge config and entity lifecycle boundary using exact 1.21.1/21.1.256 APIs | Config validation tests; attach/leave/stop GameTests; OBSERVE non-interference | NOT RUN |
| P1.3 | Exact C0 MOVE lease and fail-closed compatibility gate | Exclusive-owner, acquire/release, conflict, unsupported-entity GameTests | NOT RUN |
| P1.4 | Permission-gated status/profile/mode diagnostics | Command tests and real dedicated/integrated use | NOT RUN |
| P1.5 | Restart, unload/reload, dimension, death, removal, and disable matrix | Retained logs and fallback traces | NOT RUN |
| P1.6 | Native Windows/Ubuntu CI plus MCP-assisted live inspection | Clean CI; exact-pin runtime acceptance; screenshots/state queries where MCP supports the fixture | BLOCKED |

P1.6 is presently blocked only on the auxiliary MCP fixture: `install_version(1.21.1, neoforge)` twice returned `unexpected end of file`; it installed NeoForge 21.1.172 rather than 21.1.256, and the launched process exited before the MCP mod connected. The pinned Gradle 21.1.256 development runs remain authoritative and available for ongoing implementation.

## Stop condition

P1 can be presented as a candidate only after every slice is evidenced and the blueprint P1 exit is met: no duplicate ownership; an ordinary zombie toggles OBSERVE ↔ supported enhanced ownership; save/load/dimension lifecycle is safe; unsupported entities remain untouched. P2 navigation improvement does not begin by implication.

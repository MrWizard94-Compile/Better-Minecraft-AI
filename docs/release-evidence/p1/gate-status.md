# P1 live gate status

| Gate | Status | Evidence | Remaining requirement |
|---|---|---|---|
| P1-01 immutable profile/C0 eligibility core | PASS | [slice 01](slice-01-profile-mode-core.md), [manifest](manifest.md), [self-audit](section-0-self-audit.md), unit/coverage reports | Minecraft boundary must consume the resolver in P1.2. |
| P1-02 explicit mode/ownership state machine | PASS | [slice 01](slice-01-profile-mode-core.md) | Exact goal lease must execute its actions in P1.3. |
| P1-03 UUID lifecycle registry | PASS | [slice 01](slice-01-profile-mode-core.md) | NeoForge attach/leave/stop integration and GameTests remain P1.2. |
| P1-04 config and opt-out integration | NOT RUN | [plan](../../P1_PLAN.md) | Implement validated OBSERVE default, C0 opt-in, denylist, and atomic reload behavior. |
| P1-05 production C0 ownership/fallback | NOT RUN | [P0 proof](../p0/test-results.md) | Productionize the exact lease and pass conflict/fallback GameTests. |
| P1-06 diagnostics command | NOT RUN | [plan](../../P1_PLAN.md) | Add permission-gated status/profile/mode diagnostics. |
| P1-07 lifecycle compatibility matrix | NOT RUN | [plan](../../P1_PLAN.md) | Save/load/dimension/death/unload/restart/removal evidence required. |
| P1-08 MCP-assisted exact-pin runtime | BLOCKED | [MCP runtime record](mcp-runtime.md) | Repair/provision a connected 1.21.1 / NeoForge 21.1.256 fixture and install the candidate JAR. |

P1 is **IN PROGRESS**. No A1 gate is promoted by these domain results, and P2 remains unstarted.

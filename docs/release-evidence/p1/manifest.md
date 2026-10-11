# P1.1 review manifest

**Delivery:** Minecraft-independent eligibility, profile, ownership-transition, and UUID-runtime core

**Why:** Establish the fail-closed dependencies required before any production Minecraft entity can transfer navigation ownership.

## Changed-file inventory

| Path | Purpose |
|---|---|
| `CHANGELOG.md` | Record verified P1.1 behavior and explicit non-deliveries. |
| `README.md` | Expose accepted P0, active P1 scope, environment pins, and current safety boundary. |
| `docs/P1_PLAN.md` | Freeze P1 slices, invariants, stop condition, and honest status. |
| `docs/release-evidence/p0/constitution-skill-evolution.md` | Extend the cumulative staged skill-evolution report with runtime-control acceptance safeguards. |
| `docs/release-evidence/p0/constitution-skill-evolution.patch` | Carry the validated, apply-ready cumulative v5.2.10 canonical-source proposal without propagating it. |
| `docs/release-evidence/p1/gate-status.md` | Track P1 gates without promoting unrun Minecraft or A1 gates. |
| `docs/release-evidence/p1/mcp-runtime.md` | Record the failed auxiliary MCP fixture construction and its acceptance boundary. |
| `docs/release-evidence/p1/slice-01-profile-mode-core.md` | Record P1.1 behavior, tests, limits, and next action. |
| `docs/release-evidence/p1/manifest.md` | Inventory the complete P1.1 review package. |
| `docs/release-evidence/p1/section-0-self-audit.md` | Preserve the 15-point pre-delivery gate result. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/AiMode.java` | Define OBSERVE, ASSIST, and ENHANCED execution modes. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/BehaviorProfile.java` | Define the immutable resolved profile and core enhancement invariant. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/EntityTypeKey.java` | Validate canonical namespaced entity identifiers. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/ProfileRequest.java` | Carry immutable, already-parsed profile facts. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/ProfileResolver.java` | Apply safety-first precedence, C0 capability gating, and deterministic provenance. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/ProfileSource.java` | Identify the rule responsible for a profile result. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/SafetyExclusion.java` | Enumerate hard ownership exclusions. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/profile/package-info.java` | Apply package documentation and null-marking. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/AgentRuntime.java` | Bind one UUID to an immutable profile and safe transition controller without live Minecraft references. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/AgentRuntimeRegistry.java` | Bound lifecycle state by attached UUIDs and enforce vanilla-owned cleanup. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/FallbackReason.java` | Define stable fallback diagnostics. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/ModeTransitionController.java` | Separate ownership requests, exact adapter actions, and confirmed completions. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/ModeTransitionSnapshot.java` | Expose immutable transition diagnostics. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/NavigationOwnership.java` | Define the exclusive ownership states. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/TransitionAction.java` | Define exact acquisition, release, and fallback side effects. |
| `src/main/java/com/mrwizard94/betterminecraftai/domain/runtime/package-info.java` | Apply package documentation and null-marking. |
| `src/test/java/com/mrwizard94/betterminecraftai/domain/profile/EntityTypeKeyTest.java` | Test valid, invalid, and null identifier boundaries. |
| `src/test/java/com/mrwizard94/betterminecraftai/domain/profile/ProfileResolverTest.java` | Test precedence, exclusions, fail-closed gating, immutability, and deterministic provenance. |
| `src/test/java/com/mrwizard94/betterminecraftai/domain/runtime/AgentRuntimeRegistryTest.java` | Test attachment, duplicate rejection, profile reconciliation, and cleanup invariants. |
| `src/test/java/com/mrwizard94/betterminecraftai/domain/runtime/ModeTransitionControllerTest.java` | Test all stable, acquisition, release, fallback, invalid, and null transitions. |

## Review path

1. Read `docs/P1_PLAN.md` and `docs/release-evidence/p1/gate-status.md` for scope and phase accounting.
2. Review `ProfileResolver`, `ModeTransitionController`, and `AgentRuntimeRegistry` as the three core safety boundaries.
3. Review the four test classes and `slice-01-profile-mode-core.md` for executable evidence.
4. Review `mcp-runtime.md` separately; it is a fixture blocker, not a BMAI product result.
5. Review the staged constitution payload only if governance evolution is in scope; it was not propagated.

## Verification

```powershell
.\gradlew.bat --no-daemon --warning-mode fail clean build
git diff --check
```

Expected result: clean build, 35 tests with no failures/errors/skips, zero missed MC-free lines and branches, zero Checkstyle/compiler warnings, and no whitespace errors.

## Risks and next action

P1.1 changes no live Minecraft behavior. Its contracts still require exact 1.21.1/NeoForge 21.1.256 event, config, goal-lease, diagnostics, and lifecycle integration. The auxiliary MCP fixture is independently broken and contributes no runtime acceptance. Next, implement P1.2 OBSERVE-default config and lifecycle attachment without beginning P2 navigation work.

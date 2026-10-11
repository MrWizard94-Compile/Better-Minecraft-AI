# Changelog

All notable verified project changes are documented here. Planned features are not listed as shipped behavior.

## Unreleased — P1 in progress

### Added

- Minecraft-independent C0 profile resolution with canonical entity identifiers, hard safety exclusions, explicit opt-outs, full precedence, fail-closed unsupported ENHANCED requests, and immutable provenance.
- An explicit navigation-ownership transition controller that separates mode requests from exact lease acquisition/release/fallback side effects and keeps cleanup blocked until restoration is confirmed.
- A UUID-keyed runtime registry that retains no live Minecraft object, rejects duplicate attachment, and refuses cleanup until vanilla ownership has been restored.
- Twenty-four focused unit tests for the P1 domain contracts while retaining 100% MC-free line and branch coverage.

### Not yet included

- No Minecraft event/config wiring, production C0 goal lease, diagnostic command, P1 GameTest, improved navigation, or A1 evidence is claimed by this slice.
- The auxiliary `minecraft-mod-mcp` NeoForge 1.21.1 fixture is not accepted: its installer returned `unexpected end of file`, registered NeoForge 21.1.172 instead of the project pin 21.1.256, and the resulting client exited during class resolution before connection.

## 0.1.0-p0 — accepted foundation

### Added

- Official NeoForge 1.21.1 ModDevGradle project scaffold with a pinned Java 21 toolchain, Gradle wrapper checksum, dependency locks, and artifact verification metadata.
- Checkstyle 10 and Java compiler warning gates, plus JaCoCo 100% line/branch verification for deterministic Minecraft-independent domain code.
- Server-side OBSERVE instrumentation with deferred attachment, bounded lifecycle state, non-interference assertions, and explicit shutdown cleanup.
- Eleven unit tests and seven in-game GameTests covering deterministic domain behavior, OBSERVE non-interference, test-only exclusive zombie MOVE ownership, exact rollback, and five fixed baseline trials.
- Matched vanilla-control/OBSERVE raw baseline samples and a fail-closed analysis script.
- Dedicated-server and integrated-client smoke procedures and retained logs.
- Exact-version hook audit, framework-selection ADR, compatibility matrix, known-upstream-warning ledger, and P0 gate evidence.
- SHA-pinned Windows/Ubuntu CI with clean-runner dependency verification, paired GameTests, fail-closed analysis, and a required-gates job.
- Cross-platform verification metadata for Linux-selected native artifacts plus a narrowly scoped, documented Netty native lock exception that retains strict version and checksum controls.
- Git executable mode `100755` for `gradlew`, so native Unix checkouts retain a directly launchable wrapper.
- Director acceptance of the evidence-backed P0 milestone on 2026-10-11; no A1 implementation or release authorization is implied.

### Not included

- No enhanced navigation, pathfinder replacement, global entity takeover, asynchronous world processing, GOAP, LLM integration, or release-performance claim.
- No public release, tag, package publication, or director-approved project license.

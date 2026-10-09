# Changelog

All notable verified project changes are documented here. Planned features are not listed as shipped behavior.

## 0.1.0-p0 — candidate foundation

### Added

- Official NeoForge 1.21.1 ModDevGradle project scaffold with a pinned Java 21 toolchain, Gradle wrapper checksum, dependency locks, and artifact verification metadata.
- Checkstyle 10 and Java compiler warning gates, plus JaCoCo 100% line/branch verification for deterministic Minecraft-independent domain code.
- Server-side OBSERVE instrumentation with deferred attachment, bounded lifecycle state, non-interference assertions, and explicit shutdown cleanup.
- Eleven unit tests and seven in-game GameTests covering deterministic domain behavior, OBSERVE non-interference, test-only exclusive zombie MOVE ownership, exact rollback, and five fixed baseline trials.
- Matched vanilla-control/OBSERVE raw baseline samples and a fail-closed analysis script.
- Dedicated-server and integrated-client smoke procedures and retained logs.
- Exact-version hook audit, framework-selection ADR, compatibility matrix, known-upstream-warning ledger, and P0 gate evidence.

### Not included

- No enhanced navigation, pathfinder replacement, global entity takeover, asynchronous world processing, GOAP, LLM integration, or release-performance claim.
- No public release, tag, package publication, or director-approved project license.

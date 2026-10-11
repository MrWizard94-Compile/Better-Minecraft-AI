# Section 0 pre-delivery self-audit

1. **Completeness/dependency-first:** implementation and evidence contain no stub, unfinished marker, or broken reference; required wrapper/dependencies are committed, pushed, and identified by immutable SHAs.
2. **Warnings/tests:** Java warning-as-error, Checkstyle, 11 unit tests, 100% MC-free domain line/branch coverage, 14 paired local GameTest executions, dedicated smoke, integrated smoke, and required Windows/Ubuntu CI pass; inherited runtime warnings are owned/investigated rather than suppressed.
3. **Documentation:** README, config, exact hook audit, ADR, baseline, environment, compatibility, risks, rollback, notices, changelog, and required gate table agree with implemented P0-only behavior.
4. **Security/validation:** no secret-pattern hits; benchmark arm/path and override inputs fail closed; server smoke is loopback/online-mode with query/RCON disabled; P0 exposes no command, packet, file upload, or network service.
5. **Performance/resources:** P0 costs are bounded to loaded observed mobs and small identity/motion records; measured samples are retained but explicitly not promoted to scale/performance claims; 2 GiB JVM setting fits the documented host.
6. **Version/stack fidelity:** exact Minecraft 1.21.1, NeoForge 21.1.256, Java 21, ModDevGradle/Parchment/test-tool pins and exact mapped-source hooks are recorded.
7. **Package/reproducibility:** wrapper, locks, and verification metadata are complete; both wrapper forms launch; native Windows/Ubuntu clean-runner gates pass; runtime/source JARs rebuild byte-identically; authored local links/whitespace validate.
8. **IP/invention:** MDK notice retained, framework licenses/pins cited, ARR Sable Pathfinder code not copied, and no project public license is invented. Multi-agent coordination is N/A because no subagents were used.
9. **Review package:** required evidence tree, raw logs/data, hashes, exact file inventory, risks, accepted candidate SHA, director statement, and staged skill-evolution payload exist; binding pack verification passes v5.1.0.
10. **Gate result:** all applicable Section 0 delivery checks pass; the director accepted P0 on 2026-10-11. No tag/release/package installation/P1 work occurred, so the result is `P0 DONE` only and never A1-ready.

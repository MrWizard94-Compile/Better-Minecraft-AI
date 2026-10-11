# minecraft-mod-mcp runtime record

**Date:** 2026-10-11

**Purpose:** Provision a controllable Minecraft 1.21.1 NeoForge fixture for later P1 live acceptance.

**Status:** BLOCKED — fixture construction failure; no BMAI result inferred

## Observed tool state

- MCP server responds and exposes install/launch/process/game-control tools.
- Java discovery found Eclipse Adoptium JDK 21.0.10, matching the project major version.
- Minecraft 1.21.1 with NeoForge is listed as supported.
- Two `install_version(version=1.21.1, loader=neoforge)` calls returned `Error: unexpected end of file`.
- The partial install registered Minecraft 1.21.1 and NeoForge 21.1.172, which does not match the project pin 21.1.256.
- An offline `BmaiMcpTester` account and a 4 GiB windowed launch were requested through the MCP.
- Launch reported PID 3628 and MCP port 9876, but the process exited before `ping` or status could connect.

## Root evidence

The MCP-managed `latest.log` lists ModDev MCP 0.4.3 and NeoForge 21.1.172, then reports missing core targets including `BlockEntityType` and `MappedRegistry` during NeoForge mixin preparation. No BMAI JAR was present, so this is an independent fixture/dependency failure rather than a BMAI runtime failure.

## Acceptance boundary

The MCP fixture earns no build, install, startup, gameplay, or P1 acceptance credit. A later retry must prove all of the following before use as evidence: exact or explicitly compatible loader identity; complete dependency construction; candidate BMAI JAR installed; MCP connection live; title/world entry; queried player/world state; P1 behavior and rollback; clean logs. Project-owned Gradle runs on pinned NeoForge 21.1.256 remain authoritative meanwhile.

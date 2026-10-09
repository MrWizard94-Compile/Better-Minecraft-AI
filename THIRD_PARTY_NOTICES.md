# Third-party notices

Better Minecraft AI's P0 scaffold was derived from the official NeoForge 1.21.1 ModDevGradle MDK at commit `ad911709f06d3bdd5b1a500023a9156999216351`. The MDK template's MIT notice is retained in [`LICENSES/NeoForge-MDK-MIT.txt`](LICENSES/NeoForge-MDK-MIT.txt).

The repository includes the Gradle 9.2.1 wrapper bootstrap JAR and scripts. Gradle is distributed under the Apache License 2.0; its source and license are available from the [Gradle project](https://github.com/gradle/gradle).

Minecraft, NeoForge, ModDevGradle, Parchment, Checkstyle, JaCoCo, JUnit, JSpecify, and their transitive dependencies are resolved from their respective repositories for development and are not copied into this source tree as application source. Exact resolved coordinates and integrity checks are recorded in `gradle.lockfile` and `gradle/verification-metadata.xml`. Their own licenses apply.

AzureCortex, SmartBrainLib, and Sable Pathfinder were evaluated from public metadata/source for ADR-001. No code, assets, or binaries from those projects are copied or bundled. In particular, no Sable Pathfinder code was copied because its published source is marked All Rights Reserved.

This notice does not grant a license to Better Minecraft AI itself. Project metadata currently states `All Rights Reserved` pending a director decision.

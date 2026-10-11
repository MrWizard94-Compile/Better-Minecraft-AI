# Phase P0 build environment

Recorded on 2026-10-09 in the local WPAI workspace. Direct pins come from the project build; transitive versions and SHA-256 component verification are retained in `gradle.lockfile` and `gradle/verification-metadata.xml`.

## Direct pins

| Component | Exact value |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.256 |
| ModDevGradle | 2.0.148 |
| Parchment Minecraft version | 1.21.1 |
| Parchment mappings | 2024.11.17 |
| Gradle wrapper | 9.2.1 binary distribution |
| Java toolchain | 21 |
| Local JDK used | Eclipse Temurin 21.0.10+7 LTS |
| Checkstyle | 10.26.1 |
| JaCoCo | 0.8.15 |
| JUnit Jupiter | 5.14.4 |
| JUnit Platform launcher | 1.14.4 |
| JSpecify | 1.0.1 |
| Foojay toolchain resolver convention | 1.0.0 |

The Gradle distribution URL is `https://services.gradle.org/distributions/gradle-9.2.1-bin.zip`, pinned by SHA-256 `72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f` in `gradle-wrapper.properties`.

## Provenance and retained hashes

| Input | Provenance | SHA-256 / revision |
|---|---|---|
| MDK scaffold | Official `NeoForgeMDKs/MDK-1.21.1-ModDevGradle` | commit `ad911709f06d3bdd5b1a500023a9156999216351` |
| Gradle wrapper JAR | Imported from that official MDK revision | `7D3A4AC4DE1C32B59BC6A4EB8ECB8E612CCD0CF1AE1E99F66902DA64DF296172` |
| Gradle 9.2.1 binary distribution | `services.gradle.org` | `72F44C9F8EBCB1AF43838F45EE5C4AA9C5444898B3468AB3F4AF7B6076C5BC3F` |
| NeoForge 21.1.256 sources JAR | NeoForged Maven | `428CB928348747DE535D8CC59B134C04CA6DB6B55B6AF6BE2477CD966721E800` |
| Empty GameTest structure | Deterministic `tools/GenerateEmptyGameTestStructure.java` output | `DFC0B3121FFF129E4150686FDCB680134B3EA66E763EE139292BF65CFF34E685` |

The MDK revision was the remote default-branch head observed at bootstrap time. The project then upgraded the MDK's direct tool pins only after resolving and compiling them in this workspace.

## Host used for retained P0 evidence

| Host fact | Observed value |
|---|---|
| Runtime OS report | Gradle/ModLauncher: Windows 11 10.0 amd64 |
| Registry OS report | `ProductName=Windows 10 Home`, display version 26H2, build 26300.9457 |
| Architecture | x86-64 |
| PowerShell | 7.6.6 |
| Git | 2.55.0.windows.3 |
| CPU | Intel Core i7-9700, 8 cores / 8 logical processors |
| RAM | 51,409,330,176 bytes installed (approximately 48 GiB) |
| GPU | NVIDIA GeForce GTX 1660 Ti |
| JVM arguments | `-Xmx2G -Dfile.encoding=UTF-8` |
| Gradle mode | daemon, parallel execution, build cache, and configuration cache configured; retained validation commands used `--no-daemon` |

Both OS values are retained because Windows exposes a legacy `ProductName` string in the registry while the Java/Gradle runtime identifies this host as Windows 11. No undocumented edition inference is made from build number.

## Reproduction commands

```powershell
.\gradlew.bat --version
.\gradlew.bat --no-daemon --warning-mode fail printBuildEnvironment
.\gradlew.bat --no-daemon --warning-mode fail clean build
.\gradlew.bat --no-daemon --warning-mode fail dependencies
```

On a Unix-like host, run `chmod +x ./gradlew` once and use the same arguments with `./gradlew`. Native Ubuntu execution is verified by GitHub Actions [run 38021120525](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38021120525).

Dependency verification fails the build when a resolved artifact checksum is absent or mismatched. Dependency locking prevents silent transitive drift except for one documented OS-conditional module: `io.netty:netty-transport-native-epoll` is excluded from the shared lock state because it is selected only by the Linux resolution graph. This is not a version or integrity bypass: Minecraft metadata constrains it strictly to `4.1.97.Final`, both resolved Linux artifacts are covered by SHA-256 verification metadata, and the required Windows/Ubuntu CI matrix exercises both dependency graphs.

Linux-selected LWJGL native classifiers are also explicitly checksum-verified. Newly discovered cross-platform metadata and native hashes were compared between Maven Central and the corresponding authoritative NeoForged, Mojang Libraries, or upstream repository before admission.

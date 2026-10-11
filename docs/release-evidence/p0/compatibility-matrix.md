# Phase P0 compatibility matrix

| Environment / combination | Exact versions | Status | Observed evidence / limitation |
|---|---|---|---|
| Windows wrapper | Gradle 9.2.1, Temurin 21.0.10 | PASS | `gradlew.bat --version` exit 0. |
| POSIX wrapper script on available host | Gradle 9.2.1 via Git Bash | PASS | `bash ./gradlew --version` exit 0. This proves script launch, not a native Linux/macOS filesystem/runtime. |
| Native Ubuntu CI | Workflow `ubuntu-24.04`; accepted candidate `5848143e16a48db2da38cd859f663b845cc7e62b` | PASS | Clean build/dependency verification and both paired GameTest arms passed in [run 38157185309](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38157185309). |
| BMAI vanilla-control GameTest | Minecraft 1.21.1, NeoForge 21.1.256, BMAI 0.1.0-p0; observation forced false | PASS | 7/7; all observer counters zero; 5/5 raw navigation trials. |
| BMAI OBSERVE GameTest | Same, observation forced true | PASS | 7/7; controller identities unchanged; post-tick samples retained; 5/5 navigation trials. |
| Dedicated server, BMAI only | Minecraft 1.21.1, NeoForge 21.1.256, BMAI 0.1.0-p0 | PASS | Loopback/ephemeral port, online mode, ready → standard stop → all dimensions saved. No external client joined. |
| Integrated client/server, BMAI only | Same; dev quick-play world | PASS | Real local player + ready server for 100 active ticks; normal shutdown and saves. No human visual inspection. |
| Sable / Sable Pathfinder absent | No optional dependencies installed | PASS | Server, client, GameTests, and vanilla navigation work with both absent. |
| AzureCortex absent | No dependency | PASS | Build/runtime classpath does not require it. |
| SmartBrainLib absent | No dependency | PASS | Build/runtime classpath does not require it. |
| Bare NeoForge without BMAI | 1.21.1 / 21.1.256 | NOT RUN | P0 control hosts its GameTests inside a loaded but force-disabled BMAI artifact. A fully mod-absent external runner is a later benchmark hardening item. |
| BMAI disabled by common config | `observation_enabled=false` | PARTIAL PASS | Forced-off arm proves no observer attachment/work. Generated common-file restart path is documented; packaged installation toggle was not separately run. |
| BMAI removal from test world | — | NOT RUN | P0 registers no persistent game content, but formal removal/reopen belongs to A1-04. |
| Sable / Sable Pathfinder present | Sable and Pathfinder versions not installed | NOT RUN | Future A5 concern; no ARR code copied. |
| Common pathfinding optimizer | — | NOT RUN | A1 compatibility requirement, not P0. |
| Entity animation/behavior mod | — | NOT RUN | A1 compatibility requirement, not P0. |
| Unknown modded mob / boss / specialist navigator | — | NOT RUN | Production P0 never changes any mob; explicit adapter-exclusion tests remain P1/A1 work. |
| Packaged production JAR in external CurseForge instance | BMAI output hash in manifest | NOT RUN | Dev runtime is verified; no installation/distribution action was authorized. |

`PASS` means observed in the retained environment only. It is not a universal compatibility claim.

# Investigated upstream development-runtime warnings

Phase P0 treats warnings as evidence requiring ownership and investigation. The accepted runtime logs contain no Better Minecraft AI warning, error, fatal, stack trace, or crash. They do contain the following messages emitted by Minecraft/NeoForge development-runtime classes.

No warning is suppressed, filtered, downgraded, or excluded from the retained logs. The project uses the newest accessible NeoForge 1.21.1 pin verified for this work (`21.1.256`). Fixing third-party runtime internals or filing upstream issues would be a separate patch/external communication action and is not silently claimed here.

| Warning | Emits in | Owner/source assessment | P0 impact and disposition |
|---|---|---|---|
| Advanced terminal features unavailable | Gradle-launched Minecraft console on this captured/non-interactive terminal | `StatusConsoleListener` / TerminalConsoleAppender cannot enable an interactive advanced console for the captured process. BMAI does not configure the console implementation. | Normal plain-text logging and stdin `stop` both work. Retained in command output, not suppressed. Use an interactive terminal for enhanced console features. |
| Command dispatcher teleport ambiguity | GameTest, dedicated, integrated client | `net.minecraft.commands.Commands` logs the known overlap among vanilla teleport argument branches while the command tree is constructed. BMAI registers no commands in P0. | Startup continues; no BMAI behavior path is involved. Retained, not suppressed. Re-evaluate after a Minecraft/NeoForge update. |
| `.mcassetsroot` URL uses unexpected `union:` schema | GameTest, dedicated, integrated client | `net.minecraft.server.packs.VanillaPackResourcesBuilder` sees ModDevGradle/NeoForge's development union-filesystem URL for the generated runtime artifact. BMAI neither constructs nor rewrites this URL. | Resources load and all runtime gates continue. Retained, not suppressed. Validate packaged-jar runs separately before any release. |
| Missing goat-horn sound events | Integrated client only | `net.minecraft.client.sounds.SoundEngine` reports two vanilla sound-event resources during development client initialization. BMAI has no sounds or resource-pack override. | Local player joins and 100-tick integrated smoke passes. Retained, not suppressed. Recheck a production-like packaged client before release. |
| Entity translucent emissive shader cannot find `Sampler2` | Integrated client only | `net.minecraft.client.renderer.ShaderInstance` reports a vanilla/NeoForge development-renderer shader declaration mismatch. BMAI has no shader, rendering code, or resource pack. | World renders long enough to join and complete the smoke hook. Retained, not suppressed. A visual/manual release check remains mandatory in a later phase. |

## Checks performed

1. Searched each accepted log for `WARN`, `ERROR`, `FATAL`, exceptions, BMAI markers, readiness, and shutdown/save markers.
2. Confirmed every warning logger belongs to Minecraft/NeoForge classes and no warning is emitted by `com.mrwizard94.betterminecraftai`.
3. Removed comments from the generated client-options input after Minecraft warned about them; that project-owned warning is absent from the accepted integrated log.
4. Kept the build at `--warning-mode fail` and Java compilation at `-Xlint:all -Werror`; no build warning is waived.
5. Confirmed accepted runtime logs contain no `ERROR` or `FATAL` entry. This check is repeated in the final evidence validation.

The correct long-term action is to reassess these messages against updated upstream artifacts and a packaged client/server before release, not to add blanket logger filters.

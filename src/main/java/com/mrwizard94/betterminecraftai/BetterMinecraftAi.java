package com.mrwizard94.betterminecraftai;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge entry point for the observation-only P0 foundation.
 */
@Mod(BetterMinecraftAi.MOD_ID)
public final class BetterMinecraftAi {
    /** Stable working mod ID; changing it later requires save migration. */
    public static final String MOD_ID = "better_minecraft_ai";
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ObservationService OBSERVATION_SERVICE = new ObservationService();

    /**
     * Registers the validated configuration and the non-invasive server observer.
     *
     * @param modContainer NeoForge-owned mod container
     */
    public BetterMinecraftAi(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, BetterAiConfig.SPEC);
        NeoForge.EVENT_BUS.register(OBSERVATION_SERVICE);
        LOGGER.info("Better Minecraft AI P0 initialized in observation-only mode");
    }

    static ObservationService observationService() {
        return OBSERVATION_SERVICE;
    }
}

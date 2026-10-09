package com.mrwizard94.betterminecraftai;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * P0 common configuration. Only implemented behavior is exposed.
 */
final class BetterAiConfig {
    static final ModConfigSpec SPEC;
    static final ModConfigSpec.BooleanValue OBSERVATION_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        OBSERVATION_ENABLED = builder
                .comment(
                        "Collect read-only server-side mob lifecycle and movement metrics.",
                        "This setting never grants Better Minecraft AI movement ownership in P0.")
                .define("observation_enabled", true);
        SPEC = builder.build();
    }

    private BetterAiConfig() {
    }
}

package com.mrwizard94.betterminecraftai;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Development-only exit hook for a reproducible unattended integrated-server smoke run.
 */
@EventBusSubscriber(modid = BetterMinecraftAi.MOD_ID, value = Dist.CLIENT)
final class P0IntegratedClientSmoke {
    static final String ENABLED_PROPERTY = "better_minecraft_ai.integrated_smoke";
    private static final int REQUIRED_ACTIVE_TICKS = 100;
    private static int activeTicks;
    private static boolean completed;

    private P0IntegratedClientSmoke() {
    }

    /**
     * Waits for a real local player and ready integrated server, then requests normal client shutdown.
     *
     * @param event completed client tick
     */
    @SubscribeEvent
    public static void onClientPostTick(ClientTickEvent.Post event) {
        if (completed || FMLEnvironment.production || !Boolean.getBoolean(ENABLED_PROPERTY)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null || minecraft.player == null || !server.isReady()) {
            activeTicks = 0;
            return;
        }

        activeTicks++;
        if (activeTicks >= REQUIRED_ACTIVE_TICKS) {
            completed = true;
            BetterMinecraftAi.LOGGER.info(
                    "BMAI_P0_INTEGRATED_SMOKE_PASS player={} ticks={} serverReady={}",
                    minecraft.player.getGameProfile().getName(),
                    activeTicks,
                    server.isReady());
            minecraft.stop();
        }
    }
}

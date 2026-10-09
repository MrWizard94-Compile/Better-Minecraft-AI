package com.mrwizard94.betterminecraftai;

import com.mrwizard94.betterminecraftai.domain.MotionState;
import com.mrwizard94.betterminecraftai.domain.ObservationOverride;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Server-thread-only P0 observer. It records lifecycle and motion facts without issuing game commands.
 */
final class ObservationService {
    static final String ENABLED_PROPERTY = "better_minecraft_ai.observation.enabled";
    private static final double MOVEMENT_EPSILON_SQUARED = 1.0E-6;

    private final Optional<ObservationOverride> enabledOverride;
    private final Set<UUID> pending = new HashSet<>();
    private final Map<UUID, ObservedMob> active = new HashMap<>();
    private long joinCount;
    private long attachCount;
    private long sampleCount;
    private long idleSampleCount;
    private long stationarySampleCount;
    private long movingSampleCount;
    private long handlerNanos;

    ObservationService() {
        this.enabledOverride = ObservationOverride.parse(
                Optional.ofNullable(System.getProperty(ENABLED_PROPERTY)));
    }

    /**
     * Records only the entity identity. World and chunk state are deliberately not inspected here.
     *
     * @param event entity join event, which can precede chunk FULL state
     */
    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!isEnabled() || event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        long startedAt = System.nanoTime();
        pending.add(mob.getUUID());
        joinCount++;
        handlerNanos += System.nanoTime() - startedAt;
    }

    /**
     * Attaches on the first completed server tick and samples only already-computed entity state.
     *
     * @param event post-tick event
     */
    @SubscribeEvent
    public void onEntityPostTick(EntityTickEvent.Post event) {
        if (!isEnabled() || !(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }

        long startedAt = System.nanoTime();
        UUID entityId = mob.getUUID();
        ObservedMob observed = active.get(entityId);
        if (observed == null && pending.remove(entityId)) {
            observed = ObservedMob.attach(mob);
            active.put(entityId, observed);
            attachCount++;
        }
        if (observed != null) {
            MotionState state = observed.sample(mob);
            sampleCount++;
            switch (state) {
                case IDLE -> idleSampleCount++;
                case STATIONARY -> stationarySampleCount++;
                case MOVING -> movingSampleCount++;
            }
        }
        handlerNanos += System.nanoTime() - startedAt;
    }

    /**
     * Releases observation state when an entity leaves its level.
     *
     * @param event entity leave event
     */
    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!isEnabled() || event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        long startedAt = System.nanoTime();
        pending.remove(mob.getUUID());
        active.remove(mob.getUUID());
        handlerNanos += System.nanoTime() - startedAt;
    }

    /**
     * Emits one bounded summary and drops all server-scoped state.
     *
     * @param event server stopping event
     */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        BetterMinecraftAi.LOGGER.info(
                "BMAI_P0_OBSERVE_SUMMARY enabled={} joined={} attached={} samples={} idle={} stationary={} "
                        + "moving={} pending={} active={} handlerNanos={}",
                isEnabled(),
                joinCount,
                attachCount,
                sampleCount,
                idleSampleCount,
                stationarySampleCount,
                movingSampleCount,
                pending.size(),
                active.size(),
                handlerNanos);
        pending.clear();
        active.clear();
    }

    boolean isEnabled() {
        return enabledOverride
                .map(ObservationOverride::forcedValue)
                .orElseGet(BetterAiConfig.OBSERVATION_ENABLED::getAsBoolean);
    }

    boolean isObserving(UUID entityId) {
        return active.containsKey(entityId);
    }

    long entitySampleCount(UUID entityId) {
        ObservedMob observed = active.get(entityId);
        return observed == null ? 0L : observed.sampleCount();
    }

    boolean controllersMatch(UUID entityId, Mob mob) {
        ObservedMob observed = active.get(entityId);
        return observed != null && observed.controllersMatch(mob);
    }

    long handlerNanos() {
        return handlerNanos;
    }

    private static final class ObservedMob {
        private final int navigationIdentity;
        private final int goalSelectorIdentity;
        private final int targetSelectorIdentity;
        private double previousX;
        private double previousY;
        private double previousZ;
        private long sampleCount;

        private ObservedMob(Mob mob) {
            this.navigationIdentity = System.identityHashCode(mob.getNavigation());
            this.goalSelectorIdentity = System.identityHashCode(mob.goalSelector);
            this.targetSelectorIdentity = System.identityHashCode(mob.targetSelector);
            this.previousX = mob.getX();
            this.previousY = mob.getY();
            this.previousZ = mob.getZ();
        }

        static ObservedMob attach(Mob mob) {
            return new ObservedMob(mob);
        }

        MotionState sample(Mob mob) {
            double deltaX = mob.getX() - previousX;
            double deltaY = mob.getY() - previousY;
            double deltaZ = mob.getZ() - previousZ;
            double distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            MotionState state = MotionState.classify(
                    mob.getNavigation().isInProgress(), distanceSquared, MOVEMENT_EPSILON_SQUARED);
            previousX = mob.getX();
            previousY = mob.getY();
            previousZ = mob.getZ();
            sampleCount++;
            return state;
        }

        long sampleCount() {
            return sampleCount;
        }

        boolean controllersMatch(Mob mob) {
            return navigationIdentity == System.identityHashCode(mob.getNavigation())
                    && goalSelectorIdentity == System.identityHashCode(mob.goalSelector)
                    && targetSelectorIdentity == System.identityHashCode(mob.targetSelector);
        }
    }
}

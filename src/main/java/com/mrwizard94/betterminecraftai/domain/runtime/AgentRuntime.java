package com.mrwizard94.betterminecraftai.domain.runtime;

import com.mrwizard94.betterminecraftai.domain.profile.AiMode;
import com.mrwizard94.betterminecraftai.domain.profile.BehaviorProfile;
import java.util.Objects;
import java.util.UUID;

/**
 * Lifecycle-scoped Minecraft-independent state for one observed entity.
 *
 * <p>The runtime deliberately retains only the stable UUID and immutable profile. Live Minecraft entity and
 * level references remain outside this domain object.</p>
 */
public final class AgentRuntime {
    private final UUID entityId;
    private final ModeTransitionController modeController;
    private BehaviorProfile profile;

    /**
     * Creates a vanilla-owned runtime. Call {@link #reconcileProfile(BehaviorProfile)} to request its profile mode.
     *
     * @param entityId stable entity UUID
     * @param profile initial immutable profile
     */
    public AgentRuntime(UUID entityId, BehaviorProfile profile) {
        this.entityId = Objects.requireNonNull(entityId, "entityId");
        this.profile = Objects.requireNonNull(profile, "profile");
        if (profile.effectiveMode() == AiMode.ENHANCED) {
            throw new IllegalArgumentException(
                    "A runtime must start vanilla-owned and request ENHANCED through reconcileProfile");
        }
        modeController = new ModeTransitionController(profile.effectiveMode());
    }

    /**
     * Applies a new immutable profile and returns any exact lease operation required by its effective mode.
     *
     * <p>The transition request occurs before the stored profile changes, so a rejected in-flight request leaves the
     * previous profile intact.</p>
     *
     * @param next next resolved profile for the same entity type
     * @return required lease action
     */
    public TransitionAction reconcileProfile(BehaviorProfile next) {
        Objects.requireNonNull(next, "next");
        if (!profile.entityType().equals(next.entityType())) {
            throw new IllegalArgumentException("An entity runtime cannot change entity type");
        }
        TransitionAction action = modeController.requestMode(next.effectiveMode());
        profile = next;
        return action;
    }

    /** @return stable entity UUID */
    public UUID entityId() {
        return entityId;
    }

    /** @return current immutable behavior profile */
    public BehaviorProfile profile() {
        return profile;
    }

    /** @return controller used to complete exact lease actions on the server thread */
    public ModeTransitionController modeController() {
        return modeController;
    }
}

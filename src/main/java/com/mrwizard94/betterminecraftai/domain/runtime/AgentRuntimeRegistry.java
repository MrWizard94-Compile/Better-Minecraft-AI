package com.mrwizard94.betterminecraftai.domain.runtime;

import com.mrwizard94.betterminecraftai.domain.profile.BehaviorProfile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Bounded-by-loaded-entities UUID registry with explicit cleanup invariants.
 */
public final class AgentRuntimeRegistry {
    private final Map<UUID, AgentRuntime> runtimes = new HashMap<>();

    /**
     * Attaches one runtime and rejects duplicate lifecycle attachment.
     *
     * @param entityId stable entity UUID
     * @param profile resolved immutable profile
     * @return newly attached runtime
     */
    public AgentRuntime attach(UUID entityId, BehaviorProfile profile) {
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(profile, "profile");
        if (runtimes.containsKey(entityId)) {
            throw new IllegalStateException("Entity runtime already attached: " + entityId);
        }
        AgentRuntime runtime = new AgentRuntime(entityId, profile);
        runtimes.put(entityId, runtime);
        return runtime;
    }

    /**
     * Looks up a lifecycle-scoped runtime without exposing the mutable backing map.
     *
     * @param entityId stable entity UUID
     * @return current runtime, if attached
     */
    public Optional<AgentRuntime> find(UUID entityId) {
        return Optional.ofNullable(runtimes.get(Objects.requireNonNull(entityId, "entityId")));
    }

    /**
     * Removes a runtime only after integration code has restored vanilla ownership.
     *
     * @param entityId stable entity UUID
     * @return removed runtime, if one was attached
     */
    public Optional<AgentRuntime> removeVanillaOwned(UUID entityId) {
        Objects.requireNonNull(entityId, "entityId");
        AgentRuntime runtime = runtimes.get(entityId);
        if (runtime == null) {
            return Optional.empty();
        }
        requireVanillaOwned(runtime);
        runtimes.remove(entityId);
        return Optional.of(runtime);
    }

    /**
     * Returns an immutable snapshot for server-stop cleanup iteration.
     *
     * @return current runtime values
     */
    public List<AgentRuntime> snapshot() {
        return List.copyOf(runtimes.values());
    }

    /**
     * Clears the registry only when every integration has already restored vanilla ownership.
     */
    public void clearVanillaOwned() {
        runtimes.values().forEach(AgentRuntimeRegistry::requireVanillaOwned);
        runtimes.clear();
    }

    /** @return number of lifecycle-scoped runtimes */
    public int size() {
        return runtimes.size();
    }

    private static void requireVanillaOwned(AgentRuntime runtime) {
        NavigationOwnership ownership = runtime.modeController().snapshot().ownership();
        if (ownership != NavigationOwnership.VANILLA_OWNED) {
            throw new IllegalStateException(
                    "Runtime must restore vanilla ownership before cleanup: " + runtime.entityId());
        }
    }
}

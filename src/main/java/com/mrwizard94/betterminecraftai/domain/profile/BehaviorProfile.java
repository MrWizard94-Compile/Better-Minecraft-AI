package com.mrwizard94.betterminecraftai.domain.profile;

import java.util.List;
import java.util.Objects;

/**
 * Fully resolved immutable behavior profile with human-readable decision provenance.
 *
 * @param entityType canonical entity type
 * @param requestedMode highest-precedence requested mode before capability gating
 * @param effectiveMode safe mode the runtime may activate
 * @param navigationEnhancementEligible whether the current release certifies a navigation adapter for the type
 * @param source rule responsible for the final result
 * @param provenanceTrace ordered explanation of the rules that were evaluated
 */
public record BehaviorProfile(
        EntityTypeKey entityType,
        AiMode requestedMode,
        AiMode effectiveMode,
        boolean navigationEnhancementEligible,
        ProfileSource source,
        List<String> provenanceTrace) {
    /**
     * Enforces the core safety invariant and makes the provenance collection immutable.
     */
    public BehaviorProfile {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(requestedMode, "requestedMode");
        Objects.requireNonNull(effectiveMode, "effectiveMode");
        Objects.requireNonNull(source, "source");
        provenanceTrace = List.copyOf(Objects.requireNonNull(provenanceTrace, "provenanceTrace"));
        if (provenanceTrace.isEmpty()) {
            throw new IllegalArgumentException("A behavior profile must explain its resolution");
        }
        if (effectiveMode == AiMode.ENHANCED && !navigationEnhancementEligible) {
            throw new IllegalArgumentException("ENHANCED requires a certified navigation adapter");
        }
    }
}

package com.mrwizard94.betterminecraftai.domain.profile;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable, already-parsed facts used to resolve one entity behavior profile.
 *
 * @param entityType canonical entity type
 * @param safetyExclusions hard exclusions discovered by the Minecraft compatibility boundary
 * @param adminDenied whether the entity type is explicitly denied by the administrator
 * @param entityOverride optional per-entity mode override
 * @param tagMode optional data/tag-selected mode
 * @param globalMode conservative global request
 */
public record ProfileRequest(
        EntityTypeKey entityType,
        Set<SafetyExclusion> safetyExclusions,
        boolean adminDenied,
        Optional<AiMode> entityOverride,
        Optional<AiMode> tagMode,
        AiMode globalMode) {
    /**
     * Defensively copies all collection-like inputs so resolution cannot change underneath a runtime.
     */
    public ProfileRequest {
        Objects.requireNonNull(entityType, "entityType");
        safetyExclusions = Set.copyOf(Objects.requireNonNull(safetyExclusions, "safetyExclusions"));
        Objects.requireNonNull(entityOverride, "entityOverride");
        Objects.requireNonNull(tagMode, "tagMode");
        Objects.requireNonNull(globalMode, "globalMode");
    }
}

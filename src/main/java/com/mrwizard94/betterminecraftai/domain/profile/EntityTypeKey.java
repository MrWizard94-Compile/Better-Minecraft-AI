package com.mrwizard94.betterminecraftai.domain.profile;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Canonical namespaced entity-type identifier at the Minecraft-independent profile boundary.
 *
 * @param value lower-case {@code namespace:path} identifier
 */
public record EntityTypeKey(String value) {
    private static final Pattern CANONICAL_PATTERN =
            Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");

    /**
     * Validates the canonical identifier form expected from a Minecraft {@code ResourceLocation}.
     */
    public EntityTypeKey {
        Objects.requireNonNull(value, "value");
        if (!CANONICAL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Entity type must be a canonical namespace:path identifier: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

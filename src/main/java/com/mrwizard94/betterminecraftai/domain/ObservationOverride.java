package com.mrwizard94.betterminecraftai.domain;

import java.util.Locale;
import java.util.Optional;

/**
 * Validated development-time override for P0 observation instrumentation.
 */
public enum ObservationOverride {
    /** Force observation on. */
    ENABLED,
    /** Force observation off to form the matched vanilla-control benchmark arm. */
    DISABLED;

    /**
     * Parses the optional system-property value without silently accepting malformed input.
     *
     * @param rawValue optional raw system-property value
     * @return an empty value when no override was supplied
     * @throws IllegalArgumentException when the supplied value is not {@code true} or {@code false}
     */
    public static Optional<ObservationOverride> parse(Optional<String> rawValue) {
        if (rawValue.isEmpty()) {
            return Optional.empty();
        }

        return switch (rawValue.orElseThrow().trim().toLowerCase(Locale.ROOT)) {
            case "true" -> Optional.of(ENABLED);
            case "false" -> Optional.of(DISABLED);
            default -> throw new IllegalArgumentException(
                    "better_minecraft_ai.observation.enabled must be true or false");
        };
    }

    /**
     * Returns the forced value represented by this override.
     *
     * @return the forced Boolean value
     */
    public boolean forcedValue() {
        return this == ENABLED;
    }
}

package com.mrwizard94.betterminecraftai.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ObservationOverrideTest {
    @Test
    void leavesConfigurationInControlWhenOverrideIsAbsent() {
        assertTrue(ObservationOverride.parse(Optional.empty()).isEmpty());
    }

    @Test
    void parsesCaseInsensitiveEnabledOverride() {
        ObservationOverride override = ObservationOverride.parse(Optional.of(" TrUe ")).orElseThrow();
        assertTrue(override.forcedValue());
    }

    @Test
    void parsesDisabledOverride() {
        ObservationOverride override = ObservationOverride.parse(Optional.of("false")).orElseThrow();
        assertFalse(override.forcedValue());
    }

    @Test
    void rejectsMalformedOverride() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ObservationOverride.parse(Optional.of("sometimes")));
    }
}

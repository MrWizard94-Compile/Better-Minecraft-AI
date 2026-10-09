package com.mrwizard94.betterminecraftai.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MotionStateTest {
    @Test
    void returnsIdleWhenVanillaNavigationHasNoPath() {
        assertEquals(MotionState.IDLE, MotionState.classify(false, 4.0, 0.01));
    }

    @Test
    void returnsStationaryBelowMovementThreshold() {
        assertEquals(MotionState.STATIONARY, MotionState.classify(true, 0.009, 0.01));
    }

    @Test
    void returnsMovingAtMovementThreshold() {
        assertEquals(MotionState.MOVING, MotionState.classify(true, 0.01, 0.01));
    }

    @Test
    void rejectsNonFiniteDistance() {
        assertThrows(IllegalArgumentException.class, () -> MotionState.classify(true, Double.NaN, 0.01));
    }

    @Test
    void rejectsNegativeDistance() {
        assertThrows(IllegalArgumentException.class, () -> MotionState.classify(true, -0.01, 0.01));
    }

    @Test
    void rejectsNonFiniteThreshold() {
        assertThrows(IllegalArgumentException.class, () -> MotionState.classify(true, 0.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void rejectsNonPositiveThreshold() {
        assertThrows(IllegalArgumentException.class, () -> MotionState.classify(true, 0.0, 0.0));
    }
}

package com.mrwizard94.betterminecraftai.domain;

/**
 * Coarse motion state derived from one observation interval.
 */
public enum MotionState {
    /** The vanilla navigator has no active path. */
    IDLE,
    /** The vanilla navigator has a path but the entity did not clear the movement epsilon. */
    STATIONARY,
    /** The vanilla navigator has a path and the entity cleared the movement epsilon. */
    MOVING;

    /**
     * Classifies a read-only navigation sample.
     *
     * @param navigationInProgress whether vanilla reports an active path
     * @param distanceSquared squared movement since the preceding sample
     * @param movementEpsilonSquared minimum squared movement counted as progress
     * @return the classified state
     * @throws IllegalArgumentException when either numeric input is invalid
     */
    public static MotionState classify(
            boolean navigationInProgress,
            double distanceSquared,
            double movementEpsilonSquared) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0.0) {
            throw new IllegalArgumentException("distanceSquared must be finite and non-negative");
        }
        if (!Double.isFinite(movementEpsilonSquared) || movementEpsilonSquared <= 0.0) {
            throw new IllegalArgumentException("movementEpsilonSquared must be finite and positive");
        }
        if (!navigationInProgress) {
            return IDLE;
        }
        return distanceSquared >= movementEpsilonSquared ? MOVING : STATIONARY;
    }
}

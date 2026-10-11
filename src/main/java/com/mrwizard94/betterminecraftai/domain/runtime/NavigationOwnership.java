package com.mrwizard94.betterminecraftai.domain.runtime;

/**
 * Exclusive navigation ownership state for one lifecycle-scoped entity runtime.
 */
public enum NavigationOwnership {
    /** Vanilla goals and navigation retain all movement authority. */
    VANILLA_OWNED,
    /** A certified adapter holds the only movement lease. */
    ENHANCED_OWNED,
    /** Lease acquisition or release is in progress on the server thread. */
    TRANSITIONING
}

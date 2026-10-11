package com.mrwizard94.betterminecraftai.domain.runtime;

/**
 * Stable diagnostic reason for restoring vanilla navigation ownership.
 */
public enum FallbackReason {
    /** Configuration or a profile reload disabled enhanced ownership. */
    CONFIG_DISABLED,
    /** Vanilla rejected the route or lease request. */
    LEASE_REJECTED,
    /** Another navigation owner made exclusive control unsafe. */
    OWNER_CONFLICT,
    /** Entity unload, death, dimension change, or server shutdown ended the runtime. */
    ENTITY_LIFECYCLE
}

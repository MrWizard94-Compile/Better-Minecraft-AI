package com.mrwizard94.betterminecraftai.domain.runtime;

/**
 * Side effect that the Minecraft integration must complete for a requested mode change.
 */
public enum TransitionAction {
    /** No navigation lease operation is required. */
    NONE,
    /** Acquire the exact C0 navigation lease, then confirm acquisition. */
    ACQUIRE_C0_LEASE,
    /** Release the exact held lease, then confirm vanilla restoration. */
    RELEASE_C0_LEASE,
    /** Cancel movement and restore vanilla ownership after a failed or invalid condition. */
    RESTORE_VANILLA_FALLBACK
}

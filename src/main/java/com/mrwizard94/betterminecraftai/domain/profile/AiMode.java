package com.mrwizard94.betterminecraftai.domain.profile;

/**
 * Requested execution mode for one resolved behavior profile.
 */
public enum AiMode {
    /** Collect diagnostics without competing with vanilla AI. */
    OBSERVE,
    /** Run only non-competitive helpers while vanilla retains movement ownership. */
    ASSIST,
    /** Permit a certified adapter to request exclusive navigation ownership. */
    ENHANCED
}

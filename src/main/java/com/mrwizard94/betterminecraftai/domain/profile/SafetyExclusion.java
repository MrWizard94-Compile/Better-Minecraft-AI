package com.mrwizard94.betterminecraftai.domain.profile;

/**
 * Hard safety facts that prevent a profile from acquiring navigation ownership.
 */
public enum SafetyExclusion {
    /** Boss lifecycle and encounter logic are not C0-compatible. */
    BOSS,
    /** Brain-scheduled behavior needs a separate adapter. */
    BRAIN_CONTROLLED,
    /** Flying, swimming, climbing, or another specialized navigator is outside C0. */
    SPECIALIZED_NAVIGATION,
    /** Another integration already owns or claims the navigation boundary. */
    EXTERNAL_OWNER
}

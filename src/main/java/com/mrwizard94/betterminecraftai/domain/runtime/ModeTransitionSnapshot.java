package com.mrwizard94.betterminecraftai.domain.runtime;

import com.mrwizard94.betterminecraftai.domain.profile.AiMode;
import java.util.Optional;

/**
 * Immutable diagnostic view of one mode-transition controller.
 *
 * @param requestedMode latest requested mode
 * @param activeMode currently active mode
 * @param ownership current navigation ownership
 * @param generation monotonically increasing state-change generation
 * @param lastFallbackReason most recent forced fallback, if any
 */
public record ModeTransitionSnapshot(
        AiMode requestedMode,
        AiMode activeMode,
        NavigationOwnership ownership,
        long generation,
        Optional<FallbackReason> lastFallbackReason) {
}

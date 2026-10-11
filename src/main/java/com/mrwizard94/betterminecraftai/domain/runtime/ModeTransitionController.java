package com.mrwizard94.betterminecraftai.domain.runtime;

import com.mrwizard94.betterminecraftai.domain.profile.AiMode;
import java.util.Objects;
import java.util.Optional;

/**
 * Server-thread state machine that separates mode requests from exact lease side effects.
 */
public final class ModeTransitionController {
    private AiMode requestedMode;
    private AiMode activeMode;
    private NavigationOwnership ownership = NavigationOwnership.VANILLA_OWNED;
    private long generation;
    private Optional<FallbackReason> lastFallbackReason = Optional.empty();

    /**
     * Creates a vanilla-owned controller in an observation-compatible mode.
     *
     * @param initialMode initial OBSERVE or ASSIST mode
     */
    public ModeTransitionController(AiMode initialMode) {
        Objects.requireNonNull(initialMode, "initialMode");
        if (initialMode == AiMode.ENHANCED) {
            throw new IllegalArgumentException("A runtime cannot start ENHANCED before acquiring a lease");
        }
        requestedMode = initialMode;
        activeMode = initialMode;
    }

    /**
     * Begins a stable-state mode change and returns the exact integration action required.
     *
     * @param desiredMode desired safe profile mode
     * @return required lease action
     */
    public TransitionAction requestMode(AiMode desiredMode) {
        Objects.requireNonNull(desiredMode, "desiredMode");
        requireStable();
        lastFallbackReason = Optional.empty();

        if (desiredMode == activeMode) {
            requestedMode = desiredMode;
            return TransitionAction.NONE;
        }
        if (desiredMode == AiMode.ENHANCED) {
            requestedMode = desiredMode;
            ownership = NavigationOwnership.TRANSITIONING;
            generation++;
            return TransitionAction.ACQUIRE_C0_LEASE;
        }
        if (ownership == NavigationOwnership.ENHANCED_OWNED) {
            requestedMode = desiredMode;
            ownership = NavigationOwnership.TRANSITIONING;
            generation++;
            return TransitionAction.RELEASE_C0_LEASE;
        }

        requestedMode = desiredMode;
        activeMode = desiredMode;
        generation++;
        return TransitionAction.NONE;
    }

    /** Completes a successful exact-lease acquisition. */
    public void completeAcquisition() {
        if (ownership != NavigationOwnership.TRANSITIONING || requestedMode != AiMode.ENHANCED) {
            throw new IllegalStateException("No enhanced lease acquisition is pending");
        }
        activeMode = AiMode.ENHANCED;
        ownership = NavigationOwnership.ENHANCED_OWNED;
    }

    /** Completes an exact-lease release after vanilla state has been restored. */
    public void completeRelease() {
        if (ownership != NavigationOwnership.TRANSITIONING
                || requestedMode == AiMode.ENHANCED
                || lastFallbackReason.isPresent()) {
            throw new IllegalStateException("No enhanced lease release is pending");
        }
        activeMode = requestedMode;
        ownership = NavigationOwnership.VANILLA_OWNED;
    }

    /**
     * Requests the conservative OBSERVE fallback after a failed or invalid ownership condition.
     *
     * <p>If enhanced ownership may exist, integration must cancel movement, remove only the exact held lease, and
     * call {@link #completeFallback()} before lifecycle cleanup is allowed.</p>
     *
     * @param reason stable diagnostic reason
     * @return exact restoration action required by the integration
     */
    public TransitionAction requestFallback(FallbackReason reason) {
        FallbackReason validatedReason = Objects.requireNonNull(reason, "reason");
        if (ownership == NavigationOwnership.TRANSITIONING && lastFallbackReason.isPresent()) {
            throw new IllegalStateException("A vanilla fallback restoration is already pending");
        }
        requestedMode = AiMode.OBSERVE;
        generation++;
        lastFallbackReason = Optional.of(validatedReason);
        if (ownership == NavigationOwnership.VANILLA_OWNED) {
            activeMode = AiMode.OBSERVE;
            return TransitionAction.NONE;
        }
        ownership = NavigationOwnership.TRANSITIONING;
        return TransitionAction.RESTORE_VANILLA_FALLBACK;
    }

    /** Completes fallback only after integration has restored vanilla navigation ownership. */
    public void completeFallback() {
        if (ownership != NavigationOwnership.TRANSITIONING || lastFallbackReason.isEmpty()) {
            throw new IllegalStateException("No vanilla fallback restoration is pending");
        }
        activeMode = AiMode.OBSERVE;
        ownership = NavigationOwnership.VANILLA_OWNED;
    }

    /**
     * Returns an immutable diagnostic snapshot.
     *
     * @return current state
     */
    public ModeTransitionSnapshot snapshot() {
        return new ModeTransitionSnapshot(
                requestedMode,
                activeMode,
                ownership,
                generation,
                lastFallbackReason);
    }

    private void requireStable() {
        if (ownership == NavigationOwnership.TRANSITIONING) {
            throw new IllegalStateException("Cannot request another mode while a lease transition is incomplete");
        }
    }
}

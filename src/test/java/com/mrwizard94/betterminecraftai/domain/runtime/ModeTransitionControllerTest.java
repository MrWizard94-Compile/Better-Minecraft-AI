package com.mrwizard94.betterminecraftai.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mrwizard94.betterminecraftai.domain.profile.AiMode;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModeTransitionControllerTest {
    @Test
    void startsVanillaOwnedAndRejectsUnleasedEnhancedConstruction() {
        ModeTransitionController controller = new ModeTransitionController(AiMode.OBSERVE);

        assertSnapshot(
                controller,
                AiMode.OBSERVE,
                AiMode.OBSERVE,
                NavigationOwnership.VANILLA_OWNED,
                0L,
                Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> new ModeTransitionController(AiMode.ENHANCED));
        assertThrows(NullPointerException.class, () -> new ModeTransitionController(null));
    }

    @Test
    void switchesBetweenVanillaOwnedNonCompetitiveModesWithoutLeaseWork() {
        ModeTransitionController controller = new ModeTransitionController(AiMode.OBSERVE);

        assertEquals(TransitionAction.NONE, controller.requestMode(AiMode.ASSIST));
        assertSnapshot(
                controller,
                AiMode.ASSIST,
                AiMode.ASSIST,
                NavigationOwnership.VANILLA_OWNED,
                1L,
                Optional.empty());
        assertEquals(TransitionAction.NONE, controller.requestMode(AiMode.ASSIST));
        assertSnapshot(
                controller,
                AiMode.ASSIST,
                AiMode.ASSIST,
                NavigationOwnership.VANILLA_OWNED,
                1L,
                Optional.empty());
    }

    @Test
    void acquiresAndReleasesExclusiveLeaseInExplicitSteps() {
        ModeTransitionController controller = new ModeTransitionController(AiMode.OBSERVE);

        assertEquals(TransitionAction.ACQUIRE_C0_LEASE, controller.requestMode(AiMode.ENHANCED));
        assertSnapshot(
                controller,
                AiMode.ENHANCED,
                AiMode.OBSERVE,
                NavigationOwnership.TRANSITIONING,
                1L,
                Optional.empty());
        assertThrows(IllegalStateException.class, () -> controller.requestMode(AiMode.OBSERVE));

        controller.completeAcquisition();
        assertSnapshot(
                controller,
                AiMode.ENHANCED,
                AiMode.ENHANCED,
                NavigationOwnership.ENHANCED_OWNED,
                1L,
                Optional.empty());
        assertEquals(TransitionAction.NONE, controller.requestMode(AiMode.ENHANCED));

        assertEquals(TransitionAction.RELEASE_C0_LEASE, controller.requestMode(AiMode.ASSIST));
        assertSnapshot(
                controller,
                AiMode.ASSIST,
                AiMode.ENHANCED,
                NavigationOwnership.TRANSITIONING,
                2L,
                Optional.empty());
        controller.completeRelease();
        assertSnapshot(
                controller,
                AiMode.ASSIST,
                AiMode.ASSIST,
                NavigationOwnership.VANILLA_OWNED,
                2L,
                Optional.empty());
    }

    @Test
    void rejectsCompletionForWrongTransitionDirectionOrStableState() {
        ModeTransitionController stable = new ModeTransitionController(AiMode.OBSERVE);
        assertThrows(IllegalStateException.class, stable::completeAcquisition);
        assertThrows(IllegalStateException.class, stable::completeRelease);
        assertThrows(IllegalStateException.class, stable::completeFallback);

        ModeTransitionController acquiring = new ModeTransitionController(AiMode.OBSERVE);
        acquiring.requestMode(AiMode.ENHANCED);
        assertThrows(IllegalStateException.class, acquiring::completeRelease);
        assertThrows(IllegalStateException.class, acquiring::completeFallback);

        ModeTransitionController releasing = new ModeTransitionController(AiMode.OBSERVE);
        releasing.requestMode(AiMode.ENHANCED);
        releasing.completeAcquisition();
        releasing.requestMode(AiMode.OBSERVE);
        assertThrows(IllegalStateException.class, releasing::completeAcquisition);
    }

    @Test
    void fallbackAlwaysRestoresObserveAndRecordsReason() {
        ModeTransitionController controller = new ModeTransitionController(AiMode.OBSERVE);
        controller.requestMode(AiMode.ENHANCED);
        controller.completeAcquisition();

        assertEquals(
                TransitionAction.RESTORE_VANILLA_FALLBACK,
                controller.requestFallback(FallbackReason.OWNER_CONFLICT));

        assertSnapshot(
                controller,
                AiMode.OBSERVE,
                AiMode.ENHANCED,
                NavigationOwnership.TRANSITIONING,
                2L,
                Optional.of(FallbackReason.OWNER_CONFLICT));
        assertThrows(IllegalStateException.class, controller::completeRelease);
        assertThrows(
                IllegalStateException.class,
                () -> controller.requestFallback(FallbackReason.ENTITY_LIFECYCLE));
        controller.completeFallback();

        assertSnapshot(
                controller,
                AiMode.OBSERVE,
                AiMode.OBSERVE,
                NavigationOwnership.VANILLA_OWNED,
                2L,
                Optional.of(FallbackReason.OWNER_CONFLICT));
        assertEquals(TransitionAction.NONE, controller.requestMode(AiMode.OBSERVE));
        assertTrue(controller.snapshot().lastFallbackReason().isEmpty());

        ModeTransitionController vanillaOwned = new ModeTransitionController(AiMode.ASSIST);
        assertEquals(
                TransitionAction.NONE,
                vanillaOwned.requestFallback(FallbackReason.CONFIG_DISABLED));
        assertSnapshot(
                vanillaOwned,
                AiMode.OBSERVE,
                AiMode.OBSERVE,
                NavigationOwnership.VANILLA_OWNED,
                1L,
                Optional.of(FallbackReason.CONFIG_DISABLED));

        ModeTransitionController failedAcquisition = new ModeTransitionController(AiMode.OBSERVE);
        failedAcquisition.requestMode(AiMode.ENHANCED);
        assertEquals(
                TransitionAction.RESTORE_VANILLA_FALLBACK,
                failedAcquisition.requestFallback(FallbackReason.LEASE_REJECTED));
        failedAcquisition.completeFallback();
        assertEquals(NavigationOwnership.VANILLA_OWNED, failedAcquisition.snapshot().ownership());
    }

    @Test
    void nullFallbackReasonFailsBeforeMutatingState() {
        ModeTransitionController controller = new ModeTransitionController(AiMode.ASSIST);
        ModeTransitionSnapshot before = controller.snapshot();

        assertThrows(NullPointerException.class, () -> controller.requestFallback(null));

        assertEquals(before, controller.snapshot());
        assertThrows(NullPointerException.class, () -> controller.requestMode(null));
    }

    private static void assertSnapshot(
            ModeTransitionController controller,
            AiMode requested,
            AiMode active,
            NavigationOwnership ownership,
            long generation,
            Optional<FallbackReason> fallback) {
        assertEquals(
                new ModeTransitionSnapshot(requested, active, ownership, generation, fallback),
                controller.snapshot());
    }
}

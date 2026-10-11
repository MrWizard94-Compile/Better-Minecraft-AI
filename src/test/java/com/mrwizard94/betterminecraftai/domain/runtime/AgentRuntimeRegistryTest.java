package com.mrwizard94.betterminecraftai.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mrwizard94.betterminecraftai.domain.profile.AiMode;
import com.mrwizard94.betterminecraftai.domain.profile.BehaviorProfile;
import com.mrwizard94.betterminecraftai.domain.profile.EntityTypeKey;
import com.mrwizard94.betterminecraftai.domain.profile.ProfileResolver;
import com.mrwizard94.betterminecraftai.domain.profile.ProfileSource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgentRuntimeRegistryTest {
    private static final UUID ENTITY_ID = UUID.fromString("bdf3045c-81d4-4a97-bbe4-74575631afab");
    private static final BehaviorProfile OBSERVE_ZOMBIE = profile(
            ProfileResolver.C0_ZOMBIE,
            AiMode.OBSERVE,
            true);
    private static final BehaviorProfile ENHANCED_ZOMBIE = profile(
            ProfileResolver.C0_ZOMBIE,
            AiMode.ENHANCED,
            true);

    @Test
    void attachesFindsAndRemovesVanillaOwnedRuntime() {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();

        AgentRuntime runtime = registry.attach(ENTITY_ID, OBSERVE_ZOMBIE);

        assertEquals(1, registry.size());
        assertSame(runtime, registry.find(ENTITY_ID).orElseThrow());
        assertEquals(List.of(runtime), registry.snapshot());
        assertThrows(UnsupportedOperationException.class, () -> registry.snapshot().clear());
        assertSame(runtime, registry.removeVanillaOwned(ENTITY_ID).orElseThrow());
        assertEquals(0, registry.size());
        assertTrue(registry.find(ENTITY_ID).isEmpty());
        assertTrue(registry.removeVanillaOwned(ENTITY_ID).isEmpty());
    }

    @Test
    void rejectsDuplicateAttachmentWithoutReplacingOriginal() {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();
        AgentRuntime original = registry.attach(ENTITY_ID, OBSERVE_ZOMBIE);

        assertThrows(IllegalStateException.class, () -> registry.attach(ENTITY_ID, ENHANCED_ZOMBIE));

        assertSame(original, registry.find(ENTITY_ID).orElseThrow());
        assertEquals(1, registry.size());
    }

    @Test
    void cleanupFailsUntilLeaseIsReleasedOrFallbackRestoresVanilla() {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();
        AgentRuntime runtime = registry.attach(ENTITY_ID, OBSERVE_ZOMBIE);
        assertEquals(TransitionAction.ACQUIRE_C0_LEASE, runtime.reconcileProfile(ENHANCED_ZOMBIE));
        runtime.modeController().completeAcquisition();

        assertThrows(IllegalStateException.class, () -> registry.removeVanillaOwned(ENTITY_ID));
        assertThrows(IllegalStateException.class, registry::clearVanillaOwned);
        assertEquals(1, registry.size());

        assertEquals(
                TransitionAction.RESTORE_VANILLA_FALLBACK,
                runtime.modeController().requestFallback(FallbackReason.ENTITY_LIFECYCLE));
        assertThrows(IllegalStateException.class, registry::clearVanillaOwned);
        runtime.modeController().completeFallback();
        registry.clearVanillaOwned();
        assertEquals(0, registry.size());
    }

    @Test
    void runtimeRejectsEntityTypeMutationAndInFlightProfileReplacement() {
        AgentRuntime runtime = new AgentRuntime(ENTITY_ID, OBSERVE_ZOMBIE);
        BehaviorProfile skeleton = profile(new EntityTypeKey("minecraft:skeleton"), AiMode.OBSERVE, false);

        assertThrows(IllegalArgumentException.class, () -> runtime.reconcileProfile(skeleton));
        assertSame(OBSERVE_ZOMBIE, runtime.profile());

        assertEquals(TransitionAction.ACQUIRE_C0_LEASE, runtime.reconcileProfile(ENHANCED_ZOMBIE));
        assertThrows(IllegalStateException.class, () -> runtime.reconcileProfile(OBSERVE_ZOMBIE));
        assertSame(ENHANCED_ZOMBIE, runtime.profile());
        assertEquals(ENTITY_ID, runtime.entityId());
        assertEquals(NavigationOwnership.TRANSITIONING, runtime.modeController().snapshot().ownership());
    }

    @Test
    void validatesNullInputsBeforeRegistryMutation() {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();

        assertThrows(NullPointerException.class, () -> registry.attach(null, OBSERVE_ZOMBIE));
        assertThrows(NullPointerException.class, () -> registry.attach(ENTITY_ID, null));
        assertThrows(NullPointerException.class, () -> registry.find(null));
        assertThrows(NullPointerException.class, () -> registry.removeVanillaOwned(null));
        assertThrows(NullPointerException.class, () -> new AgentRuntime(null, OBSERVE_ZOMBIE));
        assertThrows(NullPointerException.class, () -> new AgentRuntime(ENTITY_ID, null));
        assertThrows(IllegalArgumentException.class, () -> new AgentRuntime(ENTITY_ID, ENHANCED_ZOMBIE));
        assertThrows(IllegalArgumentException.class, () -> registry.attach(ENTITY_ID, ENHANCED_ZOMBIE));
        assertEquals(0, registry.size());
    }

    @Test
    void runtimeAppliesSameModeProfileWithoutLeaseAction() {
        AgentRuntime runtime = new AgentRuntime(ENTITY_ID, OBSERVE_ZOMBIE);
        BehaviorProfile refreshed = profile(ProfileResolver.C0_ZOMBIE, AiMode.OBSERVE, true);
        AgentRuntime assistRuntime = new AgentRuntime(
                UUID.fromString("0a4a9565-f464-48e1-a7e4-24a34e74fd49"),
                profile(ProfileResolver.C0_ZOMBIE, AiMode.ASSIST, true));

        assertEquals(TransitionAction.NONE, runtime.reconcileProfile(refreshed));

        assertSame(refreshed, runtime.profile());
        assertFalse(runtime.modeController().snapshot().lastFallbackReason().isPresent());
        assertEquals(AiMode.ASSIST, assistRuntime.modeController().snapshot().activeMode());
    }

    private static BehaviorProfile profile(EntityTypeKey type, AiMode mode, boolean eligible) {
        return new BehaviorProfile(
                type,
                mode,
                mode,
                eligible,
                ProfileSource.GLOBAL_DEFAULT,
                List.of("test-profile"));
    }
}

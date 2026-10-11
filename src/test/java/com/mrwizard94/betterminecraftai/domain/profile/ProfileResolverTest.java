package com.mrwizard94.betterminecraftai.domain.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProfileResolverTest {
    private static final EntityTypeKey SKELETON = new EntityTypeKey("minecraft:skeleton");
    private final ProfileResolver resolver = new ProfileResolver();

    @Test
    void permitsGlobalEnhancedModeOnlyForCertifiedC0Zombie() {
        BehaviorProfile profile = resolver.resolve(request(
                ProfileResolver.C0_ZOMBIE,
                Set.of(),
                false,
                Optional.empty(),
                Optional.empty(),
                AiMode.ENHANCED));

        assertEquals(AiMode.ENHANCED, profile.requestedMode());
        assertEquals(AiMode.ENHANCED, profile.effectiveMode());
        assertEquals(ProfileSource.GLOBAL_DEFAULT, profile.source());
        assertTrue(profile.navigationEnhancementEligible());
        assertTrue(profile.provenanceTrace().contains("builtin-profile:c0-zombie"));
        assertTrue(profile.provenanceTrace().contains("enhanced-capability:allowed-c0"));
    }

    @Test
    void perEntityOverridePrecedesTagAndGlobalMode() {
        BehaviorProfile profile = resolver.resolve(request(
                ProfileResolver.C0_ZOMBIE,
                Set.of(),
                false,
                Optional.of(AiMode.ASSIST),
                Optional.of(AiMode.ENHANCED),
                AiMode.OBSERVE));

        assertEquals(AiMode.ASSIST, profile.effectiveMode());
        assertEquals(ProfileSource.ENTITY_OVERRIDE, profile.source());
        assertTrue(profile.provenanceTrace().contains("mode-source:entity-override=ASSIST"));
        assertTrue(profile.provenanceTrace().contains("enhanced-capability:not-requested"));
    }

    @Test
    void tagModePrecedesGlobalModeWhenEntityOverrideIsAbsent() {
        BehaviorProfile profile = resolver.resolve(request(
                ProfileResolver.C0_ZOMBIE,
                Set.of(),
                false,
                Optional.empty(),
                Optional.of(AiMode.ASSIST),
                AiMode.ENHANCED));

        assertEquals(AiMode.ASSIST, profile.effectiveMode());
        assertEquals(ProfileSource.TAG_PROFILE, profile.source());
    }

    @Test
    void administratorDenylistPrecedesEveryModeRequest() {
        BehaviorProfile profile = resolver.resolve(request(
                ProfileResolver.C0_ZOMBIE,
                Set.of(),
                true,
                Optional.of(AiMode.ENHANCED),
                Optional.of(AiMode.ENHANCED),
                AiMode.ENHANCED));

        assertEquals(AiMode.OBSERVE, profile.requestedMode());
        assertEquals(AiMode.OBSERVE, profile.effectiveMode());
        assertEquals(ProfileSource.ADMIN_DENYLIST, profile.source());
        assertTrue(profile.navigationEnhancementEligible());
        assertEquals("admin-denylist:matched", profile.provenanceTrace().get(1));
    }

    @Test
    void hardSafetyExclusionPrecedesAdministratorDenylist() {
        BehaviorProfile profile = resolver.resolve(request(
                ProfileResolver.C0_ZOMBIE,
                EnumSet.of(SafetyExclusion.BOSS, SafetyExclusion.EXTERNAL_OWNER),
                true,
                Optional.of(AiMode.ENHANCED),
                Optional.empty(),
                AiMode.ENHANCED));

        assertEquals(AiMode.OBSERVE, profile.effectiveMode());
        assertEquals(ProfileSource.HARD_SAFETY_EXCLUSION, profile.source());
        assertFalse(profile.navigationEnhancementEligible());
        assertEquals("hard-safety:excluded=BOSS,EXTERNAL_OWNER", profile.provenanceTrace().getFirst());
    }

    @Test
    void unsupportedEnhancedRequestFailsClosedToObserve() {
        BehaviorProfile profile = resolver.resolve(request(
                SKELETON,
                Set.of(),
                false,
                Optional.of(AiMode.ENHANCED),
                Optional.empty(),
                AiMode.OBSERVE));

        assertEquals(AiMode.ENHANCED, profile.requestedMode());
        assertEquals(AiMode.OBSERVE, profile.effectiveMode());
        assertEquals(ProfileSource.UNSUPPORTED_ENHANCED_REQUEST, profile.source());
        assertFalse(profile.navigationEnhancementEligible());
        assertTrue(profile.provenanceTrace().contains("builtin-profile:compatibility-only"));
        assertTrue(profile.provenanceTrace().contains("enhanced-capability:rejected-unsupported-entity"));
    }

    @Test
    void unsupportedEntityRetainsNonCompetitiveGlobalMode() {
        BehaviorProfile profile = resolver.resolve(request(
                SKELETON,
                Set.of(),
                false,
                Optional.empty(),
                Optional.empty(),
                AiMode.OBSERVE));

        assertEquals(AiMode.OBSERVE, profile.effectiveMode());
        assertEquals(ProfileSource.GLOBAL_DEFAULT, profile.source());
        assertFalse(profile.navigationEnhancementEligible());
    }

    @Test
    void requestDefensivelyCopiesSafetyExclusions() {
        EnumSet<SafetyExclusion> exclusions = EnumSet.of(SafetyExclusion.BRAIN_CONTROLLED);
        ProfileRequest request = request(
                SKELETON,
                exclusions,
                false,
                Optional.empty(),
                Optional.empty(),
                AiMode.OBSERVE);

        exclusions.clear();

        assertEquals(Set.of(SafetyExclusion.BRAIN_CONTROLLED), request.safetyExclusions());
        assertThrows(
                UnsupportedOperationException.class,
                () -> request.safetyExclusions().add(SafetyExclusion.SPECIALIZED_NAVIGATION));
    }

    @Test
    void behaviorProfileDefensivelyCopiesTraceAndEnforcesInvariant() {
        List<String> mutableTrace = new ArrayList<>(List.of("test:resolved"));
        BehaviorProfile profile = new BehaviorProfile(
                SKELETON,
                AiMode.OBSERVE,
                AiMode.OBSERVE,
                false,
                ProfileSource.GLOBAL_DEFAULT,
                mutableTrace);
        mutableTrace.clear();

        assertEquals(List.of("test:resolved"), profile.provenanceTrace());
        assertThrows(
                UnsupportedOperationException.class,
                () -> profile.provenanceTrace().add("mutated"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BehaviorProfile(
                        SKELETON,
                        AiMode.OBSERVE,
                        AiMode.OBSERVE,
                        false,
                        ProfileSource.GLOBAL_DEFAULT,
                        List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new BehaviorProfile(
                        SKELETON,
                        AiMode.ENHANCED,
                        AiMode.ENHANCED,
                        false,
                        ProfileSource.UNSUPPORTED_ENHANCED_REQUEST,
                        List.of("invalid")));
        assertThrows(NullPointerException.class, () -> resolver.resolve(null));
    }

    private static ProfileRequest request(
            EntityTypeKey entityType,
            Set<SafetyExclusion> exclusions,
            boolean adminDenied,
            Optional<AiMode> entityOverride,
            Optional<AiMode> tagMode,
            AiMode globalMode) {
        return new ProfileRequest(
                entityType,
                exclusions,
                adminDenied,
                entityOverride,
                tagMode,
                globalMode);
    }
}

package com.mrwizard94.betterminecraftai.domain.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Resolves profiles in the blueprint's safety-first precedence order.
 */
public final class ProfileResolver {
    /** The only entity type certified for a P1 enhanced-navigation adapter. */
    public static final EntityTypeKey C0_ZOMBIE = new EntityTypeKey("minecraft:zombie");

    /**
     * Resolves one immutable profile. Hard exclusions and administrator opt-outs always win.
     *
     * @param request validated resolution facts
     * @return safe behavior profile and provenance
     */
    public BehaviorProfile resolve(ProfileRequest request) {
        Objects.requireNonNull(request, "request");
        List<String> trace = new ArrayList<>();
        boolean c0Eligible = C0_ZOMBIE.equals(request.entityType());

        if (!request.safetyExclusions().isEmpty()) {
            String exclusions = request.safetyExclusions().stream()
                    .sorted()
                    .map(SafetyExclusion::name)
                    .collect(Collectors.joining(","));
            trace.add("hard-safety:excluded=" + exclusions);
            return profile(
                    request,
                    AiMode.OBSERVE,
                    AiMode.OBSERVE,
                    false,
                    ProfileSource.HARD_SAFETY_EXCLUSION,
                    trace);
        }
        trace.add("hard-safety:pass");

        if (request.adminDenied()) {
            trace.add("admin-denylist:matched");
            return profile(
                    request,
                    AiMode.OBSERVE,
                    AiMode.OBSERVE,
                    c0Eligible,
                    ProfileSource.ADMIN_DENYLIST,
                    trace);
        }
        trace.add("admin-denylist:pass");
        trace.add(c0Eligible ? "builtin-profile:c0-zombie" : "builtin-profile:compatibility-only");

        AiMode requestedMode;
        ProfileSource requestedSource;
        if (request.entityOverride().isPresent()) {
            requestedMode = request.entityOverride().orElseThrow();
            requestedSource = ProfileSource.ENTITY_OVERRIDE;
            trace.add("mode-source:entity-override=" + requestedMode);
        } else if (request.tagMode().isPresent()) {
            requestedMode = request.tagMode().orElseThrow();
            requestedSource = ProfileSource.TAG_PROFILE;
            trace.add("mode-source:tag-profile=" + requestedMode);
        } else {
            requestedMode = request.globalMode();
            requestedSource = ProfileSource.GLOBAL_DEFAULT;
            trace.add("mode-source:global-default=" + requestedMode);
        }

        if (requestedMode == AiMode.ENHANCED && !c0Eligible) {
            trace.add("enhanced-capability:rejected-unsupported-entity");
            return profile(
                    request,
                    requestedMode,
                    AiMode.OBSERVE,
                    false,
                    ProfileSource.UNSUPPORTED_ENHANCED_REQUEST,
                    trace);
        }

        trace.add(requestedMode == AiMode.ENHANCED
                ? "enhanced-capability:allowed-c0"
                : "enhanced-capability:not-requested");
        return profile(request, requestedMode, requestedMode, c0Eligible, requestedSource, trace);
    }

    private static BehaviorProfile profile(
            ProfileRequest request,
            AiMode requestedMode,
            AiMode effectiveMode,
            boolean c0Eligible,
            ProfileSource source,
            List<String> trace) {
        return new BehaviorProfile(
                request.entityType(),
                requestedMode,
                effectiveMode,
                c0Eligible,
                source,
                trace);
    }
}

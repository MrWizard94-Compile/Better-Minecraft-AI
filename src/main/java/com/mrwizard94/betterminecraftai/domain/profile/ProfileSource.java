package com.mrwizard94.betterminecraftai.domain.profile;

/**
 * Highest-precedence rule responsible for a resolved profile result.
 */
public enum ProfileSource {
    /** A hard safety fact forced compatibility-only behavior. */
    HARD_SAFETY_EXCLUSION,
    /** The server administrator explicitly opted the entity type out. */
    ADMIN_DENYLIST,
    /** A specific entity override selected the requested mode. */
    ENTITY_OVERRIDE,
    /** A validated entity-type tag or data profile selected the requested mode. */
    TAG_PROFILE,
    /** The global conservative mode supplied the request. */
    GLOBAL_DEFAULT,
    /** A non-C0 entity requested ENHANCED and was safely downgraded. */
    UNSUPPORTED_ENHANCED_REQUEST
}

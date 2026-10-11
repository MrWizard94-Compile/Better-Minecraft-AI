package com.mrwizard94.betterminecraftai.domain.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EntityTypeKeyTest {
    @Test
    void acceptsCanonicalResourceLocationText() {
        EntityTypeKey key = new EntityTypeKey("example_mod:path/to.entity-name");

        assertEquals("example_mod:path/to.entity-name", key.value());
        assertEquals(key.value(), key.toString());
    }

    @Test
    void rejectsUpperCaseAndMissingNamespace() {
        assertThrows(IllegalArgumentException.class, () -> new EntityTypeKey("Minecraft:Zombie"));
        assertThrows(IllegalArgumentException.class, () -> new EntityTypeKey("zombie"));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new EntityTypeKey(null));
    }
}

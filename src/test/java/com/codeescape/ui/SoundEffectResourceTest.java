package com.codeescape.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SoundEffectResourceTest {
    @Test
    void allSoundEffectsHaveResources() {
        for (SoundEffect effect : SoundEffect.values()) {
            assertNotNull(
                    SoundManager.class.getResource(effect.getResourcePath()),
                    () -> "Missing sound resource: " + effect.getResourcePath()
            );
        }
    }
}

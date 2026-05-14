package com.codeescape.ui;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.media.AudioClip;

public final class SoundManager {
    private static final Map<SoundEffect, Optional<AudioClip>> CLIPS = new EnumMap<>(SoundEffect.class);

    private SoundManager() {
    }

    public static void play(SoundEffect effect) {
        if (effect == null) {
            return;
        }

        try {
            CLIPS.computeIfAbsent(effect, SoundManager::loadClip)
                    .ifPresent(clip -> clip.play(effect.getVolume()));
        } catch (RuntimeException exception) {
            CLIPS.put(effect, Optional.empty());
        }
    }

    private static Optional<AudioClip> loadClip(SoundEffect effect) {
        URL resource = SoundManager.class.getResource(effect.getResourcePath());
        if (resource == null) {
            return Optional.empty();
        }
        return Optional.of(new AudioClip(resource.toExternalForm()));
    }
}

package com.codeescape.ui;

public enum SoundEffect {
    BUTTON("/sounds/button.wav", 0.35),
    PICKUP("/sounds/pickup.wav", 0.45),
    HINT("/sounds/hint.wav", 0.45),
    SUCCESS("/sounds/success.wav", 0.50),
    BUG("/sounds/bug.wav", 0.45),
    MEDAL("/sounds/medal.wav", 0.55);

    private final String resourcePath;
    private final double volume;

    SoundEffect(String resourcePath, double volume) {
        this.resourcePath = resourcePath;
        this.volume = volume;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    double getVolume() {
        return volume;
    }
}

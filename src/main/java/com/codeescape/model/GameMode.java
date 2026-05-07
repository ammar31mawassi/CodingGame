package com.codeescape.model;

public enum GameMode {
    NORMAL("Normal Mode"),
    HARD("Hard Mode");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHard() {
        return this == HARD;
    }
}

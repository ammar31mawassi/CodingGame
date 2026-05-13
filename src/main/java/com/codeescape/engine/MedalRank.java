package com.codeescape.engine;

public enum MedalRank {
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold");

    private final String displayName;

    MedalRank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

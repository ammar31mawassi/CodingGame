package com.codeescape.engine;

public enum MedalRank {
    BRONZE("Bronze", "B", "bronze"),
    SILVER("Silver", "S", "silver"),
    GOLD("Gold", "G", "gold");

    private final String displayName;
    private final String shortLabel;
    private final String styleSuffix;

    MedalRank(String displayName, String shortLabel, String styleSuffix) {
        this.displayName = displayName;
        this.shortLabel = shortLabel;
        this.styleSuffix = styleSuffix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getStyleSuffix() {
        return styleSuffix;
    }
}

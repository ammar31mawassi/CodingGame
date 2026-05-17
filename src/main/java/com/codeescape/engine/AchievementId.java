package com.codeescape.engine;

public enum AchievementId {
    CLEAN_CODER("Clean Coder", "Finish a level without making any bugs."),
    HARD_MODE_FINISHER("Hard Mode Stage Clear", "Finish the last level in a stage on hard mode."),
    HELPER_SCOUT("Helper Scout", "Find a hidden helper before completing a level."),
    BOSS_BREAKER("Boss Breaker", "Clear a stage boss room and prove you can combine concepts.");

    private final String title;
    private final String description;

    AchievementId(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

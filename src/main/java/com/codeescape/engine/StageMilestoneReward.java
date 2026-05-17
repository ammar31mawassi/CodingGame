package com.codeescape.engine;

import java.util.Arrays;
import java.util.List;

public enum StageMilestoneReward {
    STAGE_1_CLEAR("stage-1-clear", 1, false, "Stage 1 Route Key", "Cleared every Stage 1 room. Your route summary now records the stage as fully explored."),
    STAGE_1_GOLD("stage-1-gold", 1, true, "Stage 1 Gold Sigil", "Earned gold on every Stage 1 room. The menu now marks the stage as mastered."),
    STAGE_1_BOSS("stage-1-boss", 1, false, "Stage 1 Boss Seal", "Cleared the optional Stage 1 synthesis boss and proved the basics work under pressure."),
    STAGE_2_CLEAR("stage-2-clear", 2, false, "Stage 2 Route Key", "Cleared every Stage 2 room. Your route summary now records the stage as fully explored."),
    STAGE_2_GOLD("stage-2-gold", 2, true, "Stage 2 Gold Sigil", "Earned gold on every Stage 2 room. The menu now marks the stage as mastered."),
    STAGE_2_BOSS("stage-2-boss", 2, false, "Stage 2 Boss Seal", "Cleared the optional Stage 2 synthesis boss and locked in variable-plus-branch control."),
    STAGE_3_CLEAR("stage-3-clear", 3, false, "Stage 3 Route Key", "Cleared every Stage 3 room. Your route summary now records the stage as fully explored."),
    STAGE_3_GOLD("stage-3-gold", 3, true, "Stage 3 Gold Sigil", "Earned gold on every Stage 3 room. The menu now marks the stage as mastered."),
    STAGE_3_BOSS("stage-3-boss", 3, false, "Stage 3 Boss Seal", "Cleared the optional Stage 3 synthesis boss and connected text work with method structure."),
    STAGE_4_CLEAR("stage-4-clear", 4, false, "Stage 4 Route Key", "Cleared every Stage 4 room. Your route summary now records the stage as fully explored."),
    STAGE_4_GOLD("stage-4-gold", 4, true, "Stage 4 Gold Sigil", "Earned gold on every Stage 4 room. The menu now marks the stage as mastered."),
    STAGE_4_BOSS("stage-4-boss", 4, false, "Stage 4 Boss Seal", "Cleared the optional Stage 4 synthesis boss and kept loop logic stable across multiple steps."),
    STAGE_5_CLEAR("stage-5-clear", 5, false, "Stage 5 Route Key", "Cleared every Stage 5 room. Your route summary now records the stage as fully explored."),
    STAGE_5_GOLD("stage-5-gold", 5, true, "Stage 5 Gold Sigil", "Earned gold on every Stage 5 room. The menu now marks the stage as mastered."),
    STAGE_5_BOSS("stage-5-boss", 5, false, "Stage 5 Boss Seal", "Cleared the optional Stage 5 synthesis boss and finished the object/composition mastery route.");

    private final String id;
    private final int stageNumber;
    private final boolean allGoldRequired;
    private final String title;
    private final String description;

    StageMilestoneReward(String id, int stageNumber, boolean allGoldRequired, String title, String description) {
        this.id = id;
        this.stageNumber = stageNumber;
        this.allGoldRequired = allGoldRequired;
        this.title = title;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public int stageNumber() {
        return stageNumber;
    }

    public boolean allGoldRequired() {
        return allGoldRequired;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public boolean bossReward() {
        return id.endsWith("-boss");
    }

    public static List<StageMilestoneReward> forStage(int stageNumber) {
        return Arrays.stream(values())
                .filter(reward -> reward.stageNumber == stageNumber)
                .toList();
    }

    public static StageMilestoneReward fromId(String id) {
        return Arrays.stream(values())
                .filter(reward -> reward.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown stage reward id: " + id));
    }
}

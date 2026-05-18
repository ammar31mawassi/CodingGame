package com.codeescape.model;

public class Level {
    private final int levelNumber;
    private final int stageNumber;
    private final int stageLevelNumber;
    private final String stageTitle;
    private final String name;
    private final String concept;
    private final String completionExplanation;
    private final String goalHelper;
    private final String displayIdOverride;
    private final com.codeescape.engine.MedalContract medalContract;
    private final Room room;
    private boolean completed;

    public Level(int levelNumber, String name, String concept, Room room) {
        this(levelNumber, name, concept, "", "", room);
    }

    public Level(int levelNumber, String name, String concept, String completionExplanation, Room room) {
        this(levelNumber, name, concept, completionExplanation, "", room);
    }

    public Level(int levelNumber, String name, String concept, String completionExplanation, String goalHelper, Room room) {
        this(levelNumber, 1, levelNumber, concept, name, concept, completionExplanation, goalHelper, null, null, room);
    }

    public Level(
            int levelNumber,
            int stageNumber,
            int stageLevelNumber,
            String stageTitle,
            String name,
            String concept,
            String completionExplanation,
            String goalHelper,
            com.codeescape.engine.MedalContract medalContract,
            Room room
    ) {
        this(levelNumber, stageNumber, stageLevelNumber, stageTitle, name, concept, completionExplanation, goalHelper, null, medalContract, room);
    }

    public Level(
            int levelNumber,
            int stageNumber,
            int stageLevelNumber,
            String stageTitle,
            String name,
            String concept,
            String completionExplanation,
            String goalHelper,
            String displayIdOverride,
            com.codeescape.engine.MedalContract medalContract,
            Room room
    ) {
        this.levelNumber = levelNumber;
        this.stageNumber = stageNumber;
        this.stageLevelNumber = stageLevelNumber;
        this.stageTitle = stageTitle == null ? "" : stageTitle;
        this.name = name;
        this.concept = concept;
        this.completionExplanation = completionExplanation;
        this.goalHelper = goalHelper;
        this.displayIdOverride = displayIdOverride;
        this.medalContract = medalContract;
        this.room = room;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public int getStageLevelNumber() {
        return stageLevelNumber;
    }

    public String getStageTitle() {
        return stageTitle;
    }

    public String getDisplayId() {
        if (displayIdOverride != null && !displayIdOverride.isBlank()) {
            return displayIdOverride;
        }
        return stageNumber + "-" + stageLevelNumber;
    }

    public String getName() {
        return name;
    }

    public String getConcept() {
        return concept;
    }

    public String getCompletionExplanation() {
        return completionExplanation;
    }

    public String getGoalHelper() {
        return goalHelper;
    }

    public com.codeescape.engine.MedalContract getMedalContract() {
        return medalContract;
    }

    public Room getRoom() {
        return room;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        completed = true;
    }
}

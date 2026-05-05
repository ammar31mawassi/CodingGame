package com.codeescape.model;

public class Level {
    private final int levelNumber;
    private final String name;
    private final String concept;
    private final String completionExplanation;
    private final String goalHelper;
    private final Room room;
    private boolean completed;

    public Level(int levelNumber, String name, String concept, Room room) {
        this(levelNumber, name, concept, "", "", room);
    }

    public Level(int levelNumber, String name, String concept, String completionExplanation, Room room) {
        this(levelNumber, name, concept, completionExplanation, "", room);
    }

    public Level(int levelNumber, String name, String concept, String completionExplanation, String goalHelper, Room room) {
        this.levelNumber = levelNumber;
        this.name = name;
        this.concept = concept;
        this.completionExplanation = completionExplanation;
        this.goalHelper = goalHelper;
        this.room = room;
    }

    public int getLevelNumber() {
        return levelNumber;
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

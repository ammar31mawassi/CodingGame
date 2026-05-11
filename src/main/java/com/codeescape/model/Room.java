package com.codeescape.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    private final int width;
    private final int height;
    private final List<Token> tokens;
    private final List<Wall> walls;
    private final List<Chest> chests;
    private final List<ProgrammableObject> programmableObjects;
    private final List<ChestReward> chestRewards;
    private final ChestReward finalChestReward;
    private final Door door;
    private final Door challengeDoor;
    private final MultipleChoiceQuestion challengeQuestion;
    private final Puzzle puzzle;
    private final boolean hasHiddenGoal;
    private final boolean hasHiddenHelper;
    private boolean goalFound;
    private boolean helperFound;

    public Room(int width, int height, List<Token> tokens, Door door, Puzzle puzzle) {
        this(width, height, tokens, List.of(), List.of(), List.of(), List.of(), null, door, null, null, puzzle);
    }

    public Room(
            int width,
            int height,
            List<Token> tokens,
            List<Wall> walls,
            List<Chest> chests,
            List<ChestReward> chestRewards,
            ChestReward finalChestReward,
            Door door,
            Puzzle puzzle
    ) {
        this(width, height, tokens, walls, chests, List.of(), chestRewards, finalChestReward, door, null, null, puzzle);
    }

    public Room(
            int width,
            int height,
            List<Token> tokens,
            List<Wall> walls,
            List<Chest> chests,
            List<ChestReward> chestRewards,
            ChestReward finalChestReward,
            Door door,
            Door challengeDoor,
            MultipleChoiceQuestion challengeQuestion,
            Puzzle puzzle
    ) {
        this(width, height, tokens, walls, chests, List.of(), chestRewards, finalChestReward, door, challengeDoor, challengeQuestion, puzzle);
    }

    public Room(
            int width,
            int height,
            List<Token> tokens,
            List<Wall> walls,
            List<Chest> chests,
            List<ProgrammableObject> programmableObjects,
            List<ChestReward> chestRewards,
            ChestReward finalChestReward,
            Door door,
            Door challengeDoor,
            MultipleChoiceQuestion challengeQuestion,
            Puzzle puzzle
    ) {
        this.width = width;
        this.height = height;
        this.tokens = new ArrayList<>(tokens);
        this.walls = new ArrayList<>(walls);
        this.chests = new ArrayList<>(chests);
        this.programmableObjects = new ArrayList<>(programmableObjects);
        this.chestRewards = new ArrayList<>(chestRewards);
        this.finalChestReward = finalChestReward;
        this.door = door;
        this.challengeDoor = challengeDoor;
        this.challengeQuestion = challengeQuestion;
        this.puzzle = puzzle;
        this.hasHiddenGoal = hasRewardType(TokenType.GOAL);
        this.hasHiddenHelper = hasRewardType(TokenType.HELPER);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public List<Wall> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    public List<Chest> getChests() {
        return Collections.unmodifiableList(chests);
    }

    public List<ChestReward> getChestRewards() {
        return Collections.unmodifiableList(chestRewards);
    }

    public ChestReward getFinalChestReward() {
        return finalChestReward;
    }

    public List<ProgrammableObject> getProgrammableObjects() {
        return Collections.unmodifiableList(programmableObjects);
    }

    public Door getDoor() {
        return door;
    }

    public Door getChallengeDoor() {
        return challengeDoor;
    }

    public MultipleChoiceQuestion getChallengeQuestion() {
        return challengeQuestion;
    }

    public boolean hasChallengeDoor() {
        return challengeDoor != null && challengeQuestion != null;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public void collectToken(Token token, Inventory inventory) {
        if (!token.isCollected()) {
            token.collect();
            collectReward(token.getValue(), token.getType(), inventory);
        }
    }

    public ChestReward openChest(Chest chest, Inventory inventory) {
        if (chest.isOpened() || chest.isLocked()) {
            return null;
        }

        chest.open();
        ChestReward reward = nextChestReward();
        if (reward != null) {
            collectReward(reward.getValue(), reward.getType(), inventory);
        }
        return reward;
    }

    public boolean hasHiddenGoal() {
        return hasHiddenGoal;
    }

    public boolean isGoalFound() {
        return !hasHiddenGoal || goalFound;
    }

    public boolean hasHiddenHelper() {
        return hasHiddenHelper;
    }

    public boolean isHelperFound() {
        return !hasHiddenHelper || helperFound;
    }

    public boolean allRequiredTokensCollected() {
        return tokens.stream().allMatch(Token::isCollected);
    }

    private void collectReward(String value, TokenType type, Inventory inventory) {
        if (type == TokenType.CODE) {
            inventory.addToken(new Token(value, 0, 0, 0, 0));
        } else if (type == TokenType.GOAL) {
            goalFound = true;
        } else if (type == TokenType.HELPER) {
            helperFound = true;
        }
    }

    public void collectReward(ChestReward reward, Inventory inventory) {
        if (reward != null) {
            collectReward(reward.getValue(), reward.getType(), inventory);
        }
    }

    private ChestReward nextChestReward() {
        if (finalChestReward != null && unopenedChestCount() == 0) {
            return finalChestReward;
        }
        if (chestRewards.isEmpty()) {
            return null;
        }
        return chestRewards.remove(0);
    }

    private long unopenedChestCount() {
        return chests.stream().filter(chest -> !chest.isOpened()).count();
    }

    private boolean hasRewardType(TokenType type) {
        return tokens.stream().anyMatch(token -> token.getType() == type)
                || chestRewards.stream().anyMatch(reward -> reward.getType() == type)
                || (finalChestReward != null && finalChestReward.getType() == type);
    }
}

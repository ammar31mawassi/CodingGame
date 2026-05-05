package com.codeescape.model;

public class ChestReward {
    private final String value;
    private final TokenType type;

    public ChestReward(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }

    public static ChestReward code(String value) {
        return new ChestReward(value, TokenType.CODE);
    }

    public static ChestReward goal() {
        return new ChestReward("Goal", TokenType.GOAL);
    }

    public static ChestReward helper() {
        return new ChestReward("Helper", TokenType.HELPER);
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }
}

package com.codeescape.model;

public class Token {
    private final String value;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final TokenType type;
    private boolean collected;

    public Token(String value, double x, double y, double width, double height) {
        this(value, x, y, width, height, TokenType.CODE);
    }

    public Token(String value, double x, double y, double width, double height, TokenType type) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public boolean isCodeToken() {
        return type == TokenType.CODE;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public boolean intersects(Player player) {
        return player.intersects(x, y, width, height);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}

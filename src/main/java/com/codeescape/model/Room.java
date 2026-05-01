package com.codeescape.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    private final int width;
    private final int height;
    private final List<Token> tokens;
    private final Door door;
    private final Puzzle puzzle;

    public Room(int width, int height, List<Token> tokens, Door door, Puzzle puzzle) {
        this.width = width;
        this.height = height;
        this.tokens = new ArrayList<>(tokens);
        this.door = door;
        this.puzzle = puzzle;
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

    public Door getDoor() {
        return door;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public void collectToken(Token token, Inventory inventory) {
        if (!token.isCollected()) {
            token.collect();
            inventory.addToken(token);
        }
    }

    public boolean allRequiredTokensCollected() {
        return tokens.stream().allMatch(Token::isCollected);
    }
}

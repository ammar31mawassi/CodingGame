package com.codeescape.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {
    private final List<Token> tokens = new ArrayList<>();

    public void addToken(Token token) {
        tokens.add(token);
    }

    public void removeToken(Token token) {
        tokens.remove(token);
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public void clear() {
        tokens.clear();
    }

    public boolean containsValue(String value) {
        return tokens.stream().anyMatch(token -> token.getValue().equals(value));
    }

    public List<String> getTokenValues() {
        return tokens.stream().map(Token::getValue).toList();
    }
}

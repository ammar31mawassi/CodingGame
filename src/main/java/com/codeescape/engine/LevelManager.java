package com.codeescape.engine;

import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.Puzzle;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.validation.ClassDeclarationValidator;
import com.codeescape.validation.IfStatementValidator;
import com.codeescape.validation.VariableDeclarationValidator;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private final List<Level> levels = new ArrayList<>();
    private int currentLevelIndex;

    public void loadLevels() {
        levels.clear();
        currentLevelIndex = 0;
        levels.add(createVariableLevel());
        levels.add(createIfStatementLevel());
        levels.add(createClassLevel());
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }

    public Level goToNextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
        }
        return getCurrentLevel();
    }

    public Level getLevel(int levelNumber) {
        return levels.stream()
                .filter(level -> level.getLevelNumber() == levelNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown level: " + levelNumber));
    }

    private Level createVariableLevel() {
        Puzzle puzzle = new Puzzle(
                "Variables",
                "Build a valid Java variable declaration.",
                List.of("types", "variables", "assignment"),
                new VariableDeclarationValidator()
        );

        Room room = new Room(
                800,
                600,
                tokens("int", "String", "char", "boolean", "x", "name", "grade", "active", "=", "5", "\"Ammar\"", "'A'", "true", ";", "banana", "house", "if"),
                new Door(730, 250, 40, 100),
                puzzle
        );

        return new Level(1, "Variable Vault", "Variable declarations", room);
    }

    private Level createIfStatementLevel() {
        Puzzle puzzle = new Puzzle(
                "If Statements",
                "Build a valid Java if-statement.",
                List.of("conditions", "comparison operators", "blocks"),
                new IfStatementValidator()
        );

        Room room = new Room(
                800,
                600,
                tokens("if", "(", ")", "{", "}", "x", "age", ">", ">=", "5", "18", "true", "=", "==", ";"),
                new Door(730, 250, 40, 100),
                puzzle
        );

        return new Level(2, "Condition Chamber", "If-statements", room);
    }

    private Level createClassLevel() {
        Puzzle puzzle = new Puzzle(
                "Classes",
                "Build a valid Java class declaration.",
                List.of("classes", "identifiers", "braces"),
                new ClassDeclarationValidator()
        );

        Room room = new Room(
                800,
                600,
                tokens("class", "Person", "Student", "{", "}", "public", "static", "5", ";"),
                new Door(730, 250, 40, 100),
                puzzle
        );

        return new Level(3, "Classroom Exit", "Classes and objects", room);
    }

    private List<Token> tokens(String... values) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double x = 80 + (i % 6) * 100;
            double y = 100 + (i / 6) * 80;
            tokens.add(new Token(values[i], x, y, 70, 32));
        }
        return tokens;
    }
}

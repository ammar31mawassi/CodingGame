package com.codeescape.engine;

import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.Puzzle;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.util.Constants;
import com.codeescape.validation.ClassDeclarationValidator;
import com.codeescape.validation.IfStatementValidator;
import com.codeescape.validation.StringDeclarationValidator;
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
        levels.add(createStringDeclarationLevel());
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
                VariableDeclarationValidator.getInstance()
        );

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                tokens("int", "x", "=", "5", ";"),
                createExitDoor(),
                puzzle
        );

        return new Level(1, "Variable Vault", "Variable declarations", room);
    }

    private Level createStringDeclarationLevel() {
        Puzzle puzzle = new Puzzle(
                "String Declaration",
                "Build a valid String declaration statement.",
                List.of("String variables", "literal values"),
                new StringDeclarationValidator("\"Ammar\"")
        );

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                tokens("int", "String", "char", "age", "name", "grade", "=", "5", "\"Ammar\"", "'A'", ";"),
                createExitDoor(),
                puzzle
        );

        return new Level(2, "String Vault", "String declarations", room);
    }

    private Level createIfStatementLevel() {
        Puzzle puzzle = new Puzzle(
                "If Statements",
                "Build a valid Java if-statement.",
                List.of("conditions", "comparison operators", "blocks"),
                new IfStatementValidator()
        );

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                tokens("if", "(", ")", "{", "}", "x", "age", ">", ">=", "5", "18", "true", "=", "==", ";"),
                createExitDoor(),
                puzzle
        );

        return new Level(3, "Condition Chamber", "If-statements", room);
    }

    private Level createClassLevel() {
        Puzzle puzzle = new Puzzle(
                "Classes",
                "Build a valid Java class declaration.",
                List.of("classes", "identifiers", "braces"),
                new ClassDeclarationValidator()
        );

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                tokens("class", "Person", "Student", "{", "}", "public", "static", "5", ";"),
                createExitDoor(),
                puzzle
        );

        return new Level(4, "Classroom Exit", "Classes and objects", room);
    }

    private List<Token> tokens(String... values) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double x = 118 + (i % 6) * 104;
            double y = 118 + (i / 6) * 78;
            tokens.add(new Token(values[i], x, y, 46, 28));
        }
        return tokens;
    }

    private Door createExitDoor() {
        return new Door(Constants.ROOM_WIDTH - 82, Constants.ROOM_HEIGHT / 2.0 - 59, 52, 118);
    }
}

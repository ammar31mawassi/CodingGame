package com.codeescape.engine;

import com.codeescape.model.Door;
import com.codeescape.model.Chest;
import com.codeescape.model.ChestReward;
import com.codeescape.model.Level;
import com.codeescape.model.Puzzle;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import com.codeescape.model.Wall;
import com.codeescape.util.Constants;
import com.codeescape.validation.IfStatementValidator;
import com.codeescape.validation.StringDeclarationValidator;
import com.codeescape.validation.VariableDeclarationValidator;
import com.codeescape.validation.VariableThenIfValidator;
import java.util.ArrayList;
import java.util.Collections;
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
        levels.add(createVariableThenIfLevel());
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

        return new Level(
                1,
                "Variable Vault",
                "Variable declarations",
                "A variable declaration is TYPE var_name = var_value;. The type tells Java what kind of value the variable stores, so the value has to match that type. The name also has rules: it cannot be a reserved word like if or class, and it cannot reuse a variable name that already exists.",
                "Build: int x = 5;",
                room
        );
    }

    private Level createStringDeclarationLevel() {
        Puzzle puzzle = new Puzzle(
                "String Declaration",
                "Build a valid String declaration statement.",
                List.of("String variables", "literal values"),
                new StringDeclarationValidator("\"Ammar\"")
        );

        List<Token> levelTokens = tokens("int", "String", "char", "age", "name", "grade", "=", "5", "\"Ammar\"", "'A'", ";");
        levelTokens.add(specialToken("Goal", 74, Constants.ROOM_HEIGHT - 108, TokenType.GOAL));

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                levelTokens,
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

        List<Token> levelTokens = tokens("if", "(", ")", "{", "}", "x", "age", ">", ">=", "5", "18", "true", "=", "==", ";");
        levelTokens.add(specialToken("Goal", 74, Constants.ROOM_HEIGHT - 108, TokenType.GOAL));
        levelTokens.add(specialToken("Helper", 210, Constants.ROOM_HEIGHT - 108, TokenType.HELPER));

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                levelTokens,
                createExitDoor(),
                puzzle
        );

        return new Level(
                3,
                "Condition Chamber",
                "If-statements",
                "An if-statement lets Java choose whether to run a block of code. The condition goes inside parentheses, and the code that runs goes inside braces: if (condition) { }. When the condition is true, Java enters the block. When it is false, Java skips it.",
                "Hint: Use if, parentheses, a comparison like x > 5, and braces for the block.",
                room
        );
    }

    private Level createVariableThenIfLevel() {
        Puzzle puzzle = new Puzzle(
                "Variable and If",
                "Declare a variable named x and then make a true if-statement.",
                List.of("variables", "conditions", "if-statements"),
                new VariableThenIfValidator()
        );

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                List.of(),
                mazeWalls(),
                mazeChests(),
                shuffledMazeRewards(),
                ChestReward.helper(),
                createExitDoor(),
                puzzle
        );

        return new Level(
                4,
                "Logic Lock",
                "Variables and if-statements",
                "",
                "Hint: The solution uses two lines. Declare x first, then test x with an if-statement.",
                room
        );
    }

    private List<Token> tokens(String... values) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double x = 118 + (i % 6) * 132;
            double y = 118 + (i / 6) * 78;
            tokens.add(new Token(values[i], x, y, tokenWidth(values[i]), 28));
        }
        return tokens;
    }

    private double tokenWidth(String value) {
        return Math.max(46, value.length() * 11 + 24);
    }

    private Token specialToken(String value, double x, double y, TokenType type) {
        return new Token(value, x, y, tokenWidth(value), 28, type);
    }

    private List<ChestReward> shuffledMazeRewards() {
        List<ChestReward> rewards = new ArrayList<>();
        for (String value : List.of("int", "x", "=", "5", ";", "if", "(", "x", ">", "3", ")", "{", "}", "String", "name", "\"ammar\"", ";")) {
            rewards.add(ChestReward.code(value));
        }
        rewards.add(ChestReward.goal());
        Collections.shuffle(rewards);
        return rewards;
    }

    private List<Chest> mazeChests() {
        return List.of(
                new Chest(82, 82, 46, 34),
                new Chest(260, 78, 46, 34),
                new Chest(430, 88, 46, 34),
                new Chest(610, 78, 46, 34),
                new Chest(780, 95, 46, 34),
                new Chest(1060, 80, 46, 34),
                new Chest(88, 285, 46, 34),
                new Chest(260, 310, 46, 34),
                new Chest(430, 290, 46, 34),
                new Chest(610, 360, 46, 34),
                new Chest(780, 310, 46, 34),
                new Chest(1060, 335, 46, 34),
                new Chest(92, 545, 46, 34),
                new Chest(270, 535, 46, 34),
                new Chest(440, 540, 46, 34),
                new Chest(620, 535, 46, 34),
                new Chest(790, 545, 46, 34),
                new Chest(960, 540, 46, 34),
                new Chest(1090, 540, 46, 34)
        );
    }

    private List<Wall> mazeWalls() {
        return List.of(
                new Wall(180, 36, 14, 200),
                new Wall(180, 310, 14, 220),
                new Wall(340, 140, 14, 260),
                new Wall(500, 36, 14, 210),
                new Wall(500, 330, 14, 190),
                new Wall(670, 120, 14, 310),
                new Wall(840, 36, 14, 240),
                new Wall(840, 360, 14, 165),
                new Wall(1000, 170, 14, 250),
                new Wall(180, 220, 170, 14),
                new Wall(340, 400, 180, 14),
                new Wall(500, 245, 180, 14),
                new Wall(670, 430, 180, 14),
                new Wall(840, 280, 170, 14)
        );
    }

    private Door createExitDoor() {
        return new Door(Constants.ROOM_WIDTH - 82, Constants.ROOM_HEIGHT / 2.0 - 59, 52, 118);
    }
}

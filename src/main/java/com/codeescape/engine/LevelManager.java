package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.ChestReward;
import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.MultipleChoiceQuestion;
import com.codeescape.model.ProgrammableObject;
import com.codeescape.model.Puzzle;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import com.codeescape.util.Constants;
import com.codeescape.validation.AcceptedAnswerValidator;
import com.codeescape.validation.CharDeclarationValidator;
import com.codeescape.validation.ClassConstructorMethodValidator;
import com.codeescape.validation.ClassFieldsValidator;
import com.codeescape.validation.CodeValidator;
import com.codeescape.validation.ForLoopValidator;
import com.codeescape.validation.IfElsePrintValidator;
import com.codeescape.validation.IfStatementValidator;
import com.codeescape.validation.MethodDeclarationValidator;
import com.codeescape.validation.ObjectCreateAndCallValidator;
import com.codeescape.validation.ObjectFieldAssignmentValidator;
import com.codeescape.validation.PrintStatementValidator;
import com.codeescape.validation.StringDeclarationValidator;
import com.codeescape.validation.VariableDeclarationValidator;
import com.codeescape.validation.VariableThenIfValidator;
import com.codeescape.validation.VariableThenPrintValidator;
import com.codeescape.validation.WhileLoopValidator;
import com.github.javaparser.ast.expr.BinaryExpr;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class LevelManager {
    private static final String STAGE_1 = "Variables And Printing";
    private static final String STAGE_2 = "If-Else Conditions";
    private static final String STAGE_3 = "String, Char, And Functions";
    private static final String STAGE_4 = "Loops";
    private static final String STAGE_5 = "Classes";
    private static final double TOKEN_HEIGHT = 34;
    private static final double TOKEN_LEFT = RoomLayoutBuilder.GRID_ORIGIN_X + RoomLayoutBuilder.GRID_CELL_WIDTH + 18;
    private static final double TOKEN_TOP = RoomLayoutBuilder.GRID_ORIGIN_Y
            + (RoomLayoutBuilder.GRID_CELL_HEIGHT - TOKEN_HEIGHT) / 2.0;
    private static final double TOKEN_RIGHT_LIMIT = Constants.ROOM_WIDTH - 170;
    private static final double TOKEN_HORIZONTAL_GAP = 16;
    private static final double TOKEN_VERTICAL_GAP = 62;
    private static final int REVISION_STAGE_1_LEVEL = 101;
    private static final int REVISION_STAGE_2_LEVEL = 102;
    private static final int REVISION_STAGE_3_LEVEL = 103;
    private static final int REVISION_STAGE_4_LEVEL = 104;
    private static final int BOSS_STAGE_1_LEVEL = 201;
    private static final int BOSS_STAGE_2_LEVEL = 202;
    private static final int BOSS_STAGE_3_LEVEL = 203;
    private static final int BOSS_STAGE_4_LEVEL = 204;
    private static final int BOSS_STAGE_5_LEVEL = 205;

    private final List<LevelDefinition> levelDefinitions = new ArrayList<>();
    private final Map<Integer, LevelDefinition> specialLevelDefinitions = new LinkedHashMap<>();
    private final LevelLayoutOverrideStore layoutOverrideStore;
    private int currentLevelIndex;
    private Level currentLevel;

    public LevelManager() {
        this(new LevelLayoutOverrideStore());
    }

    public LevelManager(LevelLayoutOverrideStore layoutOverrideStore) {
        this.layoutOverrideStore = layoutOverrideStore;
    }

    public void loadLevels() {
        levelDefinitions.clear();
        specialLevelDefinitions.clear();
        currentLevelIndex = 0;
        currentLevel = null;

        addCampaign(this::createVariableLevel);
        addCampaign(this::createPrintLevel);
        addCampaign(this::createVariableThenPrintLevel);
        addCampaign(this::createIfStatementLevel);
        addCampaign(this::createVariableThenIfLevel);
        addCampaign(this::createIfElsePrintLevel);
        addCampaign(this::createGreaterEqualsBranchLevel);
        addCampaign(this::createBooleanBranchLevel);
        addCampaign(this::createStringDeclarationLevel);
        addCampaign(this::createCharDeclarationLevel);
        addCampaign(this::createPrintMethodLevel);
        addCampaign(this::createReturnMethodLevel);
        addCampaign(this::createWhileLoopLevel);
        addCampaign(this::createForLoopIndexLevel);
        addCampaign(this::createForLoopMessageLevel);
        addCampaign(this::createWhileLoopLivesLevel);
        addCampaign(this::createClassBlueprintLevel);
        addCampaign(this::createConstructorForgeLevel);
        addCampaign(this::createObjectLockLevel);
        addCampaign(this::createVariablesCapstoneLevel);
        addCampaign(this::createConditionsDebugCapstoneLevel);
        addCampaign(this::createMethodsDebugCapstoneLevel);
        addCampaign(this::createLoopsCapstoneLevel);
        addCampaign(this::createObjectsDebugCapstoneLevel);

        addSpecial(REVISION_STAGE_1_LEVEL, this::createStageOneRevisionWing);
        addSpecial(REVISION_STAGE_2_LEVEL, this::createStageTwoRevisionWing);
        addSpecial(REVISION_STAGE_3_LEVEL, this::createStageThreeRevisionWing);
        addSpecial(REVISION_STAGE_4_LEVEL, this::createStageFourRevisionWing);
        addSpecial(BOSS_STAGE_1_LEVEL, this::createStageOneBossLevel);
        addSpecial(BOSS_STAGE_2_LEVEL, this::createStageTwoBossLevel);
        addSpecial(BOSS_STAGE_3_LEVEL, this::createStageThreeBossLevel);
        addSpecial(BOSS_STAGE_4_LEVEL, this::createStageFourBossLevel);
        addSpecial(BOSS_STAGE_5_LEVEL, this::createStageFiveBossLevel);
    }

    public Level getCurrentLevel() {
        if (currentLevel == null) {
            currentLevel = levelDefinitions.get(currentLevelIndex).create();
        }
        return currentLevel;
    }

    public List<Level> getLevels() {
        return levelDefinitions.stream()
                .map(LevelDefinition::create)
                .toList();
    }

    public List<Level> getRevisionWingLevels() {
        return List.of(
                getLevel(REVISION_STAGE_1_LEVEL),
                getLevel(REVISION_STAGE_2_LEVEL),
                getLevel(REVISION_STAGE_3_LEVEL),
                getLevel(REVISION_STAGE_4_LEVEL)
        );
    }

    public List<Level> getStageBossLevels() {
        return List.of(
                getLevel(BOSS_STAGE_1_LEVEL),
                getLevel(BOSS_STAGE_2_LEVEL),
                getLevel(BOSS_STAGE_3_LEVEL),
                getLevel(BOSS_STAGE_4_LEVEL),
                getLevel(BOSS_STAGE_5_LEVEL)
        );
    }

    public Optional<Level> revisionWingForStage(int stageNumber) {
        return switch (stageNumber) {
            case 1 -> Optional.of(getLevel(REVISION_STAGE_1_LEVEL));
            case 2 -> Optional.of(getLevel(REVISION_STAGE_2_LEVEL));
            case 3 -> Optional.of(getLevel(REVISION_STAGE_3_LEVEL));
            case 4 -> Optional.of(getLevel(REVISION_STAGE_4_LEVEL));
            default -> Optional.empty();
        };
    }

    public Optional<Level> bossLevelForStage(int stageNumber) {
        return switch (stageNumber) {
            case 1 -> Optional.of(getLevel(BOSS_STAGE_1_LEVEL));
            case 2 -> Optional.of(getLevel(BOSS_STAGE_2_LEVEL));
            case 3 -> Optional.of(getLevel(BOSS_STAGE_3_LEVEL));
            case 4 -> Optional.of(getLevel(BOSS_STAGE_4_LEVEL));
            case 5 -> Optional.of(getLevel(BOSS_STAGE_5_LEVEL));
            default -> Optional.empty();
        };
    }

    public Level dailyChallengeFor(LocalDate date) {
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        List<Level> pool = new ArrayList<>(getLevels());
        pool.addAll(getRevisionWingLevels());
        int index = Math.floorMod(effectiveDate.getDayOfYear() * 37 + effectiveDate.getYear(), pool.size());
        return pool.get(index);
    }

    public boolean hasNextLevel() {
        return currentLevelIndex < levelDefinitions.size() - 1;
    }

    public Level goToNextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
        }
        currentLevel = levelDefinitions.get(currentLevelIndex).create();
        return currentLevel;
    }

    public Level getLevel(int levelNumber) {
        return levelDefinition(levelNumber).create();
    }

    public Level goToLevel(int levelNumber) {
        for (int i = 0; i < levelDefinitions.size(); i++) {
            Level level = levelDefinitions.get(i).create();
            if (level.getLevelNumber() == levelNumber) {
                currentLevelIndex = i;
                currentLevel = level;
                return currentLevel;
            }
        }

        LevelDefinition specialDefinition = specialLevelDefinitions.get(levelNumber);
        if (specialDefinition != null) {
            currentLevel = specialDefinition.create();
            return currentLevel;
        }

        throw new IllegalArgumentException("Unknown level: " + levelNumber);
    }

    private void addCampaign(Supplier<Level> factory) {
        levelDefinitions.add(new LevelDefinition(factory));
    }

    private void addSpecial(int levelNumber, Supplier<Level> factory) {
        specialLevelDefinitions.put(levelNumber, new LevelDefinition(factory));
    }

    private LevelDefinition levelDefinition(int levelNumber) {
        LevelDefinition campaignDefinition = levelDefinitions.stream()
                .filter(definition -> definition.create().getLevelNumber() == levelNumber)
                .findFirst()
                .orElse(null);
        if (campaignDefinition != null) {
            return campaignDefinition;
        }

        LevelDefinition specialDefinition = specialLevelDefinitions.get(levelNumber);
        if (specialDefinition != null) {
            return specialDefinition;
        }

        throw new IllegalArgumentException("Unknown level: " + levelNumber);
    }

    private Level createVariableLevel() {
        Puzzle puzzle = puzzle(
                1,
                "Variables",
                "Build a valid Java variable declaration.",
                List.of("types", "variables", "assignment"),
                VariableDeclarationValidator.getInstance()
        );

        Room room = simpleRoom(1, tokens("int", "x", "=", "5", ";"), puzzle);
        return level(
                1,
                1,
                1,
                STAGE_1,
                "Variable Vault",
                "Variable declarations",
                "A variable declaration is TYPE name = value;. Java checks that the value matches the type.",
                "Build: int x = 5;",
                room
        );
    }

    private Level createPrintLevel() {
        Puzzle puzzle = puzzle(
                2,
                "Printing",
                "Build a statement that prints \"Hello\".",
                List.of("print statements"),
                new PrintStatementValidator("\"Hello\"")
        );

        Room room = simpleRoom(2, tokens("System.out.println", "(", "\"Hello\"", ")", ";", "print", "\"Hi\""), puzzle);
        return level(
                2,
                1,
                2,
                STAGE_1,
                "Print Hall",
                "Printing",
                "System.out.println sends a value to the output window.",
                "Build: System.out.println(\"Hello\");",
                room
        );
    }

    private Level createVariableThenPrintLevel() {
        Puzzle puzzle = puzzle(
                3,
                "Variable Output",
                "Declare score as 10, then print score.",
                List.of("variables", "printing"),
                new VariableThenPrintValidator("int", "score")
        );

        Room room = simpleRoom(3, tokens("int", "score", "=", "10", ";", "System.out.println", "(", "score", ")", ";"), puzzle);
        return level(
                3,
                1,
                3,
                STAGE_1,
                "Output Workshop",
                "Variables and printing",
                "A variable can be used after it is declared. Printing the name uses its stored value.",
                "Hint: This solution has two statements: declare score, then print score.",
                room
        );
    }

    private Level createIfStatementLevel() {
        Puzzle puzzle = puzzle(
                4,
                "If Statements",
                "Build a valid Java if-statement.",
                List.of("conditions", "comparison operators", "blocks"),
                new IfStatementValidator()
        );

        List<Token> levelTokens = tokens("if", "(", ")", "{", "}", "x", "age", ">", ">=", "5", "18", "true", "=", "==", ";");
        levelTokens.add(specialToken("Goal", 74, Constants.ROOM_HEIGHT - 108, TokenType.GOAL));
        levelTokens.add(specialToken("Helper", 210, Constants.ROOM_HEIGHT - 108, TokenType.HELPER));

        Room room = simpleRoom(4, levelTokens, puzzle);
        return level(
                4,
                2,
                1,
                STAGE_2,
                "Condition Chamber",
                "If-statements",
                "An if-statement runs its block only when the condition is true.",
                "Hint: Use if, parentheses, a comparison like x > 5, and braces.",
                room
        );
    }

    private Level createVariableThenIfLevel() {
        Puzzle puzzle = puzzle(
                5,
                "Variable and If",
                "Declare a variable named x and then make a true if-statement using x.",
                List.of("variables", "conditions", "if-statements"),
                new VariableThenIfValidator()
        );
        List<ChestReward> rewards = shuffledRewards(List.of("int", "x", "=", "5", ";", "if", "(", "x", ">", "3", ")", "{", "}"));
        GridRoomBuilder.GridRoomLayout mazeLayout = layoutForLevel(
                5,
                () -> RoomLayoutBuilder.gridTrainingMaze(rewards.size() + 1)
        );
        MultipleChoiceQuestion challengeQuestion = questionForLevel(5).orElse(null);

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                visibleTokensForLevel(5, List.of()),
                mazeLayout.walls(),
                mazeLayout.chests(),
                chestRewardsForLevel(5, rewards),
                finalChestRewardForLevel(5, ChestReward.goal()),
                createGridExitDoor(),
                mazeLayout.challengeDoor(),
                challengeQuestion,
                puzzle
        );

        return level(
                5,
                2,
                2,
                STAGE_2,
                "Logic Lock",
                "Variables and if-statements",
                "",
                "Hint: The solution uses two statements. Declare x first, then compare x.",
                room
        );
    }

    private Level createIfElsePrintLevel() {
        Puzzle puzzle = puzzle(
                6,
                "If Else",
                "Create an if-else statement: if grade is higher than x, print \"passed\". Otherwise, print \"failed\".",
                List.of("if-else", "variables", "print statements"),
                new IfElsePrintValidator()
        );

        List<ChestReward> rewards = shuffledRewards(List.of(
                "if", "(", "grade", ">", ")", "{",
                "System.out.println", "(", "\"passed\"", ")", ";", "}",
                "else", "{", "System.out.println", "(", "\"failed\"", ")", ";", "}",
                "<", "=="
        ));
        GridRoomBuilder.GridRoomLayout mazeLayout = layoutForLevel(
                6,
                () -> RoomLayoutBuilder.gridLockedRoomMaze(rewards.size() + 1, 4)
        );
        MultipleChoiceQuestion challengeQuestion = questionForLevel(6).orElseGet(() -> new MultipleChoiceQuestion(
                "Fill in the blank in this code:",
                "_____ x = 56.0;",
                List.of("double", "int", "String", "boolean"),
                "double",
                ChestReward.code("x")
        ));
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                visibleTokensForLevel(6, List.of()),
                mazeLayout.walls(),
                mazeLayout.chests(),
                chestRewardsForLevel(6, rewards),
                finalChestRewardForLevel(6, ChestReward.goal()),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                challengeQuestion,
                puzzle
        );

        return level(
                6,
                2,
                3,
                STAGE_2,
                "Question Room",
                "If-else statements",
                "",
                "Hint: The room question gives you x. Use grade > x.",
                room
        );
    }

    private Level createGreaterEqualsBranchLevel() {
        Puzzle puzzle = puzzle(
                7,
                "Greater Or Equal",
                "If score is at least pass, print \"unlocked\". Otherwise, print \"locked\".",
                List.of("if-else", "greater-or-equal"),
                new IfElsePrintValidator("score", "pass", BinaryExpr.Operator.GREATER_EQUALS, "unlocked", "locked")
        );

        Room room = simpleRoom(7, tokens(
                "if", "(", "score", ">=", "pass", ")", "{",
                "System.out.println", "(", "\"unlocked\"", ")", ";", "}",
                "else", "{", "System.out.println", "(", "\"locked\"", ")", ";", "}",
                ">", "=="
        ), puzzle);
        return level(
                7,
                2,
                4,
                STAGE_2,
                "Threshold Gate",
                "Comparison branches",
                ">= means greater than or equal to, which includes the exact passing value.",
                "Hint: The true branch prints unlocked; the else branch prints locked.",
                room
        );
    }

    private Level createBooleanBranchLevel() {
        Puzzle puzzle = puzzle(
                8,
                "Boolean Branch",
                "If hasKey is true, print \"open\". Otherwise, print \"closed\".",
                List.of("if-else", "booleans"),
                new IfElsePrintValidator("hasKey", "true", BinaryExpr.Operator.EQUALS, "open", "closed")
        );

        Room room = simpleRoom(8, tokens(
                "if", "(", "hasKey", "==", "true", ")", "{",
                "System.out.println", "(", "\"open\"", ")", ";", "}",
                "else", "{", "System.out.println", "(", "\"closed\"", ")", ";", "}",
                "false", "="
        ), puzzle);
        return level(
                8,
                2,
                5,
                STAGE_2,
                "Key Branch",
                "Boolean if-else",
                "Boolean values are already true or false, so they fit directly into conditions.",
                "Hint: Compare hasKey to true for this puzzle.",
                room
        );
    }

    private Level createStringDeclarationLevel() {
        Puzzle puzzle = puzzle(
                9,
                "String Declaration",
                "Build a valid String declaration statement.",
                List.of("String variables", "literal values"),
                new StringDeclarationValidator("\"Ammar\"")
        );

        List<Token> levelTokens = tokens("int", "String", "char", "age", "name", "grade", "=", "5", "\"Ammar\"", "'A'", ";");
        levelTokens.add(specialToken("Goal", 74, Constants.ROOM_HEIGHT - 108, TokenType.GOAL));
        Room room = simpleRoom(9, levelTokens, puzzle);
        return level(
                9,
                3,
                1,
                STAGE_3,
                "String Vault",
                "String declarations",
                "A String stores text inside double quotes.",
                "Build: String name = \"Ammar\";",
                room
        );
    }

    private Level createCharDeclarationLevel() {
        Puzzle puzzle = puzzle(
                10,
                "Char Declaration",
                "Build a char declaration that stores 'A'.",
                List.of("char variables", "literal values"),
                new CharDeclarationValidator("'A'")
        );

        Room room = simpleRoom(10, tokens("char", "grade", "=", "'A'", ";", "String", "\"A\"", "\"Ammar\""), puzzle);
        return level(
                10,
                3,
                2,
                STAGE_3,
                "Char Key",
                "Char declarations",
                "A char stores one character inside single quotes.",
                "Build: char grade = 'A';",
                room
        );
    }

    private Level createPrintMethodLevel() {
        Puzzle puzzle = puzzle(
                11,
                "Print Method",
                "Build a method named greet that prints \"Hi\".",
                List.of("methods", "printing"),
                MethodDeclarationValidator.printMethod("void", "greet", "\"Hi\"")
        );

        Room room = simpleRoom(11, tokens("void", "greet", "(", ")", "{", "System.out.println", "(", "\"Hi\"", ")", ";", "}", "return"), puzzle);
        return level(
                11,
                3,
                3,
                STAGE_3,
                "Function Hall",
                "Void methods",
                "A void method performs an action and does not return a value.",
                "Build: void greet() { System.out.println(\"Hi\"); }",
                room
        );
    }

    private Level createReturnMethodLevel() {
        Puzzle puzzle = puzzle(
                12,
                "Return Method",
                "Build a method named getName that returns \"Ammar\".",
                List.of("methods", "return values"),
                MethodDeclarationValidator.returnMethod("String", "getName", "\"Ammar\"")
        );

        Room room = simpleRoom(12, tokens("String", "getName", "(", ")", "{", "return", "\"Ammar\"", ";", "}", "void", "\"Hi\""), puzzle);
        return level(
                12,
                3,
                4,
                STAGE_3,
                "Return Room",
                "Return methods",
                "A non-void method sends a value back with return.",
                "Build: String getName() { return \"Ammar\"; }",
                room
        );
    }

    private Level createWhileLoopLevel() {
        Puzzle puzzle = puzzle(
                13,
                "While Loop",
                "Build a while loop that runs while count is less than 3 and increases count.",
                List.of("while loops", "increments"),
                new WhileLoopValidator("count", "3")
        );

        Room room = simpleRoom(13, tokens("while", "(", "count", "<", "3", ")", "{", "count", "++", ";", "}", ">", "="), puzzle);
        return level(
                13,
                4,
                1,
                STAGE_4,
                "While Maze",
                "While loops",
                "A while loop repeats while its condition stays true.",
                "Build: while (count < 3) { count++; }",
                room
        );
    }

    private Level createForLoopIndexLevel() {
        Puzzle puzzle = puzzle(
                14,
                "For Loop",
                "Build a for loop that prints i while i counts from 0 to 2.",
                List.of("for loops", "printing"),
                new ForLoopValidator("i", "3", "i")
        );

        Room room = simpleRoom(14, tokens(
                "for", "(", "int", "i", "=", "0", ";", "i", "<", "3", ";", "i", "++", ")",
                "{", "System.out.println", "(", "i", ")", ";", "}"
        ), puzzle);
        return level(
                14,
                4,
                2,
                STAGE_4,
                "Counter Circuit",
                "For loops",
                "A for loop keeps setup, condition, and update in one header.",
                "Hint: Start i at 0, loop while i < 3, then print i.",
                room
        );
    }

    private Level createForLoopMessageLevel() {
        Puzzle puzzle = puzzle(
                15,
                "Loop Message",
                "Build a for loop that prints \"Loop\" twice.",
                List.of("for loops", "string output"),
                new ForLoopValidator("i", "2", "\"Loop\"")
        );

        Room room = simpleRoom(15, tokens(
                "for", "(", "int", "i", "=", "0", ";", "i", "<", "2", ";", "i", "++", ")",
                "{", "System.out.println", "(", "\"Loop\"", ")", ";", "}", "\"Done\""
        ), puzzle);
        return level(
                15,
                4,
                3,
                STAGE_4,
                "Repeat Printer",
                "Looped printing",
                "The same statement can run many times inside a loop body.",
                "Build a for loop that prints \"Loop\".",
                room
        );
    }

    private Level createWhileLoopLivesLevel() {
        Puzzle puzzle = puzzle(
                16,
                "Loop Counter",
                "Build a while loop that increases lives while lives is less than 5.",
                List.of("while loops", "counter updates"),
                new WhileLoopValidator("lives", "5")
        );

        List<ChestReward> rewards = shuffledRewards(List.of("while", "(", "lives", "<", "5", ")", "{", "lives", "++", ";", "}"));
        GridRoomBuilder.GridRoomLayout mazeLayout = layoutForLevel(
                16,
                () -> RoomLayoutBuilder.gridTrainingMaze(rewards.size() + 1)
        );
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                visibleTokensForLevel(16, List.of()),
                mazeLayout.walls(),
                mazeLayout.chests(),
                chestRewardsForLevel(16, rewards),
                finalChestRewardForLevel(16, ChestReward.goal()),
                createExitDoor(),
                puzzle
        );
        return level(
                16,
                4,
                4,
                STAGE_4,
                "Loop Labyrinth",
                "While loop counters",
                "Updating the counter is what lets a while loop eventually stop.",
                "Hint: Use lives++ inside the loop body.",
                room
        );
    }

    private Level createClassBlueprintLevel() {
        Puzzle puzzle = puzzle(
                17,
                "Class Blueprint",
                "Build a class named Item with a String name field and an int power field.",
                List.of("classes", "fields"),
                new ClassFieldsValidator()
        );

        GridRoomBuilder.GridRoomLayout mazeLayout = layoutForLevel(17, () -> RoomLayoutBuilder.gridLockedRoomMaze(1, 1));
        MultipleChoiceQuestion challengeQuestion = questionForLevel(17).orElseGet(() -> new MultipleChoiceQuestion(
                "Which type should the power field use?",
                "_____ power;",
                List.of("int", "String", "boolean", "class"),
                "int",
                ChestReward.code("power")
        ));
        List<Token> levelTokens = tokens("class", "Item", "{", "String", "name", ";", "int", ";", "}");
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                visibleTokensForLevel(17, levelTokens),
                mazeLayout.walls(),
                mazeLayout.chests(),
                chestRewardsForLevel(17, List.of()),
                finalChestRewardForLevel(17, ChestReward.goal()),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                challengeQuestion,
                puzzle
        );

        return level(
                17,
                5,
                1,
                STAGE_5,
                "Class Blueprint",
                "Classes and fields",
                "A class is a blueprint. Fields describe the data each object can store.",
                "Hint: The class name is Item, and it has fields String name and int power.",
                room
        );
    }

    private Level createConstructorForgeLevel() {
        Puzzle puzzle = puzzle(
                18,
                "Constructor Forge",
                "Build a Player class with fields, a constructor, and a heal method.",
                List.of("classes", "constructors", "methods"),
                new ClassConstructorMethodValidator()
        );

        List<ChestReward> rewards = shuffledRewards(List.of(
                "class", "Player", "{", "String", "name", ";",
                "int", "health", ";", "Player", "(", "String",
                "name", ",", "int", "health", ")", "{",
                "this.name", "=", "name", ";", "this.health", "=",
                "health", ";", "}", "void", "(", ")", "{",
                "health", "=", "health", "+", "1", ";", "}", "}"
        ));
        GridRoomBuilder.GridRoomLayout mazeLayout = layoutForLevel(
                18,
                () -> RoomLayoutBuilder.gridLockedRoomMaze(rewards.size() + 1, 6)
        );
        MultipleChoiceQuestion challengeQuestion = questionForLevel(18).orElseGet(() -> new MultipleChoiceQuestion(
                "Which method name should increase health?",
                "void _____() { health = health + 1; }",
                List.of("heal", "class", "Player", "new"),
                "heal",
                ChestReward.code("heal")
        ));
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                visibleTokensForLevel(18, List.of()),
                mazeLayout.walls(),
                mazeLayout.chests(),
                chestRewardsForLevel(18, rewards),
                finalChestRewardForLevel(18, ChestReward.goal()),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                challengeQuestion,
                puzzle
        );

        return level(
                18,
                5,
                2,
                STAGE_5,
                "Constructor Forge",
                "Constructors and methods",
                "A constructor prepares a new object by assigning its fields.",
                "Hint: Assign both fields in the constructor, then write void heal().",
                room
        );
    }

    private Level createObjectLockLevel() {
        Puzzle finalPuzzle = puzzle(
                19,
                "Object Lock",
                "Create an Item object named key, then call key.use().",
                List.of("objects", "constructors", "method calls"),
                new ObjectCreateAndCallValidator()
        );
        Puzzle objectPuzzle = puzzle(
                "ironChest",
                "Set the ironChest locked field to false.",
                List.of("objects", "fields", "assignment"),
                new ObjectFieldAssignmentValidator()
        );

        GridRoomBuilder.GridRoomLayout mazeLayout = layoutForLevel(19, RoomLayoutBuilder::gridObjectLockMaze);
        Chest goalChest = goalChestForLevel(19, RoomLayoutBuilder.gridChest(10, 3, 56, 42, true));
        ProgrammableObject ironChest = new ProgrammableObject(
                "ironChest",
                "ironChest",
                610,
                270,
                118,
                58,
                objectPuzzle,
                goalChest
        );

        List<Token> levelTokens = tokens(
                "ironChest", ".", "locked", "=", "false", ";",
                "Item", "key", "=", "new", "Item", "(",
                "\"key\"", ")", ";", "key", ".", "(", ")", ";"
        );
        MultipleChoiceQuestion challengeQuestion = questionForLevel(19).orElseGet(() -> new MultipleChoiceQuestion(
                "Which method should the key object call?",
                "key._____();",
                List.of("use", "new", "locked", "class"),
                "use",
                ChestReward.code("use")
        ));
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                visibleTokensForLevel(19, levelTokens),
                mazeLayout.walls(),
                List.of(goalChest),
                List.of(ironChest),
                chestRewardsForLevel(19, List.of()),
                finalChestRewardForLevel(19, ChestReward.goal()),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                challengeQuestion,
                finalPuzzle
        );

        return level(
                19,
                5,
                3,
                STAGE_5,
                "Object Lock",
                "Objects and field access",
                "Objects are created from classes. Dot access reaches an object's fields and methods.",
                "Hint: First program ironChest, then create the key object and call use.",
                room
        );
    }

    private Level createVariablesCapstoneLevel() {
        Puzzle puzzle = puzzle(
                20,
                "Variables Capstone",
                "Build a score variable set to 42, then print score.",
                List.of("variables", "assignment", "printing"),
                new VariableThenPrintValidator("int", "score")
        );

        Room room = simpleRoom(20, tokens(
                "int", "score", "=", "42", ";",
                "System.out.println", "(", "score", ")", ";",
                "\"42\"", "double"
        ), puzzle);
        return level(
                20,
                1,
                4,
                STAGE_1,
                "Console Checkpoint",
                "Variables capstone",
                "This capstone combines declaration and output in one clean two-step solution.",
                "Build the variable first, then print the variable name instead of the literal.",
                room
        );
    }

    private Level createConditionsDebugCapstoneLevel() {
        Puzzle puzzle = puzzle(
                21,
                "Branch Debug Drill",
                """
                This code is almost correct, but one token is wrong. Fix it:
                if (score >= pass) { System.out.println("pass"); } else { System.out.println("retry"); }
                """,
                List.of("if-else", "comparison operators", "debugging"),
                new AcceptedAnswerValidator(List.of(
                        "if (score > pass) { System.out.println(\"pass\"); } else { System.out.println(\"retry\"); }"
                ))
        );

        Room room = simpleRoom(21, tokens(
                "if", "(", "score", ">", "pass", ")", "{",
                "System.out.println", "(", "\"pass\"", ")", ";", "}",
                "else", "{", "System.out.println", "(", "\"retry\"", ")", ";", "}",
                ">=", "<", "=="
        ), puzzle);
        return level(
                21,
                2,
                6,
                STAGE_2,
                "Branch Debug Drill",
                "Condition debugging capstone",
                "Debugging means reading the existing logic closely and replacing only the token that breaks the rule.",
                "The branch text is already correct. Focus on the comparison operator.",
                room
        );
    }

    private Level createMethodsDebugCapstoneLevel() {
        Puzzle puzzle = puzzle(
                22,
                "Method Repair",
                """
                This method almost works, but two details are wrong. Fix it:
                String greet() { return "Hi"; }
                """,
                List.of("methods", "return values", "debugging"),
                new AcceptedAnswerValidator(List.of(
                        "void greet() { System.out.println(\"Hi\"); }"
                ))
        );

        Room room = simpleRoom(22, tokens(
                "void", "String", "greet", "(", ")", "{",
                "System.out.println", "(", "\"Hi\"", ")", ";",
                "return", "\"Hi\"", "}"
        ), puzzle);
        return level(
                22,
                3,
                5,
                STAGE_3,
                "Method Repair",
                "Method debugging capstone",
                "This capstone checks whether you can spot when a method should act with print instead of return a value.",
                "Replace the wrong return shape with the same print pattern you used earlier.",
                room
        );
    }

    private Level createLoopsCapstoneLevel() {
        Puzzle puzzle = puzzle(
                23,
                "Loops Capstone",
                "Build a for loop that prints i while i counts from 0 to 3.",
                List.of("for loops", "counter updates", "printing"),
                new ForLoopValidator("i", "4", "i")
        );

        Room room = simpleRoom(23, tokens(
                "for", "(", "int", "i", "=", "0", ";", "i", "<", "4", ";", "i", "++", ")",
                "{", "System.out.println", "(", "i", ")", ";", "}",
                "while", "3"
        ), puzzle);
        return level(
                23,
                4,
                5,
                STAGE_4,
                "Loop Relay",
                "Loops capstone",
                "This capstone combines loop setup, stopping condition, update, and printed output in one pattern.",
                "Keep the classic for-loop header together: setup, compare, then increment.",
                room
        );
    }

    private Level createObjectsDebugCapstoneLevel() {
        Puzzle puzzle = puzzle(
                24,
                "Object Repair",
                """
                This object code almost works, but one call is wrong. Fix it:
                Item key = new Item("key"); key.unlock();
                """,
                List.of("objects", "constructors", "method calls", "debugging"),
                new ObjectCreateAndCallValidator()
        );

        Room room = simpleRoom(24, tokens(
                "Item", "key", "=", "new", "Item", "(",
                "\"key\"", ")", ";", "key", ".", "use", "(", ")", ";",
                "unlock"
        ), puzzle);
        return level(
                24,
                5,
                4,
                STAGE_5,
                "Object Repair",
                "Objects debugging capstone",
                "The fix is small, but it proves you can read object code and spot the wrong method call.",
                "The object creation line is already right. Repair the method name after the dot.",
                room
        );
    }

    private Level createStageOneRevisionWing() {
        Puzzle puzzle = puzzle(
                "Revision Wing: Stage 1",
                "Rebuild the basics in one route: declare score as 7, then print score.",
                List.of("variables", "printing", "revision"),
                new VariableThenPrintValidator("int", "score")
        );

        Room room = simpleRoom(REVISION_STAGE_1_LEVEL, tokens(
                "int", "score", "=", "7", ";",
                "System.out.println", "(", "score", ")", ";",
                "\"7\"", "String"
        ), puzzle);
        return specialLevel(
                REVISION_STAGE_1_LEVEL,
                1,
                "REV-1",
                "Stage 1 Revision Wing",
                "Variables and printing refresh",
                "One compact room that makes you declare a value and use it immediately in output before Stage 2 starts.",
                "Build the variable first, then print the variable name.",
                room
        );
    }

    private Level createStageTwoRevisionWing() {
        Puzzle puzzle = puzzle(
                "Revision Wing: Stage 2",
                "Declare int score = 8; then use if/else to print \"open\" when score > 5, otherwise print \"wait\".",
                List.of("variables", "if-else", "printing", "revision"),
                new AcceptedAnswerValidator(List.of(
                        "int score = 8; if (score > 5) { System.out.println(\"open\"); } else { System.out.println(\"wait\"); }"
                ))
        );

        Room room = simpleRoom(REVISION_STAGE_2_LEVEL, tokens(
                "int", "score", "=", "8", ";",
                "if", "(", "score", ">", "5", ")", "{",
                "System.out.println", "(", "\"open\"", ")", ";", "}",
                "else", "{", "System.out.println", "(", "\"wait\"", ")", ";", "}",
                ">=", "=="
        ), puzzle);
        return specialLevel(
                REVISION_STAGE_2_LEVEL,
                2,
                "REV-2",
                "Stage 2 Revision Wing",
                "Variable plus branch refresh",
                "This bridge room rechecks declaration, comparison, and two-branch output before the text-and-method stage.",
                "There are three steps: declare, branch, then print inside both outcomes.",
                room
        );
    }

    private Level createStageThreeRevisionWing() {
        Puzzle puzzle = puzzle(
                "Revision Wing: Stage 3",
                "Build a method named tag that returns the String \"Ammar-A\".",
                List.of("String variables", "char variables", "methods", "revision"),
                new AcceptedAnswerValidator(List.of(
                        "String tag() { return \"Ammar-A\"; }"
                ))
        );

        Room room = simpleRoom(REVISION_STAGE_3_LEVEL, tokens(
                "String", "tag", "(", ")", "{", "return", "\"Ammar-A\"", ";", "}",
                "void", "\"Ammar\"", "'A'"
        ), puzzle);
        return specialLevel(
                REVISION_STAGE_3_LEVEL,
                3,
                "REV-3",
                "Stage 3 Revision Wing",
                "String and method refresh",
                "This bridge room condenses Stage 3 into one clean return-method pattern anchored on text values.",
                "Use the return-method structure, not a print method.",
                room
        );
    }

    private Level createStageFourRevisionWing() {
        Puzzle puzzle = puzzle(
                "Revision Wing: Stage 4",
                "Build a for loop that prints i while i counts from 1 to 4.",
                List.of("for loops", "printing", "revision"),
                new AcceptedAnswerValidator(List.of(
                        "for (int i = 1; i < 5; i++) { System.out.println(i); }"
                ))
        );

        Room room = simpleRoom(REVISION_STAGE_4_LEVEL, tokens(
                "for", "(", "int", "i", "=", "1", ";", "i", "<", "5", ";", "i", "++", ")",
                "{", "System.out.println", "(", "i", ")", ";", "}",
                "while", "0", "4"
        ), puzzle);
        return specialLevel(
                REVISION_STAGE_4_LEVEL,
                4,
                "REV-4",
                "Stage 4 Revision Wing",
                "Loop control refresh",
                "This bridge room checks whether you can set a loop range intentionally instead of copying the earlier starter pattern.",
                "The loop starts at 1 and stops before 5.",
                room
        );
    }

    private Level createStageOneBossLevel() {
        Puzzle puzzle = puzzle(
                "Stage 1 Boss Room",
                "Declare int gems = 12; then print gems.",
                List.of("variables", "printing", "synthesis"),
                new VariableThenPrintValidator("int", "gems")
        );

        Room room = simpleRoom(BOSS_STAGE_1_LEVEL, tokens(
                "int", "gems", "=", "12", ";",
                "System.out.println", "(", "gems", ")", ";",
                "\"12\"", "double"
        ), puzzle);
        return specialLevel(
                BOSS_STAGE_1_LEVEL,
                1,
                "BOSS-1",
                "Stage 1 Boss: Signal Forge",
                "Variables plus printing synthesis",
                "The Stage 1 boss compresses the full basic loop into one clean declaration-to-output sequence.",
                "Build the value first, then route the variable into print.",
                room
        );
    }

    private Level createStageTwoBossLevel() {
        Puzzle puzzle = puzzle(
                "Stage 2 Boss Room",
                "Declare int score = 9; then print \"pass\" when score >= 8, otherwise print \"retry\".",
                List.of("variables", "if-else", "printing", "synthesis"),
                new AcceptedAnswerValidator(List.of(
                        "int score = 9; if (score >= 8) { System.out.println(\"pass\"); } else { System.out.println(\"retry\"); }"
                ))
        );

        Room room = simpleRoom(BOSS_STAGE_2_LEVEL, tokens(
                "int", "score", "=", "9", ";",
                "if", "(", "score", ">=", "8", ")", "{",
                "System.out.println", "(", "\"pass\"", ")", ";", "}",
                "else", "{", "System.out.println", "(", "\"retry\"", ")", ";", "}",
                ">", "=="
        ), puzzle);
        return specialLevel(
                BOSS_STAGE_2_LEVEL,
                2,
                "BOSS-2",
                "Stage 2 Boss: Branch Sentinel",
                "Variable plus branch synthesis",
                "This boss makes you set state and then route two explicit outcomes from the comparison.",
                "It is the same three-part flow every time: declare, compare, print each branch.",
                room
        );
    }

    private Level createStageThreeBossLevel() {
        Puzzle puzzle = puzzle(
                "Stage 3 Boss Room",
                "Build a method named badge that returns \"Ammar-A\".",
                List.of("String variables", "methods", "return values", "synthesis"),
                new AcceptedAnswerValidator(List.of(
                        "String badge() { return \"Ammar-A\"; }"
                ))
        );

        Room room = simpleRoom(BOSS_STAGE_3_LEVEL, tokens(
                "String", "badge", "(", ")", "{", "return", "\"Ammar-A\"", ";", "}",
                "void", "System.out.println", "\"Ammar\"", "'A'"
        ), puzzle);
        return specialLevel(
                BOSS_STAGE_3_LEVEL,
                3,
                "BOSS-3",
                "Stage 3 Boss: Signature Vault",
                "String and method synthesis",
                "This boss checks whether you can package a text value inside the right method shape instead of defaulting to print.",
                "The method returns a String, so it needs a return statement inside braces.",
                room
        );
    }

    private Level createStageFourBossLevel() {
        Puzzle puzzle = puzzle(
                "Stage 4 Boss Room",
                "Build a for loop that prints i while i counts from 0 to 4.",
                List.of("for loops", "printing", "synthesis"),
                new ForLoopValidator("i", "5", "i")
        );

        Room room = simpleRoom(BOSS_STAGE_4_LEVEL, tokens(
                "for", "(", "int", "i", "=", "0", ";", "i", "<", "5", ";", "i", "++", ")",
                "{", "System.out.println", "(", "i", ")", ";", "}",
                "while", "4"
        ), puzzle);
        return specialLevel(
                BOSS_STAGE_4_LEVEL,
                4,
                "BOSS-4",
                "Stage 4 Boss: Loop Reactor",
                "Loop setup and output synthesis",
                "This boss forces the full loop header and output path to stay stable across a slightly extended range.",
                "Use the standard for-loop header, but count up to 4 instead of 3.",
                room
        );
    }

    private Level createStageFiveBossLevel() {
        Puzzle puzzle = puzzle(
                "Stage 5 Boss Room",
                "Build a Player class with fields, a constructor, and a heal method.",
                List.of("classes", "constructors", "methods", "synthesis"),
                new ClassConstructorMethodValidator()
        );

        Room room = simpleRoom(BOSS_STAGE_5_LEVEL, tokens(
                "class", "Player", "{", "String", "name", ";",
                "int", "health", ";", "Player", "(", "String", "name", ",", "int", "health", ")", "{",
                "this.name", "=", "name", ";", "this.health", "=", "health", ";", "}",
                "void", "heal", "(", ")", "{", "health", "=", "health", "+", "1", ";", "}", "}"
        ), puzzle);
        return specialLevel(
                BOSS_STAGE_5_LEVEL,
                5,
                "BOSS-5",
                "Stage 5 Boss: Constructor Core",
                "Class, constructor, and method synthesis",
                "The final boss room asks for the whole object blueprint loop in one pass: fields, setup, then behavior.",
                "Write the class shell first, then the constructor, then heal().",
                room
        );
    }

    private Puzzle puzzle(int levelNumber, String title, String instructions, List<String> concepts, CodeValidator validator) {
        Optional<LevelLayoutOverride> override = layoutOverrideStore.load(levelNumber);
        String effectiveTitle = override
                .map(LevelLayoutOverride::goalTitle)
                .orElse(null);
        String effectiveInstructions = override
                .map(LevelLayoutOverride::goalInstructions)
                .orElse(null);
        List<String> acceptedAnswers = override
                .map(LevelLayoutOverride::acceptedAnswers)
                .orElse(List.of());
        CodeValidator effectiveValidator = acceptedAnswers.isEmpty()
                ? validator
                : new AcceptedAnswerValidator(acceptedAnswers);
        return new Puzzle(
                effectiveTitle == null ? title : effectiveTitle,
                effectiveInstructions == null ? instructions : effectiveInstructions,
                concepts,
                effectiveValidator
        );
    }

    private Puzzle puzzle(String title, String instructions, List<String> concepts, CodeValidator validator) {
        return new Puzzle(title, instructions, concepts, validator);
    }

    private Level level(
            int levelNumber,
            int stageNumber,
            int stageLevelNumber,
            String stageTitle,
            String name,
            String concept,
            String completionExplanation,
            String goalHelper,
            Room room
    ) {
        return new Level(
                levelNumber,
                stageNumber,
                stageLevelNumber,
                stageTitle,
                name,
                concept,
                completionExplanation,
                overriddenHelper(levelNumber, goalHelper),
                room
        );
    }

    private Level specialLevel(
            int levelNumber,
            int stageNumber,
            String displayId,
            String name,
            String concept,
            String completionExplanation,
            String goalHelper,
            Room room
    ) {
        return new Level(
                levelNumber,
                stageNumber,
                99,
                "Special Routes",
                name,
                concept,
                completionExplanation,
                goalHelper,
                displayId,
                room
        );
    }

    private Room simpleRoom(int levelNumber, List<Token> levelTokens, Puzzle puzzle) {
        List<Token> effectiveTokens = visibleTokensForLevel(levelNumber, levelTokens);
        List<ChestReward> effectiveChestRewards = chestRewardsForLevel(levelNumber, List.of());
        Optional<GridRoomBuilder.GridRoomLayout> overrideLayout = overrideLayoutForLevel(levelNumber);
        if (overrideLayout.isEmpty()) {
            if (effectiveChestRewards.isEmpty()) {
                return new Room(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT, effectiveTokens, createExitDoor(), puzzle);
            }

            return new Room(
                    Constants.ROOM_WIDTH,
                    Constants.ROOM_HEIGHT,
                    effectiveTokens,
                    List.of(),
                    List.of(),
                    effectiveChestRewards,
                    null,
                    createExitDoor(),
                    puzzle
            );
        }

        GridRoomBuilder.GridRoomLayout layout = overrideLayout.get();
        return new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                effectiveTokens,
                layout.walls(),
                layout.chests(),
                effectiveChestRewards,
                finalChestRewardForLevel(levelNumber, null),
                createExitDoor(),
                layout.challengeDoor(),
                questionForLevel(levelNumber).orElse(null),
                puzzle
        );
    }

    private GridRoomBuilder.GridRoomLayout layoutForLevel(
            int levelNumber,
            Supplier<GridRoomBuilder.GridRoomLayout> defaultLayout
    ) {
        return overrideLayoutForLevel(levelNumber).orElseGet(defaultLayout);
    }

    private Optional<GridRoomBuilder.GridRoomLayout> overrideLayoutForLevel(int levelNumber) {
        return layoutOverrideStore.load(levelNumber).flatMap(override -> {
            try {
                return Optional.of(RoomLayoutBuilder.fromOverride(override));
            } catch (RuntimeException exception) {
                System.err.println("Ignoring invalid layout override for level " + levelNumber + ": " + exception.getMessage());
                return Optional.empty();
            }
        });
    }

    private Optional<MultipleChoiceQuestion> questionForLevel(int levelNumber) {
        return layoutOverrideStore.load(levelNumber)
                .flatMap(override -> override.questionDoors().stream().findFirst())
                .map(door -> new MultipleChoiceQuestion(
                        door.prompt(),
                        door.code(),
                        door.choices().isEmpty() ? List.of(door.correctAnswer()) : door.choices(),
                        door.correctAnswer(),
                        ChestReward.code(door.reward())
                ));
    }

    private String overriddenHelper(int levelNumber, String defaultHelper) {
        Optional<LevelLayoutOverride> override = layoutOverrideStore.load(levelNumber);
        if (override.isEmpty() || override.get().helperText() == null) {
            return defaultHelper;
        }

        return override.get().helperText();
    }

    private List<Token> visibleTokensForLevel(int levelNumber, List<Token> defaultTokens) {
        Optional<LevelLayoutOverride> override = layoutOverrideStore.load(levelNumber)
                .filter(LevelLayoutOverride::tokensOverridden);
        if (override.isEmpty()) {
            return defaultTokens;
        }

        return override.get().tokens().stream()
                .filter(token -> token.kind() == LevelLayoutOverride.TokenPlacementKind.VISIBLE)
                .map(this::visibleToken)
                .toList();
    }

    private List<ChestReward> chestRewardsForLevel(int levelNumber, List<ChestReward> defaultRewards) {
        Optional<LevelLayoutOverride> override = layoutOverrideStore.load(levelNumber)
                .filter(LevelLayoutOverride::tokensOverridden);
        if (override.isEmpty()) {
            return defaultRewards;
        }

        return override.get().tokens().stream()
                .filter(token -> token.kind() == LevelLayoutOverride.TokenPlacementKind.CHEST)
                .sorted(java.util.Comparator.comparingInt(LevelLayoutOverride.TokenPlacement::chestOrder))
                .map(token -> new ChestReward(token.value(), token.type()))
                .toList();
    }

    private ChestReward finalChestRewardForLevel(int levelNumber, ChestReward defaultReward) {
        return layoutOverrideStore.load(levelNumber)
                .filter(LevelLayoutOverride::tokensOverridden)
                .isPresent()
                ? null
                : defaultReward;
    }

    private Token visibleToken(LevelLayoutOverride.TokenPlacement token) {
        int column = token.column();
        int row = token.row();
        if (column == 0 && row == 0) {
            column = 1;
        }

        double width = tokenWidth(token.value());
        double x = RoomLayoutBuilder.GRID_ORIGIN_X
                + column * RoomLayoutBuilder.GRID_CELL_WIDTH
                + (RoomLayoutBuilder.GRID_CELL_WIDTH - width) / 2.0;
        double y = RoomLayoutBuilder.GRID_ORIGIN_Y
                + row * RoomLayoutBuilder.GRID_CELL_HEIGHT
                + (RoomLayoutBuilder.GRID_CELL_HEIGHT - TOKEN_HEIGHT) / 2.0;
        return new Token(token.value(), x, y, width, TOKEN_HEIGHT, token.type());
    }

    private Chest goalChestForLevel(int levelNumber, Chest defaultChest) {
        return layoutOverrideStore.load(levelNumber)
                .flatMap(override -> override.chests().stream().findFirst())
                .map(chest -> RoomLayoutBuilder.gridChest(chest.column(), chest.row(), 56, 42, true))
                .orElse(defaultChest);
    }

    private List<Token> tokens(String... values) {
        List<Token> tokens = new ArrayList<>();
        double x = TOKEN_LEFT;
        double y = TOKEN_TOP;
        for (String value : values) {
            double width = tokenWidth(value);
            if (x > TOKEN_LEFT && x + width > TOKEN_RIGHT_LIMIT) {
                x = TOKEN_LEFT;
                y += TOKEN_VERTICAL_GAP;
            }
            tokens.add(new Token(value, x, y, width, TOKEN_HEIGHT));
            x += width + TOKEN_HORIZONTAL_GAP;
        }
        return tokens;
    }

    private double tokenWidth(String value) {
        return Math.max(48, value.length() * 11 + 28);
    }

    private Token specialToken(String value, double x, double y, TokenType type) {
        return new Token(value, x, y, tokenWidth(value), TOKEN_HEIGHT, type);
    }

    private List<ChestReward> shuffledRewards(List<String> values) {
        List<ChestReward> rewards = new ArrayList<>();
        for (String value : values) {
            rewards.add(ChestReward.code(value));
        }
        Collections.shuffle(rewards);
        return rewards;
    }

    private Door createExitDoor() {
        return createGridExitDoor();
    }

    private Door createGridExitDoor() {
        double gridRightEdge = RoomLayoutBuilder.GRID_ORIGIN_X
                + RoomLayoutBuilder.GRID_COLUMNS * RoomLayoutBuilder.GRID_CELL_WIDTH;
        return new Door(gridRightEdge - 52, Constants.ROOM_HEIGHT / 2.0 - 59, 52, 118);
    }

    private record LevelDefinition(Supplier<Level> factory) {
        private Level create() {
            return factory.get();
        }
    }
}

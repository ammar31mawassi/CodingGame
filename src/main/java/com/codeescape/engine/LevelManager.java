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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class LevelManager {
    private static final String STAGE_1 = "Variables And Printing";
    private static final String STAGE_2 = "If-Else Conditions";
    private static final String STAGE_3 = "String, Char, And Functions";
    private static final String STAGE_4 = "Loops";
    private static final String STAGE_5 = "Classes";

    private final List<LevelDefinition> levelDefinitions = new ArrayList<>();
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
        currentLevelIndex = 0;
        currentLevel = null;

        add(this::createVariableLevel);
        add(this::createPrintLevel);
        add(this::createVariableThenPrintLevel);
        add(this::createIfStatementLevel);
        add(this::createVariableThenIfLevel);
        add(this::createIfElsePrintLevel);
        add(this::createGreaterEqualsBranchLevel);
        add(this::createBooleanBranchLevel);
        add(this::createStringDeclarationLevel);
        add(this::createCharDeclarationLevel);
        add(this::createPrintMethodLevel);
        add(this::createReturnMethodLevel);
        add(this::createWhileLoopLevel);
        add(this::createForLoopIndexLevel);
        add(this::createForLoopMessageLevel);
        add(this::createWhileLoopLivesLevel);
        add(this::createClassBlueprintLevel);
        add(this::createConstructorForgeLevel);
        add(this::createObjectLockLevel);
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

        throw new IllegalArgumentException("Unknown level: " + levelNumber);
    }

    private void add(Supplier<Level> factory) {
        levelDefinitions.add(new LevelDefinition(factory));
    }

    private LevelDefinition levelDefinition(int levelNumber) {
        return levelDefinitions.stream()
                .filter(definition -> definition.create().getLevelNumber() == levelNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown level: " + levelNumber));
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
        double width = tokenWidth(token.value());
        double x = RoomLayoutBuilder.GRID_ORIGIN_X
                + token.column() * RoomLayoutBuilder.GRID_CELL_WIDTH
                + (RoomLayoutBuilder.GRID_CELL_WIDTH - width) / 2.0;
        double y = RoomLayoutBuilder.GRID_ORIGIN_Y
                + token.row() * RoomLayoutBuilder.GRID_CELL_HEIGHT
                + (RoomLayoutBuilder.GRID_CELL_HEIGHT - 28) / 2.0;
        return new Token(token.value(), x, y, width, 28, token.type());
    }

    private Chest goalChestForLevel(int levelNumber, Chest defaultChest) {
        return layoutOverrideStore.load(levelNumber)
                .flatMap(override -> override.chests().stream().findFirst())
                .map(chest -> RoomLayoutBuilder.gridChest(chest.column(), chest.row(), 56, 42, true))
                .orElse(defaultChest);
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

    private List<ChestReward> shuffledRewards(List<String> values) {
        List<ChestReward> rewards = new ArrayList<>();
        for (String value : values) {
            rewards.add(ChestReward.code(value));
        }
        Collections.shuffle(rewards);
        return rewards;
    }

    private Door createExitDoor() {
        return RoomLayoutBuilder.rightExitDoor();
    }

    private Door createGridExitDoor() {
        double gridRightEdge = RoomLayoutBuilder.GRID_ORIGIN_X
                + RoomLayoutBuilder.GRID_COLUMNS * RoomLayoutBuilder.GRID_CELL_WIDTH;
        return new Door(gridRightEdge - 26, Constants.ROOM_HEIGHT / 2.0 - 59, 52, 118);
    }

    private record LevelDefinition(Supplier<Level> factory) {
        private Level create() {
            return factory.get();
        }
    }
}

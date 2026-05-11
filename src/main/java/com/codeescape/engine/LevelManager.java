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
import java.util.function.Supplier;

public class LevelManager {
    private static final String STAGE_1 = "Variables And Printing";
    private static final String STAGE_2 = "If-Else Conditions";
    private static final String STAGE_3 = "String, Char, And Functions";
    private static final String STAGE_4 = "Loops";
    private static final String STAGE_5 = "Classes";

    private final List<LevelDefinition> levelDefinitions = new ArrayList<>();
    private int currentLevelIndex;
    private Level currentLevel;

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
                "Variables",
                "Build a valid Java variable declaration.",
                List.of("types", "variables", "assignment"),
                VariableDeclarationValidator.getInstance()
        );

        Room room = simpleRoom(tokens("int", "x", "=", "5", ";"), puzzle);
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
                "Printing",
                "Build a statement that prints \"Hello\".",
                List.of("print statements"),
                new PrintStatementValidator("\"Hello\"")
        );

        Room room = simpleRoom(tokens("System.out.println", "(", "\"Hello\"", ")", ";", "print", "\"Hi\""), puzzle);
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
                "Variable Output",
                "Declare score as 10, then print score.",
                List.of("variables", "printing"),
                new VariableThenPrintValidator("int", "score")
        );

        Room room = simpleRoom(tokens("int", "score", "=", "10", ";", "System.out.println", "(", "score", ")", ";"), puzzle);
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
                "If Statements",
                "Build a valid Java if-statement.",
                List.of("conditions", "comparison operators", "blocks"),
                new IfStatementValidator()
        );

        List<Token> levelTokens = tokens("if", "(", ")", "{", "}", "x", "age", ">", ">=", "5", "18", "true", "=", "==", ";");
        levelTokens.add(specialToken("Goal", 74, Constants.ROOM_HEIGHT - 108, TokenType.GOAL));
        levelTokens.add(specialToken("Helper", 210, Constants.ROOM_HEIGHT - 108, TokenType.HELPER));

        Room room = simpleRoom(levelTokens, puzzle);
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
                "Variable and If",
                "Declare a variable named x and then make a true if-statement using x.",
                List.of("variables", "conditions", "if-statements"),
                new VariableThenIfValidator()
        );
        List<ChestReward> rewards = shuffledRewards(List.of("int", "x", "=", "5", ";", "if", "(", "x", ">", "3", ")", "{", "}"));
        GridRoomBuilder.GridRoomLayout mazeLayout = RoomLayoutBuilder.gridTrainingMaze(rewards.size() + 1);

        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                List.of(),
                mazeLayout.walls(),
                mazeLayout.chests(),
                rewards,
                ChestReward.goal(),
                createExitDoor(),
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
        GridRoomBuilder.GridRoomLayout mazeLayout = RoomLayoutBuilder.gridLockedRoomMaze(rewards.size() + 1, 4);
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                List.of(),
                mazeLayout.walls(),
                mazeLayout.chests(),
                rewards,
                ChestReward.goal(),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                new MultipleChoiceQuestion(
                        "Fill in the blank in this code:",
                        "_____ x = 56.0;",
                        List.of("double", "int", "String", "boolean"),
                        "double",
                        ChestReward.code("x")
                ),
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
                "Greater Or Equal",
                "If score is at least pass, print \"unlocked\". Otherwise, print \"locked\".",
                List.of("if-else", "greater-or-equal"),
                new IfElsePrintValidator("score", "pass", BinaryExpr.Operator.GREATER_EQUALS, "unlocked", "locked")
        );

        Room room = simpleRoom(tokens(
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
                "Boolean Branch",
                "If hasKey is true, print \"open\". Otherwise, print \"closed\".",
                List.of("if-else", "booleans"),
                new IfElsePrintValidator("hasKey", "true", BinaryExpr.Operator.EQUALS, "open", "closed")
        );

        Room room = simpleRoom(tokens(
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
                "String Declaration",
                "Build a valid String declaration statement.",
                List.of("String variables", "literal values"),
                new StringDeclarationValidator("\"Ammar\"")
        );

        List<Token> levelTokens = tokens("int", "String", "char", "age", "name", "grade", "=", "5", "\"Ammar\"", "'A'", ";");
        levelTokens.add(specialToken("Goal", 74, Constants.ROOM_HEIGHT - 108, TokenType.GOAL));
        Room room = simpleRoom(levelTokens, puzzle);
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
                "Char Declaration",
                "Build a char declaration that stores 'A'.",
                List.of("char variables", "literal values"),
                new CharDeclarationValidator("'A'")
        );

        Room room = simpleRoom(tokens("char", "grade", "=", "'A'", ";", "String", "\"A\"", "\"Ammar\""), puzzle);
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
                "Print Method",
                "Build a method named greet that prints \"Hi\".",
                List.of("methods", "printing"),
                MethodDeclarationValidator.printMethod("void", "greet", "\"Hi\"")
        );

        Room room = simpleRoom(tokens("void", "greet", "(", ")", "{", "System.out.println", "(", "\"Hi\"", ")", ";", "}", "return"), puzzle);
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
                "Return Method",
                "Build a method named getName that returns \"Ammar\".",
                List.of("methods", "return values"),
                MethodDeclarationValidator.returnMethod("String", "getName", "\"Ammar\"")
        );

        Room room = simpleRoom(tokens("String", "getName", "(", ")", "{", "return", "\"Ammar\"", ";", "}", "void", "\"Hi\""), puzzle);
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
                "While Loop",
                "Build a while loop that runs while count is less than 3 and increases count.",
                List.of("while loops", "increments"),
                new WhileLoopValidator("count", "3")
        );

        Room room = simpleRoom(tokens("while", "(", "count", "<", "3", ")", "{", "count", "++", ";", "}", ">", "="), puzzle);
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
                "For Loop",
                "Build a for loop that prints i while i counts from 0 to 2.",
                List.of("for loops", "printing"),
                new ForLoopValidator("i", "3", "i")
        );

        Room room = simpleRoom(tokens(
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
                "Loop Message",
                "Build a for loop that prints \"Loop\" twice.",
                List.of("for loops", "string output"),
                new ForLoopValidator("i", "2", "\"Loop\"")
        );

        Room room = simpleRoom(tokens(
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
                "Loop Counter",
                "Build a while loop that increases lives while lives is less than 5.",
                List.of("while loops", "counter updates"),
                new WhileLoopValidator("lives", "5")
        );

        List<ChestReward> rewards = shuffledRewards(List.of("while", "(", "lives", "<", "5", ")", "{", "lives", "++", ";", "}"));
        GridRoomBuilder.GridRoomLayout mazeLayout = RoomLayoutBuilder.gridTrainingMaze(rewards.size() + 1);
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                List.of(),
                mazeLayout.walls(),
                mazeLayout.chests(),
                rewards,
                ChestReward.goal(),
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
                "Class Blueprint",
                "Build a class named Item with a String name field and an int power field.",
                List.of("classes", "fields"),
                new ClassFieldsValidator()
        );

        GridRoomBuilder.GridRoomLayout mazeLayout = RoomLayoutBuilder.gridLockedRoomMaze(1, 1);
        List<Token> levelTokens = tokens("class", "Item", "{", "String", "name", ";", "int", ";", "}");
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                levelTokens,
                mazeLayout.walls(),
                mazeLayout.chests(),
                List.of(),
                ChestReward.goal(),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                new MultipleChoiceQuestion(
                        "Which type should the power field use?",
                        "_____ power;",
                        List.of("int", "String", "boolean", "class"),
                        "int",
                        ChestReward.code("power")
                ),
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
        GridRoomBuilder.GridRoomLayout mazeLayout = RoomLayoutBuilder.gridLockedRoomMaze(rewards.size() + 1, 6);
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                List.of(),
                mazeLayout.walls(),
                mazeLayout.chests(),
                rewards,
                ChestReward.goal(),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                new MultipleChoiceQuestion(
                        "Which method name should increase health?",
                        "void _____() { health = health + 1; }",
                        List.of("heal", "class", "Player", "new"),
                        "heal",
                        ChestReward.code("heal")
                ),
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

        GridRoomBuilder.GridRoomLayout mazeLayout = RoomLayoutBuilder.gridObjectLockMaze();
        Chest goalChest = RoomLayoutBuilder.gridChest(10, 3, 56, 42, true);
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
        Room room = new Room(
                Constants.ROOM_WIDTH,
                Constants.ROOM_HEIGHT,
                levelTokens,
                mazeLayout.walls(),
                List.of(goalChest),
                List.of(ironChest),
                List.of(),
                ChestReward.goal(),
                createExitDoor(),
                mazeLayout.challengeDoor(),
                new MultipleChoiceQuestion(
                        "Which method should the key object call?",
                        "key._____();",
                        List.of("use", "new", "locked", "class"),
                        "use",
                        ChestReward.code("use")
                ),
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
                goalHelper,
                room
        );
    }

    private Room simpleRoom(List<Token> levelTokens, Puzzle puzzle) {
        return new Room(Constants.ROOM_WIDTH, Constants.ROOM_HEIGHT, levelTokens, createExitDoor(), puzzle);
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

    private record LevelDefinition(Supplier<Level> factory) {
        private Level create() {
            return factory.get();
        }
    }
}

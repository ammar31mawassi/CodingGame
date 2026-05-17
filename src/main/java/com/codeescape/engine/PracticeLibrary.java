package com.codeescape.engine;

import com.codeescape.model.Level;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PracticeLibrary {
    private static final Map<String, List<PracticePrompt>> PROMPTS = Map.ofEntries(
            Map.entry("variable-declaration", List.of(
                    prompt(
                            "Variable Warmup",
                            "Rebuild a simple typed value before you retry the room.",
                            "Create one int variable named `keys` that stores any whole number of keys collected.",
                            "",
                            "int keys = 0;",
                            List.of("int keys = 0;"),
                            "Start with the type, then the variable name.",
                            "Use = to store any whole-number value.",
                            "End the statement with a semicolon.",
                            "variable-declaration"
                    ),
                    prompt(
                            "Variable Debug Fix",
                            "Repair the declaration instead of rewriting it from memory.",
                            "Fix the typed value so `keys` stores a whole number.",
                            "int keys : 4",
                            "int keys = 4;",
                            List.of("int keys = 4;"),
                            "Java uses = for assignment, not :.",
                            "The statement still needs a semicolon.",
                            "Keep the variable type at the front.",
                            "variable-declaration"
                    )
            )),
            Map.entry("print-statement", List.of(
                    prompt(
                            "Console Warmup",
                            "Practice printing one clear message.",
                            "Print the text \"Door unlocked\" to the console.",
                            "",
                            "System.out.println(\"Door unlocked\");",
                            List.of("System.out.println(\"Door unlocked\");"),
                            "Use System.out.println for one line of output.",
                            "Text stays inside double quotes.",
                            "Do not forget the final semicolon.",
                            "print-statement"
                    ),
                    prompt(
                            "Console Debug Fix",
                            "Correct a broken print statement.",
                            "Fix the snippet so it prints \"Gate open\" exactly once.",
                            "System.out.printIn(\"Gate open\")",
                            "System.out.println(\"Gate open\");",
                            List.of("System.out.println(\"Gate open\");"),
                            "The method name uses println with a lowercase l.",
                            "The text still belongs inside double quotes.",
                            "End the statement with a semicolon.",
                            "print-statement"
                    )
            )),
            Map.entry("if-block", List.of(
                    prompt(
                            "If Check",
                            "Rehearse a single conditional before going back.",
                            "Write an if block that runs only when score is greater than 5.",
                            "",
                            "if (score > 5) {\n    System.out.println(\"open\");\n}",
                            List.of(
                                    "if (score > 5) {\n    System.out.println(\"open\");\n}",
                                    "if (score > 5) {\nSystem.out.println(\"open\");\n}"
                            ),
                            "The condition belongs inside parentheses.",
                            "Use braces to wrap the body.",
                            "Think about what should happen only when the condition is true.",
                            "if-block"
                    ),
                    prompt(
                            "If Debug Fix",
                            "Clean up a conditional with the wrong comparison and braces.",
                            "Fix the snippet so it prints \"open\" only when `score` is greater than 7.",
                            "if score >= 7\n    System.out.println(\"open\");",
                            "if (score > 7) {\n    System.out.println(\"open\");\n}",
                            List.of(
                                    "if (score > 7) {\n    System.out.println(\"open\");\n}",
                                    "if (score > 7) {\nSystem.out.println(\"open\");\n}"
                            ),
                            "The condition must sit inside parentheses.",
                            "This drill wants greater than, not greater than or equal.",
                            "Use braces even for a one-line body.",
                            "if-block"
                    )
            )),
            Map.entry("if-else-branch", List.of(
                    prompt(
                            "Branch Practice",
                            "Compare two outcomes and choose the right branch.",
                            "If `energy` is greater than 0, print \"move\". Otherwise print \"rest\".",
                            "",
                            "if (energy > 0) {\n    System.out.println(\"move\");\n} else {\n    System.out.println(\"rest\");\n}",
                            List.of(
                                    "if (energy > 0) {\n    System.out.println(\"move\");\n} else {\n    System.out.println(\"rest\");\n}",
                                    "if (energy > 0) {\nSystem.out.println(\"move\");\n} else {\nSystem.out.println(\"rest\");\n}"
                            ),
                            "The if branch covers the true case.",
                            "The else branch handles everything left over.",
                            "Check that each branch prints the correct message.",
                            "if-else-branch"
                    ),
                    prompt(
                            "Branch Debug Fix",
                            "Repair a two-path branch instead of rebuilding it from scratch.",
                            "Fix the branch so it prints \"heal\" when `health` is below 3, otherwise prints \"hold\".",
                            "if (health < 3) {\n    System.out.println(\"hold\");\n}\nelse\n    System.out.println(\"heal\");",
                            "if (health < 3) {\n    System.out.println(\"heal\");\n} else {\n    System.out.println(\"hold\");\n}",
                            List.of(
                                    "if (health < 3) {\n    System.out.println(\"heal\");\n} else {\n    System.out.println(\"hold\");\n}",
                                    "if (health < 3) {\nSystem.out.println(\"heal\");\n} else {\nSystem.out.println(\"hold\");\n}"
                            ),
                            "The true branch should cover the low-health case.",
                            "Give the else branch its own braces.",
                            "Make sure the printed words land in the correct branch.",
                            "if-else-branch"
                    )
            )),
            Map.entry("string-char", List.of(
                    prompt(
                            "Quotes Drill",
                            "Separate text values from single characters.",
                            "Store the player name as a String and the grade as a char.",
                            "",
                            "String playerName = \"Ammar\";\nchar grade = 'A';",
                            List.of("String playerName = \"Ammar\";\nchar grade = 'A';"),
                            "Strings use double quotes.",
                            "Chars use single quotes.",
                            "Each declaration still needs a semicolon.",
                            "string-char"
                    ),
                    prompt(
                            "Quotes Debug Fix",
                            "Repair mismatched quote types.",
                            "Fix the declarations so `title` is a String and `rank` is a char.",
                            "String title = 'Coder';\nchar rank = \"A\";",
                            "String title = \"Coder\";\nchar rank = 'A';",
                            List.of("String title = \"Coder\";\nchar rank = 'A';"),
                            "A String can hold more than one character, so use double quotes.",
                            "A char holds one character in single quotes.",
                            "Keep the types aligned with the quote style.",
                            "string-char"
                    )
            )),
            Map.entry("void-method", List.of(
                    prompt(
                            "Method Action",
                            "Write a method that acts without returning data.",
                            "Create `void cheer()` that prints \"Go!\".",
                            "",
                            "void cheer() {\n    System.out.println(\"Go!\");\n}",
                            List.of(
                                    "void cheer() {\n    System.out.println(\"Go!\");\n}",
                                    "void cheer(){\n    System.out.println(\"Go!\");\n}"
                            ),
                            "Use void when the method performs an action.",
                            "Put the method body inside braces.",
                            "Make the print statement part of the method body.",
                            "void-method"
                    ),
                    prompt(
                            "Method Debug Fix",
                            "Correct a method that mixes action and return syntax.",
                            "Fix the method so `alarm()` is a `void` method that prints \"Run!\".",
                            "String alarm() {\n    return \"Run!\";\n}",
                            "void alarm() {\n    System.out.println(\"Run!\");\n}",
                            List.of(
                                    "void alarm() {\n    System.out.println(\"Run!\");\n}",
                                    "void alarm(){\n    System.out.println(\"Run!\");\n}"
                            ),
                            "The method should act, not return data.",
                            "Use System.out.println inside the method body.",
                            "Remove the return type and return statement mismatch.",
                            "void-method"
                    )
            )),
            Map.entry("return-method", List.of(
                    prompt(
                            "Return Drill",
                            "Practice sending one value back to the caller.",
                            "Create `int bonus()` that returns 2.",
                            "",
                            "int bonus() {\n    return 2;\n}",
                            List.of(
                                    "int bonus() {\n    return 2;\n}",
                                    "int bonus(){\n    return 2;\n}"
                            ),
                            "The method type must match the returned value.",
                            "Use the return keyword inside the body.",
                            "A return statement ends with a semicolon.",
                            "return-method"
                    ),
                    prompt(
                            "Return Debug Fix",
                            "Repair a method that forgets to return the right kind of value.",
                            "Fix the method so `livesLeft()` returns the number 1 as an int.",
                            "void livesLeft() {\n    System.out.println(1);\n}",
                            "int livesLeft() {\n    return 1;\n}",
                            List.of(
                                    "int livesLeft() {\n    return 1;\n}",
                                    "int livesLeft(){\n    return 1;\n}"
                            ),
                            "The method type should announce an int result.",
                            "Use return instead of printing the answer.",
                            "Keep the returned value inside the method body.",
                            "return-method"
                    )
            )),
            Map.entry("while-loop", List.of(
                    prompt(
                            "While Loop Lap",
                            "Repeat a small action while a rule stays true.",
                            "Start with `count = 0` and keep increasing it while it is less than 3.",
                            "",
                            "int count = 0;\nwhile (count < 3) {\n    count++;\n}",
                            List.of(
                                    "int count = 0;\nwhile (count < 3) {\n    count++;\n}",
                                    "int count = 0;\nwhile (count < 3){\n    count++;\n}"
                            ),
                            "Set up the variable before the loop.",
                            "Keep the loop condition inside parentheses.",
                            "Change the variable so the loop can stop.",
                            "while-loop"
                    ),
                    prompt(
                            "While Debug Fix",
                            "Correct a loop that never changes its state properly.",
                            "Fix the loop so `energy` starts at 1 and increases until it reaches 4.",
                            "int energy = 1;\nwhile energy < 4 {\n    energy--;\n}",
                            "int energy = 1;\nwhile (energy < 4) {\n    energy++;\n}",
                            List.of(
                                    "int energy = 1;\nwhile (energy < 4) {\n    energy++;\n}",
                                    "int energy = 1;\nwhile (energy < 4){\n    energy++;\n}"
                            ),
                            "The while condition still needs parentheses.",
                            "The update should move toward the stop condition.",
                            "Keep the opening and closing braces around the loop body.",
                            "while-loop"
                    )
            )),
            Map.entry("for-loop", List.of(
                    prompt(
                            "For Loop Lap",
                            "Rebuild setup, condition, and update together.",
                            "Print `i` for values 0 through 2 with a for loop.",
                            "",
                            "for (int i = 0; i < 3; i++) {\n    System.out.println(i);\n}",
                            List.of(
                                    "for (int i = 0; i < 3; i++) {\n    System.out.println(i);\n}",
                                    "for (int i = 0; i < 3; i++){\n    System.out.println(i);\n}"
                            ),
                            "The header has setup, condition, and update.",
                            "Use braces for the repeated body.",
                            "Check that the loop runs the intended number of times.",
                            "for-loop"
                    ),
                    prompt(
                            "For Debug Fix",
                            "Repair the loop header and body update direction.",
                            "Fix the snippet so it prints `step` for 1 through 3.",
                            "for (int step = 1; step <= 3; step--) {\n    System.out.println(step)\n}",
                            "for (int step = 1; step <= 3; step++) {\n    System.out.println(step);\n}",
                            List.of(
                                    "for (int step = 1; step <= 3; step++) {\n    System.out.println(step);\n}",
                                    "for (int step = 1; step <= 3; step++){\n    System.out.println(step);\n}"
                            ),
                            "The update should move forward through the range.",
                            "Print statements still need semicolons.",
                            "Keep all three loop header pieces inside the parentheses.",
                            "for-loop"
                    )
            )),
            Map.entry("class-fields", List.of(
                    prompt(
                            "Class Blueprint",
                            "Practice describing a class with fields only.",
                            "Build a `Chest` class with `String name` and `int coins`.",
                            "",
                            "class Chest {\n    String name;\n    int coins;\n}",
                            List.of("class Chest {\n    String name;\n    int coins;\n}"),
                            "A class starts with the class keyword and a name.",
                            "Fields live inside the braces.",
                            "Use one declaration line per field.",
                            "class-fields"
                    ),
                    prompt(
                            "Class Debug Fix",
                            "Repair a class that mixes field syntax.",
                            "Fix the class so `Potion` has `String label` and `int charges` fields.",
                            "class Potion {\n    String = label;\n    int charges\n}",
                            "class Potion {\n    String label;\n    int charges;\n}",
                            List.of("class Potion {\n    String label;\n    int charges;\n}"),
                            "Field declarations do not use = unless you are assigning a value.",
                            "Each field line still ends with a semicolon.",
                            "Keep both fields inside the class braces.",
                            "class-fields"
                    )
            )),
            Map.entry("constructor-method", List.of(
                    prompt(
                            "Object Setup",
                            "Connect construction and behavior in one small class.",
                            "Create a `Player` class with a constructor that sets `name`, plus `void heal()` that adds 1 health.",
                            "",
                            "class Player {\n    String name;\n    int health;\n\n    Player(String name, int health) {\n        this.name = name;\n        this.health = health;\n    }\n\n    void heal() {\n        health = health + 1;\n    }\n}",
                            List.of("class Player {\n    String name;\n    int health;\n\n    Player(String name, int health) {\n        this.name = name;\n        this.health = health;\n    }\n\n    void heal() {\n        health = health + 1;\n    }\n}"),
                            "Constructors use the class name and no return type.",
                            "Use this.field when assigning constructor inputs.",
                            "Methods can change the object's stored data.",
                            "constructor-method"
                    ),
                    prompt(
                            "Constructor Debug Fix",
                            "Repair a class where the constructor and method both miss key syntax.",
                            "Fix the class so `Door` stores `code`, sets it in the constructor, and `void unlock()` increases `uses` by 1.",
                            "class Door {\n    String code;\n    int uses;\n\n    void Door(String code) {\n        code = this.code;\n    }\n\n    void unlock() {\n        uses = uses - 1;\n    }\n}",
                            "class Door {\n    String code;\n    int uses;\n\n    Door(String code) {\n        this.code = code;\n    }\n\n    void unlock() {\n        uses = uses + 1;\n    }\n}",
                            List.of("class Door {\n    String code;\n    int uses;\n\n    Door(String code) {\n        this.code = code;\n    }\n\n    void unlock() {\n        uses = uses + 1;\n    }\n}"),
                            "Constructors do not declare a return type.",
                            "Assign the incoming value into the field with this.code = code.",
                            "The unlock method should increase uses, not decrease it.",
                            "constructor-method"
                    )
            )),
            Map.entry("object-call", List.of(
                    prompt(
                            "Object Call",
                            "Practice creating an object and using dot access.",
                            "Create an `Item` object named `key`, then call `use()` on it.",
                            "",
                            "Item key = new Item(\"key\");\nkey.use();",
                            List.of("Item key = new Item(\"key\");\nkey.use();"),
                            "Construct the object with new.",
                            "Store it in a variable before using it.",
                            "Dot access calls a method on that specific object.",
                            "object-call"
                    ),
                    prompt(
                            "Object Call Debug Fix",
                            "Repair object creation and method-call syntax.",
                            "Fix the snippet so it creates a `Chest` named `loot` and then calls `open()`.",
                            "Chest loot = Chest.new(\"loot\")\nloot-open();",
                            "Chest loot = new Chest(\"loot\");\nloot.open();",
                            List.of("Chest loot = new Chest(\"loot\");\nloot.open();"),
                            "The `new` keyword comes before the class name call.",
                            "Use dot access for the method call.",
                            "Both lines should end with semicolons.",
                            "object-call"
                    )
            ))
    );

    private PracticeLibrary() {
    }

    public static Optional<PracticePrompt> forLevel(Level level) {
        return forLevel(level, 0);
    }

    public static Optional<PracticePrompt> forLevel(Level level, int practiceCount) {
        if (level == null) {
            return Optional.empty();
        }

        int variantSeed = Math.max(0, practiceCount) + Math.max(0, level.getLevelNumber() - 1);
        return NotebookLibrary.entryForLevel(level)
                .or(() -> NotebookLibrary.recommendedForLevel(level))
                .flatMap(entry -> forNotebookEntry(entry.id(), variantSeed));
    }

    public static Optional<PracticePrompt> forNotebookEntry(String entryId) {
        return forNotebookEntry(entryId, 0);
    }

    public static Optional<PracticePrompt> forNotebookEntry(String entryId, int practiceCount) {
        if (entryId == null || entryId.isBlank()) {
            return Optional.empty();
        }
        List<PracticePrompt> variants = PROMPTS.get(entryId);
        if (variants == null || variants.isEmpty()) {
            return Optional.empty();
        }

        int index = Math.floorMod(practiceCount, variants.size());
        return Optional.of(variants.get(index));
    }

    private static PracticePrompt prompt(
            String title,
            String summary,
            String task,
            String challengeCode,
            String sampleSolution,
            List<String> acceptedAnswers,
            String tipOne,
            String tipTwo,
            String tipThree,
            String notebookEntryId
    ) {
        return new PracticePrompt(
                title,
                summary,
                task,
                challengeCode,
                sampleSolution,
                acceptedAnswers,
                List.of(tipOne, tipTwo, tipThree),
                notebookEntryId
        );
    }
}

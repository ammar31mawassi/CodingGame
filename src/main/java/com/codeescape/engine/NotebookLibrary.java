package com.codeescape.engine;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class NotebookLibrary {
    private static final List<NotebookEntry> ENTRIES = List.of(
            new NotebookEntry("variable-declaration", "Variable Declaration", "Store a typed value in one step.", "int score = 10;", 1),
            new NotebookEntry("print-statement", "Print Statement", "Send text or values to the console.", "System.out.println(\"Hello\");", 2),
            new NotebookEntry("if-block", "If Block", "Run code only when the condition is true.", "if (x > 3) { }", 4),
            new NotebookEntry("if-else-branch", "If-Else Branch", "Choose between two different outcomes.", "if (grade > pass) { System.out.println(\"passed\"); } else { System.out.println(\"failed\"); }", 6),
            new NotebookEntry("string-char", "String And Char", "Remember the quote styles for text vs one character.", "String name = \"Ammar\";\nchar grade = 'A';", 9),
            new NotebookEntry("void-method", "Void Method", "Use void when the method acts instead of returns.", "void greet() { System.out.println(\"Hi\"); }", 11),
            new NotebookEntry("return-method", "Return Method", "Return gives a value back to the caller.", "String getName() { return \"Ammar\"; }", 12),
            new NotebookEntry("while-loop", "While Loop", "Repeat while the condition stays true.", "while (count < 3) { count++; }", 13),
            new NotebookEntry("for-loop", "For Loop", "Keep setup, condition, and update together.", "for (int i = 0; i < 3; i++) { System.out.println(i); }", 14),
            new NotebookEntry("class-fields", "Class Fields", "A class groups named fields into one blueprint.", "class Item { String name; int power; }", 17),
            new NotebookEntry("constructor-method", "Constructor And Method", "Constructors prepare objects before methods use them.", "class Player { String name; int health; Player(String name, int health) { this.name = name; this.health = health; } void heal() { health = health + 1; } }", 18),
            new NotebookEntry("object-call", "Object Creation And Call", "Create an object, then use dot access to call methods.", "Item key = new Item(\"key\");\nkey.use();", 19)
    );

    private NotebookLibrary() {
    }

    public static List<NotebookEntry> unlockedThroughLevel(int levelNumber) {
        return ENTRIES.stream()
                .filter(entry -> entry.unlockLevelNumber() <= levelNumber)
                .sorted(Comparator.comparingInt(NotebookEntry::unlockLevelNumber))
                .toList();
    }

    public static List<NotebookEntry> allEntries() {
        return ENTRIES;
    }

    public static Optional<NotebookEntry> recommendedForLevel(com.codeescape.model.Level level) {
        if (level == null) {
            return Optional.empty();
        }

        Optional<NotebookEntry> mapped = entryForLevel(level);
        if (mapped.isPresent()) {
            return mapped;
        }

        return ENTRIES.stream()
                .filter(entry -> entry.unlockLevelNumber() <= level.getLevelNumber())
                .max(Comparator.comparingInt(NotebookEntry::unlockLevelNumber));
    }

    public static Optional<NotebookEntry> entryForLevel(com.codeescape.model.Level level) {
        if (level == null) {
            return Optional.empty();
        }

        return switch (level.getDisplayId()) {
            case "1-1", "1-3", "1-4", "2-2" -> find("variable-declaration");
            case "1-2" -> find("print-statement");
            case "REV-1", "BOSS-1" -> find("variable-declaration");
            case "2-1" -> find("if-block");
            case "2-3", "2-4", "2-5", "2-6" -> find("if-else-branch");
            case "REV-2", "BOSS-2" -> find("if-else-branch");
            case "3-1", "3-2" -> find("string-char");
            case "3-3", "3-5" -> find("void-method");
            case "3-4" -> find("return-method");
            case "REV-3", "BOSS-3" -> find("return-method");
            case "4-1", "4-4" -> find("while-loop");
            case "4-2", "4-3", "4-5" -> find("for-loop");
            case "REV-4", "BOSS-4" -> find("for-loop");
            case "5-1" -> find("class-fields");
            case "5-2" -> find("constructor-method");
            case "5-3", "5-4", "BOSS-5" -> find("object-call");
            default -> Optional.empty();
        };
    }

    public static Optional<NotebookEntry> find(String id) {
        return ENTRIES.stream()
                .filter(entry -> entry.id().equals(id))
                .findFirst();
    }
}

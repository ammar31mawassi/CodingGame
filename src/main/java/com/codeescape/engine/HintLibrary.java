package com.codeescape.engine;

import com.codeescape.model.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class HintLibrary {
    private HintLibrary() {
    }

    public static List<String> hintsFor(Level level) {
        List<String> hints = new ArrayList<>();
        String concepts = level.getRoom().getPuzzle().getRequiredConcepts().stream()
                .collect(Collectors.joining(", "));
        if (!concepts.isBlank()) {
            hints.add("Focus on these ideas first: " + concepts + ".");
        }
        if (!level.getGoalHelper().isBlank()) {
            hints.add(level.getGoalHelper());
        } else {
            hints.add("Break the answer into small Java pieces and check the order of tokens carefully.");
        }

        notebookHint(level).ifPresentOrElse(
                hints::add,
                () -> hints.add("Look for the shape of a valid Java statement: opening tokens, the middle expression, then the closing symbol."));
        return hints;
    }

    private static Optional<String> notebookHint(Level level) {
        return switch (level.getDisplayId()) {
            case "1-1" -> notebookPattern("variable-declaration");
            case "1-2" -> notebookPattern("print-statement");
            case "1-3", "1-4", "2-2" -> notebookPattern("variable-declaration");
            case "2-1" -> notebookPattern("if-block");
            case "2-3", "2-4", "2-5", "2-6" -> notebookPattern("if-else-branch");
            case "3-1", "3-2" -> notebookPattern("string-char");
            case "3-3", "3-5" -> notebookPattern("void-method");
            case "3-4" -> notebookPattern("return-method");
            case "4-1", "4-4" -> notebookPattern("while-loop");
            case "4-2", "4-3", "4-5" -> notebookPattern("for-loop");
            case "5-1" -> notebookPattern("class-fields");
            case "5-2" -> notebookPattern("constructor-method");
            case "5-3", "5-4" -> notebookPattern("object-call");
            default -> Optional.empty();
        };
    }

    private static Optional<String> notebookPattern(String entryId) {
        return NotebookLibrary.find(entryId)
                .map(entry -> "Pattern reminder: " + entry.pattern());
    }
}

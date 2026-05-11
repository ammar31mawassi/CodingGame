package com.codeescape.engine;

import java.util.ArrayList;
import java.util.List;
import com.codeescape.model.TokenType;

public record LevelLayoutOverride(
        int levelNumber,
        int columns,
        int rows,
        int[][] cells,
        List<ChestPlacement> chests,
        List<QuestionDoorPlacement> questionDoors,
        String goalTitle,
        String goalInstructions,
        String helperText,
        List<String> acceptedAnswers,
        boolean tokensOverridden,
        List<TokenPlacement> tokens
) {
    public LevelLayoutOverride(
            int levelNumber,
            int columns,
            int rows,
            int[][] cells,
            List<ChestPlacement> chests,
            List<QuestionDoorPlacement> questionDoors
    ) {
        this(levelNumber, columns, rows, cells, chests, questionDoors, null, null, null, List.of(), false, List.of());
    }

    public LevelLayoutOverride {
        cells = copyCells(cells, rows, columns);
        chests = List.copyOf(chests);
        questionDoors = List.copyOf(questionDoors);
        acceptedAnswers = List.copyOf(acceptedAnswers == null ? new ArrayList<>() : acceptedAnswers);
        tokens = List.copyOf(tokens == null ? new ArrayList<>() : tokens);
    }

    private static int[][] copyCells(int[][] source, int rows, int columns) {
        if (source.length != rows) {
            throw new IllegalArgumentException("Cell matrix row count does not match layout rows.");
        }

        int[][] copy = new int[rows][columns];
        for (int row = 0; row < rows; row++) {
            if (source[row].length != columns) {
                throw new IllegalArgumentException("Cell matrix column count does not match layout columns.");
            }
            System.arraycopy(source[row], 0, copy[row], 0, columns);
        }
        return copy;
    }

    public int[][] copyCells() {
        return copyCells(cells, rows, columns);
    }

    public record ChestPlacement(int column, int row, boolean lockedRoomOnly) {
    }

    public enum TokenPlacementKind {
        VISIBLE,
        CHEST
    }

    public record TokenPlacement(
            String value,
            TokenType type,
            TokenPlacementKind kind,
            int column,
            int row,
            int chestOrder
    ) {
        public TokenPlacement {
            value = value == null ? "" : value;
            type = type == null ? TokenType.CODE : type;
            kind = kind == null ? TokenPlacementKind.VISIBLE : kind;
        }
    }

    public record QuestionDoorPlacement(
            int column,
            int row,
            GridRoomBuilder.Side side,
            String prompt,
            String code,
            List<String> choices,
            String correctAnswer,
            String reward
    ) {
        public QuestionDoorPlacement {
            prompt = prompt == null ? "" : prompt;
            code = code == null ? "" : code;
            choices = List.copyOf(choices == null ? new ArrayList<>() : choices);
            correctAnswer = correctAnswer == null ? "" : correctAnswer;
            reward = reward == null ? "" : reward;
        }
    }
}

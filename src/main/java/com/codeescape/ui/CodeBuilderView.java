package com.codeescape.ui;

import com.codeescape.model.Inventory;
import com.codeescape.model.Puzzle;
import com.codeescape.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class CodeBuilderView {
    private final Inventory inventory;
    private final Puzzle puzzle;
    private final Runnable onSolved;
    private final List<String> selectedTokens = new ArrayList<>();
    private final List<Integer> selectedTokenIndexes = new ArrayList<>();
    private final List<Button> availableTokenButtons = new ArrayList<>();
    private final FlowPane selectedTokenBox = new FlowPane(8, 8);
    private final FlowPane availableTokenBox = new FlowPane(8, 8);
    private final Label answerPreviewLabel = new Label();
    private final Label feedbackLabel = new Label();
    private final Button removeLastButton = new Button("Remove Last");
    private final Button clearButton = new Button("Clear");
    private final Button submitButton = new Button("Submit");
    private boolean solved;

    public CodeBuilderView(Inventory inventory, Puzzle puzzle) {
        this(inventory, puzzle, () -> {
        });
    }

    public CodeBuilderView(Inventory inventory, Puzzle puzzle, Runnable onSolved) {
        this.inventory = inventory;
        this.puzzle = puzzle;
        this.onSolved = onSolved;
    }

    public Parent createView() {
        Label title = new Label(puzzle.getTitle());
        title.getStyleClass().add("puzzle-title");
        Label instructions = new Label(puzzle.getInstructions());
        instructions.setWrapText(true);
        instructions.getStyleClass().add("puzzle-copy");

        removeLastButton.setOnAction(event -> removeLastToken());
        clearButton.setOnAction(event -> clearAnswer());
        submitButton.setOnAction(event -> submitAnswer());

        answerPreviewLabel.setWrapText(true);
        answerPreviewLabel.getStyleClass().add("answer-preview");
        feedbackLabel.getStyleClass().add("feedback-label");
        removeLastButton.getStyleClass().add("pixel-button");
        clearButton.getStyleClass().add("pixel-button");
        submitButton.getStyleClass().add("pixel-button");

        refreshAvailableTokens();
        refreshSelectedTokens();
        updateControls();

        FlowPane actionControls = new FlowPane(8, 8);
        actionControls.getChildren().addAll(removeLastButton, clearButton, submitButton);

        VBox root = new VBox(
                12,
                title,
                instructions,
                new Separator(),
                new Label("Answer"),
                answerPreviewLabel,
                selectedTokenBox,
                new Label("Available Tokens"),
                availableTokenBox,
                actionControls,
                feedbackLabel
        );
        root.setPadding(new Insets(20));
        root.setPrefWidth(240);
        root.setMaxWidth(240);
        return root;
    }

    private void addTokenToAnswer(int tokenIndex, String token) {
        if (solved || selectedTokenIndexes.contains(tokenIndex)) {
            return;
        }

        selectedTokenIndexes.add(tokenIndex);
        selectedTokens.add(token);
        feedbackLabel.setText("");
        refreshSelectedTokens();
        refreshAvailableTokens();
        updateControls();
    }

    private void removeLastToken() {
        if (selectedTokens.isEmpty() || solved) {
            return;
        }

        selectedTokens.remove(selectedTokens.size() - 1);
        selectedTokenIndexes.remove(selectedTokenIndexes.size() - 1);
        feedbackLabel.setText("");
        refreshSelectedTokens();
        refreshAvailableTokens();
        updateControls();
    }

    private void clearAnswer() {
        if (solved) {
            return;
        }

        selectedTokens.clear();
        selectedTokenIndexes.clear();
        feedbackLabel.setText("");
        refreshSelectedTokens();
        refreshAvailableTokens();
        updateControls();
    }

    private String buildCodeString() {
        if (selectedTokens.isEmpty()) {
            return "";
        }

        StringBuilder code = new StringBuilder();
        String previousToken = "";
        Set<String> noSpaceBefore = Set.of(";", ")", ",");

        for (String token : selectedTokens) {
            if (code.isEmpty()) {
                code.append(token);
            } else if (noSpaceBefore.contains(token)) {
                code.append(token);
            } else if ("}".equals(token)) {
                if (!"{".equals(previousToken)) {
                    code.append(" ");
                }
                code.append(token);
            } else if ("(".equals(previousToken)) {
                code.append(token);
            } else {
                code.append(" ").append(token);
            }

            previousToken = token;
        }

        return code.toString();
    }

    private void submitAnswer() {
        ValidationResult result = puzzle.checkAnswer(buildCodeString());
        showFeedback(result);
        if (result.isValid()) {
            solved = true;
            onSolved.run();
            updateControls();
            refreshAvailableTokens();
        }
    }

    private void showFeedback(ValidationResult result) {
        feedbackLabel.setText(result.getMessage());
    }

    private void refreshSelectedTokens() {
        selectedTokenBox.getChildren().setAll(
                selectedTokens.stream()
                        .map(Label::new)
                        .toList()
        );

        String code = buildCodeString();
        answerPreviewLabel.setText(code.isBlank() ? "(empty)" : code);
    }

    private void refreshAvailableTokens() {
        List<String> tokenValues = inventory.getTokenValues();
        if (availableTokenButtons.size() != tokenValues.size()) {
            rebuildAvailableTokenButtons(tokenValues);
        }

        for (int i = 0; i < availableTokenButtons.size(); i++) {
            availableTokenButtons.get(i).setDisable(solved || selectedTokenIndexes.contains(i));
        }
    }

    private void updateControls() {
        boolean hasSelection = !selectedTokens.isEmpty();
        removeLastButton.setDisable(!hasSelection || solved);
        clearButton.setDisable(!hasSelection || solved);
        submitButton.setDisable(!hasSelection || solved);
    }

    private void rebuildAvailableTokenButtons(List<String> tokenValues) {
        availableTokenButtons.clear();
        availableTokenBox.getChildren().clear();

        for (int i = 0; i < tokenValues.size(); i++) {
            int tokenIndex = i;
            String value = tokenValues.get(i);
            Button button = new Button(value);
            button.getStyleClass().add("token-button");
            button.setOnAction(event -> addTokenToAnswer(tokenIndex, value));
            availableTokenButtons.add(button);
            availableTokenBox.getChildren().add(button);
        }
    }
}

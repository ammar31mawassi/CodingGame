package com.codeescape.ui;

import com.codeescape.model.Inventory;
import com.codeescape.model.Puzzle;
import com.codeescape.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CodeBuilderView {
    private final Inventory inventory;
    private final Puzzle puzzle;
    private final List<String> selectedTokens = new ArrayList<>();
    private final HBox selectedTokenBox = new HBox(8);
    private final VBox availableTokenBox = new VBox(8);
    private final Label feedbackLabel = new Label();

    public CodeBuilderView(Inventory inventory, Puzzle puzzle) {
        this.inventory = inventory;
        this.puzzle = puzzle;
    }

    public Parent createView() {
        Label title = new Label(puzzle.getTitle());
        Label instructions = new Label(puzzle.getInstructions());

        Button removeLastButton = new Button("Remove Last");
        removeLastButton.setOnAction(event -> removeLastToken());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> clearAnswer());

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> submitAnswer());

        refreshAvailableTokens();

        return new VBox(
                12,
                title,
                instructions,
                new Label("Answer"),
                selectedTokenBox,
                new Label("Available Tokens"),
                availableTokenBox,
                new HBox(8, removeLastButton, clearButton, submitButton),
                feedbackLabel
        );
    }

    private void addTokenToAnswer(String token) {
        selectedTokens.add(token);
        selectedTokenBox.getChildren().add(new Label(token));
    }

    private void removeLastToken() {
        if (!selectedTokens.isEmpty()) {
            selectedTokens.remove(selectedTokens.size() - 1);
            selectedTokenBox.getChildren().remove(selectedTokenBox.getChildren().size() - 1);
        }
    }

    private void clearAnswer() {
        selectedTokens.clear();
        selectedTokenBox.getChildren().clear();
    }

    private String buildCodeString() {
        return String.join(" ", selectedTokens);
    }

    private void submitAnswer() {
        ValidationResult result = puzzle.checkAnswer(buildCodeString());
        showFeedback(result);
    }

    private void showFeedback(ValidationResult result) {
        feedbackLabel.setText(result.getMessage());
    }

    private void refreshAvailableTokens() {
        availableTokenBox.getChildren().setAll(
                inventory.getTokenValues().stream()
                        .map(value -> {
                            Button button = new Button(value);
                            button.setOnAction(event -> addTokenToAnswer(value));
                            return button;
                        })
                        .toList()
        );
    }
}

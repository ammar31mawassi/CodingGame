package com.codeescape.ui;

import com.codeescape.model.Inventory;
import com.codeescape.model.Puzzle;
import com.codeescape.validation.TypedTokenUsageValidator;
import com.codeescape.validation.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CodeBuilderView {
    private final Inventory inventory;
    private final Puzzle puzzle;
    private final Consumer<ValidationResult> onSolved;
    private final Consumer<ValidationResult> onFailed;
    private final List<String> selectedTokens = new ArrayList<>();
    private final List<Integer> selectedTokenIndexes = new ArrayList<>();
    private final List<Button> availableTokenButtons = new ArrayList<>();
    private final FlowPane selectedTokenBox = new FlowPane(8, 8);
    private final FlowPane availableTokenBox = new FlowPane(8, 8);
    private final Label answerPreviewLabel = new Label();
    private final Label feedbackLabel = new Label();
    private Label titleLabel;
    private Label instructionsLabel;
    private TextArea typedCodeInput;
    private final Button removeLastButton = new Button("Remove Last");
    private final Button clearButton = new Button("Clear");
    private final Button submitButton = new Button("Submit Tokens");
    private final Button submitTypedButton = new Button("Submit Typed Code");
    private boolean showGoal;
    private boolean solved;

    public CodeBuilderView(Inventory inventory, Puzzle puzzle) {
        this(inventory, puzzle, true, result -> {
        }, result -> {
        });
    }

    public CodeBuilderView(Inventory inventory, Puzzle puzzle, Runnable onSolved) {
        this(inventory, puzzle, true, result -> onSolved.run(), result -> {
        });
    }

    public CodeBuilderView(Inventory inventory, Puzzle puzzle, boolean showGoal, Consumer<ValidationResult> onSolved) {
        this(inventory, puzzle, showGoal, onSolved, result -> {
        });
    }

    public CodeBuilderView(
            Inventory inventory,
            Puzzle puzzle,
            boolean showGoal,
            Consumer<ValidationResult> onSolved,
            Consumer<ValidationResult> onFailed
    ) {
        this.inventory = inventory;
        this.puzzle = puzzle;
        this.showGoal = showGoal;
        this.onSolved = onSolved;
        this.onFailed = onFailed;
    }

    public Parent createView() {
        titleLabel = new Label();
        titleLabel.getStyleClass().add("puzzle-title");
        instructionsLabel = new Label();
        instructionsLabel.setWrapText(true);
        instructionsLabel.getStyleClass().add("puzzle-copy");
        refreshGoalText();

        removeLastButton.setOnAction(event -> removeLastToken());
        clearButton.setOnAction(event -> clearAnswer());
        submitButton.setOnAction(event -> submitAnswer());
        submitTypedButton.setOnAction(event -> submitTypedAnswer());

        answerPreviewLabel.setWrapText(true);
        answerPreviewLabel.setMaxWidth(Double.MAX_VALUE);
        answerPreviewLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        answerPreviewLabel.setMaxHeight(Double.MAX_VALUE);
        answerPreviewLabel.setMinHeight(Region.USE_PREF_SIZE);
        answerPreviewLabel.setTextOverrun(OverrunStyle.CLIP);
        answerPreviewLabel.getStyleClass().add("answer-preview");
        feedbackLabel.getStyleClass().add("feedback-label");
        removeLastButton.getStyleClass().add("pixel-button");
        clearButton.getStyleClass().add("pixel-button");
        submitButton.getStyleClass().add("pixel-button");
        submitTypedButton.getStyleClass().add("pixel-button");

        typedCodeInput = new TextArea();
        typedCodeInput.setPromptText("Type Java code here...");
        typedCodeInput.setWrapText(true);
        typedCodeInput.setPrefRowCount(4);
        typedCodeInput.getStyleClass().add("typed-code-input");
        typedCodeInput.textProperty().addListener((observable, oldValue, newValue) -> updateControls());

        refreshAvailableTokens();
        refreshSelectedTokens();
        updateControls();

        FlowPane actionControls = new FlowPane(8, 8);
        actionControls.getChildren().addAll(removeLastButton, clearButton, submitButton);

        Label answerLabel = new Label("Answer");
        answerLabel.getStyleClass().add("puzzle-copy");

        Label availableTokensLabel = new Label("Available Tokens");
        availableTokensLabel.getStyleClass().add("puzzle-copy");

        Label typedCodeLabel = new Label("Typed Code");
        typedCodeLabel.getStyleClass().add("puzzle-copy");

        VBox root = new VBox(
                12,
                titleLabel,
                instructionsLabel,
                new Separator(),
                answerLabel,
                answerPreviewLabel,
                selectedTokenBox,
                typedCodeLabel,
                typedCodeInput,
                availableTokensLabel,
                availableTokenBox,
                actionControls,
                submitTypedButton,
                feedbackLabel
        );
        root.setPadding(new Insets(20));
        root.setPrefWidth(720);
        root.setMaxWidth(720);
        return root;
    }

    public void refresh(boolean showGoal) {
        this.showGoal = showGoal;
        refreshGoalText();
        refreshSelectedTokens();
        refreshAvailableTokens();
        updateControls();
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

    private void removeTokenAt(int selectedIndex) {
        if (solved || selectedIndex < 0 || selectedIndex >= selectedTokens.size()) {
            return;
        }

        selectedTokens.remove(selectedIndex);
        selectedTokenIndexes.remove(selectedIndex);
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
        Set<String> noSpaceBefore = Set.of(")", ",");

        for (int i = 0; i < selectedTokens.size(); i++) {
            String token = selectedTokens.get(i);
            boolean startsLine = code.isEmpty() || code.charAt(code.length() - 1) == '\n';

            if (startsLine) {
                code.append(token);
            } else if (";".equals(token)) {
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
            if (";".equals(token) && i < selectedTokens.size() - 1) {
                code.append("\n");
            }
        }

        return code.toString();
    }

    private void submitAnswer() {
        submitCode(buildCodeString());
    }

    private void submitTypedAnswer() {
        if (typedCodeInput == null) {
            return;
        }

        String typedCode = typedCodeInput.getText();
        ValidationResult tokenUsageResult = TypedTokenUsageValidator.validate(typedCode, inventory.getTokenValues());
        if (!tokenUsageResult.isValid()) {
            showFeedback(tokenUsageResult);
            return;
        }

        submitCode(typedCode);
    }

    private void submitCode(String code) {
        ValidationResult result = puzzle.checkAnswer(code);
        showFeedback(result);
        if (result.isValid()) {
            solved = true;
            onSolved.accept(result);
            updateControls();
            refreshAvailableTokens();
        } else {
            onFailed.accept(result);
        }
    }

    private void showFeedback(ValidationResult result) {
        feedbackLabel.setText(result.getMessage());
    }

    private void refreshGoalText() {
        if (titleLabel == null || instructionsLabel == null) {
            return;
        }

        titleLabel.setText(showGoal ? puzzle.getTitle() : "Terminal");
        instructionsLabel.setText(showGoal ? puzzle.getInstructions() : "Goal not found yet.");
    }

    private void refreshSelectedTokens() {
        selectedTokenBox.getChildren().clear();
        for (int i = 0; i < selectedTokens.size(); i++) {
            int selectedIndex = i;
            Label label = new Label(selectedTokens.get(i));
            label.getStyleClass().add("selected-token");
            label.setOnMouseClicked(event -> removeTokenAt(selectedIndex));
            selectedTokenBox.getChildren().add(label);
        }

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
        submitButton.setDisable(!hasSelection || solved || !showGoal);
        boolean hasTypedCode = typedCodeInput != null && !typedCodeInput.getText().isBlank();
        submitTypedButton.setDisable(!hasTypedCode || solved || !showGoal);
        if (typedCodeInput != null) {
            typedCodeInput.setEditable(!solved);
        }
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

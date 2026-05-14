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
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CodeBuilderView {
    private static final String AVAILABLE_DRAG_PREFIX = "available:";
    private static final String SELECTED_DRAG_PREFIX = "selected:";

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
    private final Button submitButton = new Button("Submit");
    private boolean showGoal;
    private boolean solved;
    private boolean syncingTypedCode;

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

        removeLastButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            removeLastToken();
        });
        clearButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            clearAnswer();
        });
        submitButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            submitAnswer();
        });
        configureSelectedTokenBoxDropTarget();

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

        typedCodeInput = new TextArea();
        typedCodeInput.setPromptText("Type Java code here, or build it with tokens...");
        typedCodeInput.setWrapText(true);
        typedCodeInput.setPrefRowCount(4);
        typedCodeInput.getStyleClass().add("typed-code-input");
        typedCodeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            syncTokensFromTypedCode(newValue);
            updateControls();
        });

        refreshAvailableTokens();
        refreshSelectedTokens();
        updateControls();

        FlowPane actionControls = new FlowPane(8, 8);
        actionControls.getChildren().addAll(removeLastButton, clearButton, submitButton);

        Label answerLabel = new Label("Answer");
        answerLabel.getStyleClass().add("puzzle-copy");

        Label tokenizedAnswerLabel = new Label("Used Tokens");
        tokenizedAnswerLabel.getStyleClass().add("puzzle-copy");

        Label availableTokensLabel = new Label("Available Tokens");
        availableTokensLabel.getStyleClass().add("puzzle-copy");

        VBox root = new VBox(
                12,
                titleLabel,
                instructionsLabel,
                new Separator(),
                answerLabel,
                typedCodeInput,
                tokenizedAnswerLabel,
                answerPreviewLabel,
                selectedTokenBox,
                availableTokensLabel,
                availableTokenBox,
                actionControls,
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
        syncTokensFromTypedCode(typedCodeInput == null ? "" : typedCodeInput.getText());
        refreshAvailableTokens();
        updateControls();
    }

    private void addTokenToAnswer(int tokenIndex, String token) {
        addTokenToAnswerAt(tokenIndex, token, selectedTokens.size());
    }

    private void addTokenToAnswerAt(int tokenIndex, String token, int selectedIndex) {
        if (solved || selectedTokenIndexes.contains(tokenIndex)) {
            return;
        }

        int insertionIndex = Math.max(0, Math.min(selectedIndex, selectedTokens.size()));
        selectedTokenIndexes.add(insertionIndex, tokenIndex);
        selectedTokens.add(insertionIndex, token);
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
        Set<String> noSpaceBefore = Set.of(")", ",", ".");

        for (int i = 0; i < selectedTokens.size(); i++) {
            String token = selectedTokens.get(i);
            boolean startsLine = code.isEmpty() || code.charAt(code.length() - 1) == '\n';

            if (startsLine) {
                code.append(token);
            } else if (";".equals(token)) {
                code.append(token);
            } else if (noSpaceBefore.contains(token)) {
                code.append(token);
            } else if (".".equals(previousToken)) {
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
        if (typedCodeInput == null) {
            return;
        }

        String code = typedCodeInput.getText();
        ValidationResult tokenUsageResult = TypedTokenUsageValidator.validate(code, inventory.getTokenValues());
        if (!tokenUsageResult.isValid()) {
            showFeedback(tokenUsageResult);
            return;
        }

        submitCode(code);
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
        refreshSelectedTokens(true);
    }

    private void refreshSelectedTokens(boolean updateTypedInput) {
        selectedTokenBox.getChildren().clear();
        for (int i = 0; i < selectedTokens.size(); i++) {
            int selectedIndex = i;
            Label label = new Label(selectedTokens.get(i));
            label.getStyleClass().add("selected-token");
            label.setOnMouseClicked(event -> {
                SoundManager.play(SoundEffect.BUTTON);
                removeTokenAt(selectedIndex);
            });
            label.setOnDragDetected(event -> {
                Dragboard dragboard = label.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(SELECTED_DRAG_PREFIX + selectedIndex);
                dragboard.setContent(content);
                event.consume();
            });
            label.setOnDragOver(event -> {
                if (isSupportedTokenDrag(event.getDragboard())) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });
            label.setOnDragDropped(event -> {
                event.setDropCompleted(handleTokenDrop(event.getDragboard(), selectedIndex));
                event.consume();
            });
            selectedTokenBox.getChildren().add(label);
        }

        String code = buildCodeString();
        answerPreviewLabel.setText(code.isBlank() ? "(empty)" : code);
        if (updateTypedInput) {
            syncTypedInputFromTokens(code);
        }
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
        boolean hasTypedCode = typedCodeInput != null && !typedCodeInput.getText().isBlank();
        submitButton.setDisable(!hasTypedCode || solved || !showGoal);
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
            button.setOnAction(event -> {
                SoundManager.play(SoundEffect.BUTTON);
                addTokenToAnswer(tokenIndex, value);
            });
            button.setOnDragDetected(event -> {
                if (button.isDisabled()) {
                    return;
                }
                Dragboard dragboard = button.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(AVAILABLE_DRAG_PREFIX + tokenIndex);
                dragboard.setContent(content);
                event.consume();
            });
            availableTokenButtons.add(button);
            availableTokenBox.getChildren().add(button);
        }
    }

    private void syncTokensFromTypedCode(String code) {
        if (syncingTypedCode || solved) {
            return;
        }

        TypedTokenUsageValidator.resolveTokenIndexes(code, inventory.getTokenValues()).ifPresentOrElse(indexes -> {
            selectedTokens.clear();
            selectedTokenIndexes.clear();
            List<String> tokenValues = inventory.getTokenValues();
            for (Integer tokenIndex : indexes) {
                selectedTokenIndexes.add(tokenIndex);
                selectedTokens.add(tokenValues.get(tokenIndex));
            }
            feedbackLabel.setText("");
            refreshSelectedTokens(false);
            refreshAvailableTokens();
        }, () -> {
            selectedTokens.clear();
            selectedTokenIndexes.clear();
            refreshSelectedTokens(false);
            refreshAvailableTokens();
        });
    }

    private void syncTypedInputFromTokens(String code) {
        if (typedCodeInput == null) {
            return;
        }

        syncingTypedCode = true;
        typedCodeInput.setText(code);
        syncingTypedCode = false;
    }

    private void configureSelectedTokenBoxDropTarget() {
        selectedTokenBox.setOnDragOver(event -> {
            if (isSupportedTokenDrag(event.getDragboard())) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });
        selectedTokenBox.setOnDragDropped(event -> {
            event.setDropCompleted(handleTokenDrop(event.getDragboard(), selectedTokens.size()));
            event.consume();
        });
    }

    private boolean isSupportedTokenDrag(Dragboard dragboard) {
        if (dragboard == null || !dragboard.hasString()) {
            return false;
        }

        String payload = dragboard.getString();
        return payload.startsWith(AVAILABLE_DRAG_PREFIX) || payload.startsWith(SELECTED_DRAG_PREFIX);
    }

    private boolean handleTokenDrop(Dragboard dragboard, int insertionIndex) {
        if (!isSupportedTokenDrag(dragboard) || solved) {
            return false;
        }

        String payload = dragboard.getString();
        try {
            if (payload.startsWith(AVAILABLE_DRAG_PREFIX)) {
                int tokenIndex = Integer.parseInt(payload.substring(AVAILABLE_DRAG_PREFIX.length()));
                List<String> tokenValues = inventory.getTokenValues();
                if (tokenIndex < 0 || tokenIndex >= tokenValues.size()) {
                    return false;
                }
                addTokenToAnswerAt(tokenIndex, tokenValues.get(tokenIndex), insertionIndex);
                return true;
            }

            int selectedIndex = Integer.parseInt(payload.substring(SELECTED_DRAG_PREFIX.length()));
            moveSelectedToken(selectedIndex, insertionIndex);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void moveSelectedToken(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= selectedTokens.size()) {
            return;
        }

        String token = selectedTokens.remove(fromIndex);
        Integer tokenIndex = selectedTokenIndexes.remove(fromIndex);
        int insertionIndex = Math.max(0, Math.min(toIndex, selectedTokens.size()));
        if (insertionIndex > fromIndex) {
            insertionIndex--;
        }
        selectedTokens.add(insertionIndex, token);
        selectedTokenIndexes.add(insertionIndex, tokenIndex);
        feedbackLabel.setText("");
        refreshSelectedTokens();
        refreshAvailableTokens();
        updateControls();
    }
}

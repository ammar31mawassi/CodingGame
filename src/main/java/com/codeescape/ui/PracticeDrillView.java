package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.PracticeDrillEvaluator;
import com.codeescape.engine.PracticePrompt;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class PracticeDrillView {
    private PracticeDrillView() {
    }

    public static VBox create(GameApp app, PracticePrompt prompt, String notebookTitle, boolean countsTowardRecovery) {
        Label title = new Label("Practice Drill: " + prompt.title());
        title.getStyleClass().add("modal-section-title");

        Label summary = new Label(prompt.summary());
        summary.getStyleClass().add("modal-copy");
        summary.setWrapText(true);
        summary.setMaxWidth(760);

        Label notebook = new Label("Linked notebook pattern: " + notebookTitle);
        notebook.getStyleClass().add("level-complete-reward");
        notebook.setWrapText(true);
        notebook.setMaxWidth(760);

        Label task = new Label(prompt.task());
        task.getStyleClass().add("modal-copy");
        task.setWrapText(true);
        task.setMaxWidth(760);

        Label tips = new Label("Tips: " + String.join("  |  ", prompt.coachingTips()));
        tips.getStyleClass().add("modal-copy");
        tips.setWrapText(true);
        tips.setMaxWidth(760);

        VBox card = new VBox(10, title, summary, notebook, task, tips);

        if (!prompt.challengeCode().isBlank()) {
            Label snippetLabel = new Label("Snippet to fix");
            snippetLabel.getStyleClass().add("level-complete-reward");

            Label snippet = new Label(prompt.challengeCode());
            snippet.getStyleClass().add("answer-preview");
            snippet.setWrapText(true);
            snippet.setMaxWidth(760);
            card.getChildren().addAll(snippetLabel, snippet);
        }

        TextArea answerInput = new TextArea();
        answerInput.setPromptText("Type your practice answer here.");
        answerInput.getStyleClass().add("typed-code-input");
        answerInput.setPrefRowCount(4);
        answerInput.setWrapText(false);
        answerInput.setMaxWidth(760);
        if (!prompt.challengeCode().isBlank()) {
            answerInput.setText(prompt.challengeCode());
        }

        Label feedback = new Label("Solve the drill here to log practice for this concept.");
        feedback.getStyleClass().add("modal-copy");
        feedback.setWrapText(true);
        feedback.setMaxWidth(760);

        Label solution = new Label();
        solution.getStyleClass().add("answer-preview");
        solution.setWrapText(true);
        solution.setMaxWidth(760);
        solution.setVisible(false);
        solution.setManaged(false);

        final boolean[] solved = {false};

        Button checkButton = new Button("Check Answer");
        checkButton.getStyleClass().add("pixel-button");
        checkButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            if (PracticeDrillEvaluator.matches(prompt, answerInput.getText())) {
                feedback.setText(countsTowardRecovery
                        ? "Correct. Practice logged and recovery reward is armed for a clean retry."
                        : "Correct. Practice logged for this concept.");
                feedback.getStyleClass().setAll("achievement-unlocked-copy");
                if (!solved[0]) {
                    app.recordPracticeCompletion(prompt, countsTowardRecovery);
                    solved[0] = true;
                }
            } else {
                feedback.setText("Not quite yet. Compare your structure to the tips and try again.");
                feedback.getStyleClass().setAll("modal-copy");
            }
        });

        Button revealButton = new Button("Reveal Sample");
        revealButton.getStyleClass().add("pixel-button");
        revealButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            solution.setText(prompt.sampleSolution());
            solution.setVisible(true);
            solution.setManaged(true);
        });

        HBox actions = new HBox(10, checkButton, revealButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(answerInput, actions, feedback, solution);
        card.getStyleClass().add("stage-recap-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(800);
        return card;
    }
}

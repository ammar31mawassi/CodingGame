package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.AchievementId;
import com.codeescape.engine.LevelCompletionSummary;
import com.codeescape.engine.NotebookEntry;
import com.codeescape.model.Level;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LevelCompleteView {
    private final GameApp app;
    private final Level completedLevel;

    public LevelCompleteView(GameApp app, Level completedLevel) {
        this.app = app;
        this.completedLevel = completedLevel;
    }

    public Parent createView() {
        LevelCompletionSummary summary = app.getLastCompletionSummary();

        Label title = new Label("Congrats!");
        title.getStyleClass().add("level-complete-title");

        Label message = new Label("Now to the next level.");
        message.getStyleClass().add("level-complete-message");

        Label concept = new Label("Completed " + completedLevel.getDisplayId() + ": " + completedLevel.getConcept());
        concept.getStyleClass().add("level-complete-concept");

        Label explanation = new Label(completedLevel.getCompletionExplanation());
        explanation.setWrapText(true);
        explanation.setMaxWidth(820);
        explanation.getStyleClass().add("level-complete-explanation");

        Label medal = new Label("Medal earned: " + summary.medalRank().getDisplayName());
        medal.getStyleClass().add("level-complete-reward");

        Button nextButton = new Button("Next Level");
        nextButton.getStyleClass().add("pixel-button");
        nextButton.setOnAction(event -> app.goToNextLevel());

        VBox root = new VBox(20, title, message, concept, medal);
        if (!completedLevel.getCompletionExplanation().isBlank()) {
            root.getChildren().add(explanation);
        }
        for (AchievementId achievementId : summary.newAchievements()) {
            Label achievement = new Label("Unlocked achievement: " + achievementId.getTitle());
            achievement.getStyleClass().add("level-complete-reward");
            root.getChildren().add(achievement);
        }
        for (NotebookEntry entry : summary.newNotebookEntries()) {
            Label notebook = new Label("Notebook entry added: " + entry.title());
            notebook.getStyleClass().add("level-complete-reward");
            root.getChildren().add(notebook);
        }
        root.getChildren().add(nextButton);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("level-complete-screen");
        return root;
    }
}

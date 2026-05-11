package com.codeescape.ui;

import com.codeescape.app.GameApp;
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

        Button nextButton = new Button("Next Level");
        nextButton.getStyleClass().add("pixel-button");
        nextButton.setOnAction(event -> app.goToNextLevel());

        VBox root = new VBox(20, title, message, concept);
        if (!completedLevel.getCompletionExplanation().isBlank()) {
            root.getChildren().add(explanation);
        }
        root.getChildren().add(nextButton);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("level-complete-screen");
        return root;
    }
}

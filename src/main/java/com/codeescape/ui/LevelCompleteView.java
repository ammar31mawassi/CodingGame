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
        Label title = new Label("Level Complete");
        Label concept = new Label("Concept learned: " + completedLevel.getConcept());
        Button nextButton = new Button("Next Level");
        nextButton.setOnAction(event -> app.goToNextLevel());

        VBox root = new VBox(20, title, concept, nextButton);
        root.setAlignment(Pos.CENTER);
        return root;
    }
}

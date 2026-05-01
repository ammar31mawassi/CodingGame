package com.codeescape.ui;

import com.codeescape.app.GameApp;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameOverView {
    private final GameApp app;

    public GameOverView(GameApp app) {
        this.app = app;
    }

    public Parent createView() {
        Label title = new Label("Code Escape Complete");
        Label summary = new Label("You completed variables, if-statements, and classes.");
        Button restartButton = new Button("Restart");
        restartButton.setOnAction(event -> app.startNewGame());

        VBox root = new VBox(20, title, summary, restartButton);
        root.setAlignment(Pos.CENTER);
        return root;
    }
}

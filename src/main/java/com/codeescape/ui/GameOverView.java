package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.model.GameMode;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameOverView {
    private final GameApp app;

    public GameOverView(GameApp app) {
        this.app = app;
    }

    public Parent createView() {
        Label title = new Label("Code Escape Complete");
        Label summary = new Label("You completed variables, printing, conditions, strings, loops, classes, and objects.");
        Button normalRestartButton = new Button("Restart Normal");
        normalRestartButton.getStyleClass().add("pixel-button");
        normalRestartButton.setOnAction(event -> app.startNewGame(GameMode.NORMAL));

        Button hardRestartButton = new Button("Restart Hard");
        hardRestartButton.getStyleClass().add("pixel-button");
        hardRestartButton.setOnAction(event -> app.startNewGame(GameMode.HARD));

        Button menuButton = new Button("Main Menu");
        menuButton.getStyleClass().add("pixel-button");
        menuButton.setOnAction(event -> app.showMainMenu());

        HBox restartOptions = new HBox(12, normalRestartButton, hardRestartButton, menuButton);
        restartOptions.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, summary, restartOptions);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("level-complete-screen");
        return root;
    }
}

package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.model.Level;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class MainMenuView {
    private final GameApp app;

    public MainMenuView(GameApp app) {
        this.app = app;
    }

    public Parent createView() {
        Label title = new Label("Code Escape");
        title.getStyleClass().add("title");

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("pixel-button");
        startButton.setOnAction(event -> app.startNewGame());

        Label selectorTitle = new Label("Level Select");
        selectorTitle.getStyleClass().add("menu-subtitle");

        FlowPane levelSelector = new FlowPane(10, 10);
        levelSelector.setAlignment(Pos.CENTER);
        levelSelector.setMaxWidth(720);
        for (Level level : app.getAvailableLevels()) {
            Button levelButton = new Button("Level " + level.getLevelNumber());
            levelButton.getStyleClass().add("pixel-button");
            levelButton.setOnAction(event -> app.startAtLevel(level.getLevelNumber()));
            levelSelector.getChildren().add(levelButton);
        }

        VBox root = new VBox(20, title, startButton, selectorTitle, levelSelector);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("main-menu");
        return root;
    }
}

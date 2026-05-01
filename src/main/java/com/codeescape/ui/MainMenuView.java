package com.codeescape.ui;

import com.codeescape.app.GameApp;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
        startButton.setOnAction(event -> app.startNewGame());

        VBox root = new VBox(20, title, startButton);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("main-menu");
        return root;
    }
}

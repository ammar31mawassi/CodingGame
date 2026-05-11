package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainMenuView {
    private final GameApp app;

    public MainMenuView(GameApp app) {
        this.app = app;
    }

    public Parent createView() {
        Label title = new Label("Code Escape");
        title.getStyleClass().add("title");

        Label modeTitle = new Label("Choose Mode");
        modeTitle.getStyleClass().add("menu-subtitle");

        Button normalModeButton = createModeButton(GameMode.NORMAL);
        Button hardModeButton = createModeButton(GameMode.HARD);
        normalModeButton.setOnAction(event -> {
            app.setSelectedGameMode(GameMode.NORMAL);
            refreshModeButtons(normalModeButton, hardModeButton);
        });
        hardModeButton.setOnAction(event -> {
            app.setSelectedGameMode(GameMode.HARD);
            refreshModeButtons(normalModeButton, hardModeButton);
        });
        refreshModeButtons(normalModeButton, hardModeButton);

        HBox modeSelector = new HBox(12, normalModeButton, hardModeButton);
        modeSelector.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("pixel-button");
        startButton.setOnAction(event -> app.startNewGame(app.getSelectedGameMode()));

        VBox actionButtons = new VBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        if (app.hasContinuableSave()) {
            Button continueButton = new Button("Continue Game");
            continueButton.getStyleClass().add("pixel-button");
            continueButton.setOnAction(event -> app.continueGame());
            actionButtons.getChildren().add(continueButton);
        }
        actionButtons.getChildren().add(startButton);

        Label selectorTitle = new Label("Level Select");
        selectorTitle.getStyleClass().add("menu-subtitle");

        VBox levelSelector = new VBox(12);
        levelSelector.setAlignment(Pos.CENTER);
        levelSelector.setMaxWidth(860);
        int highestSelectableLevel = app.getHighestSelectableLevel();
        for (Map.Entry<String, List<Level>> stage : levelsByStage().entrySet()) {
            Label stageLabel = new Label(stage.getKey());
            stageLabel.getStyleClass().add("stage-title");

            FlowPane stageLevels = new FlowPane(10, 10);
            stageLevels.setAlignment(Pos.CENTER);
            for (Level level : stage.getValue()) {
                Button levelButton = new Button(level.getDisplayId());
                levelButton.getStyleClass().add("pixel-button");
                levelButton.setDisable(level.getLevelNumber() > highestSelectableLevel);
                levelButton.setTooltip(new Tooltip(level.getName() + " - " + level.getConcept()));
                levelButton.setOnAction(event -> app.startAtLevel(level.getLevelNumber()));
                stageLevels.getChildren().add(levelButton);
            }

            levelSelector.getChildren().addAll(stageLabel, stageLevels);
        }

        VBox root = new VBox(20, title, modeTitle, modeSelector, actionButtons, selectorTitle, levelSelector);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("main-menu");
        return root;
    }

    private Button createModeButton(GameMode mode) {
        Button button = new Button(mode.getDisplayName());
        button.getStyleClass().add("pixel-button");
        return button;
    }

    private void refreshModeButtons(Button normalModeButton, Button hardModeButton) {
        normalModeButton.getStyleClass().remove("mode-button-selected");
        hardModeButton.getStyleClass().remove("mode-button-selected");
        if (app.getSelectedGameMode() == GameMode.HARD) {
            hardModeButton.getStyleClass().add("mode-button-selected");
        } else {
            normalModeButton.getStyleClass().add("mode-button-selected");
        }
    }

    private Map<String, List<Level>> levelsByStage() {
        Map<String, List<Level>> stages = new LinkedHashMap<>();
        for (Level level : app.getAvailableLevels()) {
            String stageLabel = "Stage " + level.getStageNumber() + ": " + level.getStageTitle();
            stages.computeIfAbsent(stageLabel, key -> new java.util.ArrayList<>()).add(level);
        }
        return stages;
    }
}

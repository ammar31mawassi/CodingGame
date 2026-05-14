package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminView {
    private final GameApp app;

    public AdminView(GameApp app) {
        this.app = app;
    }

    public Parent createView() {
        Label title = new Label("Admin View");
        title.getStyleClass().add("title");

        Label note = new Label("All levels are unlocked here. Admin runs are unsaved, so your normal save stays untouched.");
        note.getStyleClass().add("admin-note");

        Button normalModeButton = createModeButton(GameMode.NORMAL);
        Button hardModeButton = createModeButton(GameMode.HARD);
        normalModeButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.setSelectedGameMode(GameMode.NORMAL);
            refreshModeButtons(normalModeButton, hardModeButton);
        });
        hardModeButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.setSelectedGameMode(GameMode.HARD);
            refreshModeButtons(normalModeButton, hardModeButton);
        });
        refreshModeButtons(normalModeButton, hardModeButton);

        HBox modeSelector = new HBox(12, normalModeButton, hardModeButton);
        modeSelector.setAlignment(Pos.CENTER);

        VBox levelSelector = new VBox(12);
        levelSelector.setAlignment(Pos.CENTER);
        levelSelector.setMaxWidth(980);
        for (Map.Entry<String, List<Level>> stage : levelsByStage().entrySet()) {
            Label stageLabel = new Label(stage.getKey());
            stageLabel.getStyleClass().add("stage-title");

            FlowPane stageLevels = new FlowPane(10, 10);
            stageLevels.setAlignment(Pos.CENTER);
            for (Level level : stage.getValue()) {
                Button levelButton = new Button(level.getDisplayId());
                levelButton.getStyleClass().addAll("pixel-button", "level-select-button");
                levelButton.setTooltip(new Tooltip(level.getName() + " - " + level.getConcept()));
                levelButton.setOnAction(event -> {
                    SoundManager.play(SoundEffect.BUTTON);
                    app.startAdminAtLevel(level.getLevelNumber());
                });
                stageLevels.getChildren().add(levelButton);
            }

            levelSelector.getChildren().addAll(stageLabel, stageLevels);
        }

        Button backButton = new Button("Main Menu");
        backButton.getStyleClass().add("pixel-button");
        backButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.showMainMenu();
        });

        VBox content = new VBox(18, title, note, modeSelector, levelSelector, backButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));
        content.getStyleClass().add("admin-view");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("admin-scroll");
        return scrollPane;
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
            stages.computeIfAbsent(stageLabel, key -> new ArrayList<>()).add(level);
        }
        return stages;
    }
}

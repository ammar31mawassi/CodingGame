package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.FocusRouteKind;
import com.codeescape.engine.FocusRouteRecommendation;
import com.codeescape.engine.MedalRank;
import com.codeescape.engine.NotebookEntry;
import com.codeescape.engine.NotebookLibrary;
import com.codeescape.engine.PlayerProgressProfile;
import com.codeescape.engine.StageMilestoneReward;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuView {
    private final GameApp app;

    public MainMenuView(GameApp app) {
        this.app = app;
    }

    public Parent createView() {
        Label title = new Label("Code Escape");
        title.getStyleClass().add("title");

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

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().addAll("pixel-button", "primary-action-button");
        startButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.startNewGame(app.getSelectedGameMode());
        });

        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        if (app.hasContinuableSave()) {
            Button continueButton = new Button("Continue Game");
            continueButton.getStyleClass().addAll("pixel-button", "primary-action-button");
            continueButton.setOnAction(event -> {
                SoundManager.play(SoundEffect.BUTTON);
                app.continueGame();
            });
            actionButtons.getChildren().add(continueButton);
        }
        actionButtons.getChildren().add(startButton);
        Button adminButton = new Button("Admin View");
        adminButton.getStyleClass().addAll("pixel-button", "secondary-action-button");
        adminButton.setTooltip(new Tooltip("Open all levels without changing saved progress."));
        adminButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.showAdminView();
        });
        actionButtons.getChildren().add(adminButton);

        Label selectorTitle = new Label("Level Select");
        selectorTitle.getStyleClass().add("menu-subtitle");

        VBox header = new VBox(12, PixelArtView.terminalLogo(5), title, modeSelector, actionButtons, selectorTitle);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("main-menu-header");

        VBox levelSelector = new VBox(16);
        levelSelector.setAlignment(Pos.CENTER);
        levelSelector.setFillWidth(false);
        levelSelector.setPrefWidth(680);
        levelSelector.setMaxWidth(680);
        levelSelector.getStyleClass().add("level-selector");
        int highestSelectableLevel = app.getHighestSelectableLevel();
        PlayerProgressProfile profile = app.getPlayerProfile();
        levelSelector.getChildren().add(createDailyChallengePanel());
        Node customChallengePanel = createCustomChallengeHub();
        if (customChallengePanel != null) {
            levelSelector.getChildren().add(customChallengePanel);
        }
        Node focusRoutePanel = createFocusRoutePanel(app.getFocusRouteRecommendations());
        if (focusRoutePanel != null) {
            levelSelector.getChildren().add(focusRoutePanel);
        }
        for (Map.Entry<String, List<Level>> stage : levelsByStage().entrySet()) {
            Label stageLabel = new Label(stage.getKey());
            stageLabel.getStyleClass().add("stage-title");

            Node stageSummary = createStageSummary(stage.getValue(), profile, highestSelectableLevel);

            FlowPane stageLevels = new FlowPane(10, 10);
            stageLevels.setAlignment(Pos.CENTER);
            for (Level level : stage.getValue()) {
                Button levelButton = new Button(level.getDisplayId());
                levelButton.getStyleClass().addAll("pixel-button", "level-select-button");
                levelButton.setDisable(level.getLevelNumber() > highestSelectableLevel);
                levelButton.setTooltip(new Tooltip(level.getName() + " - " + level.getConcept()));
                levelButton.setOnAction(event -> {
                    SoundManager.play(SoundEffect.BUTTON);
                    app.startAtLevel(level.getLevelNumber());
                });
                stageLevels.getChildren().add(levelButton);
            }

            VBox stageSection = new VBox(8, stageLabel, stageSummary, stageLevels);
            stageSection.setAlignment(Pos.CENTER);
            stageSection.setPrefWidth(680);
            stageSection.setMaxWidth(680);
            stageSection.getStyleClass().add("stage-section");
            levelSelector.getChildren().add(stageSection);
        }

        StackPane levelWrapper = new StackPane(levelSelector);
        levelWrapper.setAlignment(Pos.TOP_CENTER);
        levelWrapper.getStyleClass().add("level-selector-wrapper");

        ScrollPane levelScroll = new ScrollPane(levelWrapper);
        levelScroll.setFitToWidth(true);
        levelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        levelScroll.setPrefWidth(740);
        levelScroll.setMaxWidth(740);
        levelScroll.getStyleClass().add("main-menu-scroll");
        VBox.setVgrow(levelScroll, Priority.ALWAYS);

        VBox root = new VBox(12, header, levelScroll);
        root.setAlignment(Pos.TOP_CENTER);
        root.setFillWidth(false);
        root.setPadding(new Insets(18, 28, 22, 28));
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

    private Node createStageSummary(List<Level> stageLevels, PlayerProgressProfile profile, int highestSelectableLevel) {
        if (stageLevels.isEmpty()) {
            return new HBox();
        }

        List<Level> orderedLevels = stageLevels.stream()
                .sorted(Comparator.comparingInt(Level::getStageLevelNumber))
                .toList();
        int clearedLevels = (int) orderedLevels.stream()
                .filter(level -> profile.levelMedals().containsKey(level.getLevelNumber()))
                .count();
        long goldMedals = orderedLevels.stream()
                .map(level -> profile.levelMedals().get(level.getLevelNumber()))
                .filter(MedalRank.GOLD::equals)
                .count();
        long silverMedals = orderedLevels.stream()
                .map(level -> profile.levelMedals().get(level.getLevelNumber()))
                .filter(MedalRank.SILVER::equals)
                .count();
        long bronzeMedals = orderedLevels.stream()
                .map(level -> profile.levelMedals().get(level.getLevelNumber()))
                .filter(MedalRank.BRONZE::equals)
                .count();

        List<NotebookEntry> stageEntries = NotebookLibrary.allEntries().stream()
                .filter(entry -> belongsToStage(entry, orderedLevels))
                .toList();
        long unlockedEntries = stageEntries.stream()
                .filter(entry -> profile.unlockedNotebookEntries().contains(entry.id()))
                .count();

        int unlockedLevels = (int) orderedLevels.stream()
                .filter(level -> level.getLevelNumber() <= highestSelectableLevel)
                .count();

        Label summary = new Label("Cleared " + clearedLevels + "/" + orderedLevels.size()
                + "  Medals G:" + goldMedals + " S:" + silverMedals + " B:" + bronzeMedals
                + "  Notebook " + unlockedEntries + "/" + stageEntries.size()
                + "  Unlocked " + unlockedLevels + "/" + orderedLevels.size());
        summary.setAlignment(Pos.CENTER);
        summary.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        summary.setPrefWidth(640);
        summary.setMaxWidth(640);
        summary.setWrapText(true);
        summary.getStyleClass().add("stage-summary");

        VBox box = new VBox(6, summary);
        box.setAlignment(Pos.CENTER);

        int stageNumber = orderedLevels.get(0).getStageNumber();
        List<StageMilestoneReward> stageRewards = app.getUnlockedStageRewardsForStage(stageNumber);
        if (!stageRewards.isEmpty()) {
            Label rewards = new Label("Rewards: " + stageRewards.stream().map(StageMilestoneReward::title).collect(java.util.stream.Collectors.joining(" | ")));
            rewards.getStyleClass().add("stage-reward-summary");
            rewards.setWrapText(true);
            rewards.setMaxWidth(640);
            rewards.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            box.getChildren().add(rewards);
        }

        List<FocusRouteRecommendation> recommendations = app.getFocusRouteRecommendationsForStage(stageNumber);
        if (!recommendations.isEmpty()) {
            VBox routeBox = new VBox(6);
            routeBox.setAlignment(Pos.CENTER);
            for (FocusRouteRecommendation recommendation : recommendations) {
                routeBox.getChildren().add(createRouteRow(recommendation));
            }
            box.getChildren().add(routeBox);
        }

        HBox routes = createStageRouteButtons(stageNumber);
        if (routes != null) {
            box.getChildren().add(routes);
        }

        return box;
    }

    private boolean belongsToStage(NotebookEntry entry, List<Level> stageLevels) {
        return stageLevels.stream().anyMatch(level -> level.getLevelNumber() == entry.unlockLevelNumber());
    }

    private Node createFocusRoutePanel(List<FocusRouteRecommendation> recommendations) {
        if (recommendations.isEmpty()) {
            return null;
        }

        Label title = new Label("Focus Route");
        title.getStyleClass().add("menu-subtitle");

        Label summary = new Label("Next best drill, replay, and push from your medals, hints, bugs, and practice history.");
        summary.getStyleClass().add("modal-copy");
        summary.setWrapText(true);
        summary.setMaxWidth(620);
        summary.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox card = new VBox(10, title, summary);
        card.getStyleClass().add("focus-route-compact");
        for (FocusRouteRecommendation recommendation : recommendations) {
            card.getChildren().add(createRouteRow(recommendation));
        }
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("focus-route-card");
        card.setMaxWidth(680);
        return card;
    }

    private Node createDailyChallengePanel() {
        Level daily = app.getDailyChallengeLevel();
        Label title = new Label("Daily Challenge");
        title.getStyleClass().add("menu-subtitle");

        Label summary = new Label(app.getDailyChallengeLabel()
                + " route: "
                + daily.getName()
                + " ("
                + daily.getDisplayId()
                + ") in "
                + app.getSelectedGameMode().getDisplayName()
                + ".");
        summary.getStyleClass().add("modal-copy");
        summary.setWrapText(true);
        summary.setMaxWidth(620);
        summary.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox card = new VBox(10, title, summary);
        app.getMedalContract(daily).ifPresent(contract -> {
            Label contractLabel = new Label("Today's contract: " + contract.title() + " - " + contract.description());
            contractLabel.getStyleClass().add("stage-summary");
            contractLabel.setWrapText(true);
            contractLabel.setMaxWidth(620);
            contractLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            card.getChildren().add(contractLabel);
        });

        Button play = new Button("Play Daily");
        play.getStyleClass().addAll("pixel-button", "primary-action-button");
        play.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.startDailyChallenge();
        });

        card.getChildren().add(play);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("focus-route-card", "daily-route-card");
        card.setMaxWidth(680);
        return card;
    }

    private Node createCustomChallengeHub() {
        List<Level> customLevels = app.getCustomChallengeLevels();
        if (customLevels.isEmpty()) {
            return null;
        }

        Label title = new Label("Custom Challenge Hub");
        title.getStyleClass().add("menu-subtitle");

        Label summary = new Label("Editor override rooms open here as standalone runs, so players can test custom layouts without touching campaign progress.");
        summary.getStyleClass().add("modal-copy");
        summary.setWrapText(true);
        summary.setMaxWidth(620);
        summary.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox card = new VBox(10, title, summary);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("focus-route-card", "custom-hub-card");
        card.setMaxWidth(680);
        for (Level level : customLevels) {
            Label copy = new Label("Custom " + level.getDisplayId() + ": " + level.getName() + " | " + level.getRoom().getPuzzle().getTitle());
            copy.getStyleClass().add("stage-summary");
            copy.setWrapText(true);
            copy.setMaxWidth(470);

            Button play = new Button("Play Custom");
            play.getStyleClass().addAll("pixel-button", "primary-action-button", "route-action-button");
            play.setOnAction(event -> {
                SoundManager.play(SoundEffect.BUTTON);
                app.startCustomChallenge(level.getLevelNumber());
            });

            HBox row = new HBox(10, copy, play);
            row.setAlignment(Pos.CENTER);
            row.getStyleClass().add("focus-route-row");
            card.getChildren().add(row);
        }
        return card;
    }

    private HBox createStageRouteButtons(int stageNumber) {
        Button revisionButton = app.getRevisionWingForStage(stageNumber)
                .map(level -> {
                    Button button = new Button("Play " + level.getDisplayId());
                    button.getStyleClass().addAll("pixel-button", "secondary-action-button");
                    button.setTooltip(new Tooltip(level.getName() + " - " + level.getConcept()));
                    button.setOnAction(event -> {
                        SoundManager.play(SoundEffect.BUTTON);
                        app.startRevisionWing(stageNumber);
                    });
                    return button;
                })
                .orElse(null);

        Button bossButton = app.getStageBossForStage(stageNumber)
                .map(level -> {
                    Button button = new Button("Fight " + level.getDisplayId());
                    button.getStyleClass().addAll("pixel-button", "secondary-action-button");
                    button.setTooltip(new Tooltip(level.getName() + " - " + app.stageBossRequirementLabel(stageNumber)));
                    button.setDisable(!app.canPlayStageBoss(stageNumber));
                    button.setOnAction(event -> {
                        SoundManager.play(SoundEffect.BUTTON);
                        app.startStageBoss(stageNumber);
                    });
                    return button;
                })
                .orElse(null);

        if (revisionButton == null && bossButton == null) {
            return null;
        }

        HBox row = revisionButton == null
                ? new HBox(10, bossButton)
                : bossButton == null ? new HBox(10, revisionButton) : new HBox(10, revisionButton, bossButton);
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add("stage-route-buttons");
        return row;
    }

    private Node createRouteRow(FocusRouteRecommendation recommendation) {
        Label copy = new Label(recommendation.headline() + ": " + recommendation.detail());
        copy.getStyleClass().add("stage-summary");
        copy.setWrapText(true);
        copy.setMaxWidth(470);

        Button action = new Button(recommendation.actionLabel());
        action.getStyleClass().addAll("pixel-button", "primary-action-button", "route-action-button");
        action.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            if (recommendation.kind() == FocusRouteKind.PRACTICE) {
                app.showPracticeRoute(recommendation.notebookEntryId(), recommendation.levelNumber());
            } else if (recommendation.levelNumber() != null) {
                app.startAtLevel(recommendation.levelNumber());
            }
        });

        HBox row = new HBox(10, copy, action);
        row.setAlignment(Pos.CENTER);
        row.getStyleClass().add("focus-route-row");
        return row;
    }
}

package com.codeescape.ui;

import com.codeescape.app.GameApp;
import com.codeescape.engine.AchievementId;
import com.codeescape.engine.FocusRouteKind;
import com.codeescape.engine.FocusRouteRecommendation;
import com.codeescape.engine.LevelCompletionSummary;
import com.codeescape.engine.NotebookEntry;
import com.codeescape.engine.PracticePrompt;
import com.codeescape.engine.MedalRank;
import com.codeescape.engine.StageMilestoneReward;
import com.codeescape.model.Level;
import java.util.EnumMap;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.HBox;
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
        SoundManager.play(SoundEffect.MEDAL);
        boolean stageFinal = app.isStageFinalLevel(completedLevel);

        Label title = new Label(stageFinal ? "Stage Cleared!" : "Congrats!");
        title.getStyleClass().add("level-complete-title");
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setPrefWidth(920);
        title.setMaxWidth(920);

        Label message = new Label(stageFinal
                ? "You wrapped up Stage " + completedLevel.getStageNumber() + ": " + completedLevel.getStageTitle() + "."
                : "Now to the next level.");
        message.getStyleClass().add("level-complete-message");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setPrefWidth(920);
        message.setMaxWidth(920);

        Label concept = new Label("Completed " + completedLevel.getDisplayId() + ": " + completedLevel.getConcept());
        concept.getStyleClass().add("level-complete-concept");
        concept.setWrapText(true);
        concept.setAlignment(Pos.CENTER);
        concept.setTextAlignment(TextAlignment.CENTER);
        concept.setPrefWidth(860);
        concept.setMaxWidth(860);

        Label explanation = new Label(completedLevel.getCompletionExplanation());
        explanation.setWrapText(true);
        explanation.setAlignment(Pos.CENTER);
        explanation.setTextAlignment(TextAlignment.CENTER);
        explanation.setPrefWidth(820);
        explanation.setMaxWidth(820);
        explanation.getStyleClass().add("level-complete-explanation");

        Label medalLabel = new Label("Medal earned");
        medalLabel.getStyleClass().add("level-complete-reward");
        HBox medal = new HBox(12, medalLabel, MedalBadgeView.create(summary.medalRank()));
        medal.setAlignment(Pos.CENTER);
        medal.getStyleClass().add("level-complete-medal-row");

        Button nextButton = new Button(app.isStandaloneSession() ? "Back To Menu" : stageFinal ? "Next Stage" : "Next Level");
        nextButton.getStyleClass().add("pixel-button");
        nextButton.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            app.advanceAfterLevelComplete();
        });

        VBox root = new VBox(stageFinal ? 14 : 20, title, message, concept, medal);
        if (summary.medalContract() != null) {
            Label contract = new Label("Contract " + summary.medalContract().title() + ": "
                    + (summary.medalContractCompleted() ? "completed" : "missed"));
            contract.getStyleClass().add(summary.medalContractCompleted() ? "achievement-unlocked-copy" : "modal-copy");
            contract.setWrapText(true);
            root.getChildren().add(contract);
        }
        if (!stageFinal && !completedLevel.getCompletionExplanation().isBlank()) {
            root.getChildren().add(explanation);
        }
        if (!stageFinal) {
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
            for (NotebookEntry entry : summary.newRecoveryStamps()) {
                Label recovery = new Label("Recovery stamp earned: " + entry.title());
                recovery.getStyleClass().add("achievement-unlocked-copy");
                root.getChildren().add(recovery);
            }
            for (StageMilestoneReward reward : summary.newStageRewards()) {
                Label rewardLabel = new Label("Stage reward unlocked: " + reward.title());
                rewardLabel.getStyleClass().add("achievement-unlocked-copy");
                rewardLabel.setWrapText(true);
                root.getChildren().add(rewardLabel);
            }
        }
        if (stageFinal) {
            root.getChildren().add(createStageRecap(summary));
            app.getPracticePromptForLevel(completedLevel).ifPresent(prompt -> root.getChildren().add(
                    PracticeDrillView.create(app, prompt, notebookTitle(prompt.notebookEntryId()), false)
            ));
        }
        root.getChildren().add(nextButton);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("level-complete-screen");
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().addAll("screen-scroll", "level-complete-scroll");
        return scrollPane;
    }

    private VBox createStageRecap(LevelCompletionSummary summary) {
        List<Level> stageLevels = app.getAvailableLevels().stream()
                .filter(level -> level.getStageNumber() == completedLevel.getStageNumber())
                .toList();
        long clearedLevels = stageLevels.stream()
                .filter(level -> app.getPlayerProfile().levelMedals().containsKey(level.getLevelNumber()))
                .count();
        EnumMap<MedalRank, Long> medalCounts = new EnumMap<>(MedalRank.class);
        for (MedalRank medalRank : MedalRank.values()) {
            long count = stageLevels.stream()
                    .filter(level -> app.getPlayerProfile().levelMedals().get(level.getLevelNumber()) == medalRank)
                    .count();
            medalCounts.put(medalRank, count);
        }

        List<NotebookEntry> stageEntries = app.getNotebookEntriesForStage(completedLevel.getStageNumber());
        long unlockedEntries = stageEntries.stream()
                .filter(entry -> app.getPlayerProfile().unlockedNotebookEntries().contains(entry.id()))
                .count();

        Label recapTitle = new Label("Stage Recap");
        recapTitle.getStyleClass().add("modal-section-title");

        Label recapSummary = new Label("Cleared " + clearedLevels + "/" + stageLevels.size()
                + " levels | Gold " + medalCounts.getOrDefault(MedalRank.GOLD, 0L)
                + " | Silver " + medalCounts.getOrDefault(MedalRank.SILVER, 0L)
                + " | Bronze " + medalCounts.getOrDefault(MedalRank.BRONZE, 0L)
                + " | Notebook " + unlockedEntries + "/" + stageEntries.size());
        recapSummary.getStyleClass().add("modal-copy");
        recapSummary.setWrapText(true);
        recapSummary.setMaxWidth(760);

        String recapText = completedLevel.getCompletionExplanation().isBlank()
                ? "You closed this stage. Take one quick practice lap before the next concept stack starts."
                : completedLevel.getCompletionExplanation();
        Label reflection = new Label(recapText);
        reflection.getStyleClass().add("modal-copy");
        reflection.setWrapText(true);
        reflection.setMaxWidth(760);

        VBox recap = new VBox(10, recapTitle, recapSummary, reflection);
        if (!summary.newNotebookEntries().isEmpty()) {
            Label newEntryLabel = new Label("New study unlock: " + summary.newNotebookEntries().get(summary.newNotebookEntries().size() - 1).title());
            newEntryLabel.getStyleClass().add("level-complete-reward");
            recap.getChildren().add(newEntryLabel);
        }
        if (!summary.newAchievements().isEmpty()) {
            Label achievementsLabel = new Label("New achievements: " + summary.newAchievements().stream()
                    .map(AchievementId::getTitle)
                    .collect(java.util.stream.Collectors.joining(" | ")));
            achievementsLabel.getStyleClass().add("level-complete-reward");
            achievementsLabel.setWrapText(true);
            recap.getChildren().add(achievementsLabel);
        }
        if (!summary.newRecoveryStamps().isEmpty()) {
            Label recoveryLabel = new Label("Recovery stamps: " + summary.newRecoveryStamps().stream()
                    .map(NotebookEntry::title)
                    .collect(java.util.stream.Collectors.joining(" | ")));
            recoveryLabel.getStyleClass().add("achievement-unlocked-copy");
            recoveryLabel.setWrapText(true);
            recap.getChildren().add(recoveryLabel);
        }
        if (!summary.newStageRewards().isEmpty()) {
            Label rewardsLabel = new Label("New stage rewards: " + summary.newStageRewards().stream().map(StageMilestoneReward::title).collect(java.util.stream.Collectors.joining(" | ")));
            rewardsLabel.getStyleClass().add("achievement-unlocked-copy");
            rewardsLabel.setWrapText(true);
            recap.getChildren().add(rewardsLabel);
        } else {
            List<StageMilestoneReward> unlockedRewards = app.getUnlockedStageRewardsForStage(completedLevel.getStageNumber());
            if (!unlockedRewards.isEmpty()) {
                Label rewardsLabel = new Label("Stage rewards held: " + unlockedRewards.stream().map(StageMilestoneReward::title).collect(java.util.stream.Collectors.joining(" | ")));
                rewardsLabel.getStyleClass().add("level-complete-reward");
                rewardsLabel.setWrapText(true);
                recap.getChildren().add(rewardsLabel);
            }
        }
        List<FocusRouteRecommendation> recommendations = app.getFocusRouteRecommendationsForStage(completedLevel.getStageNumber());
        if (!recommendations.isEmpty()) {
            Label routeTitle = new Label("Recommended next route");
            routeTitle.getStyleClass().add("level-complete-reward");
            recap.getChildren().add(routeTitle);
            for (FocusRouteRecommendation recommendation : recommendations) {
                recap.getChildren().add(createRouteRow(recommendation));
            }
        }
        recap.getStyleClass().add("stage-recap-card");
        recap.setAlignment(Pos.CENTER_LEFT);
        recap.setMaxWidth(800);
        return recap;
    }

    private String notebookTitle(String entryId) {
        return app.getUnlockedNotebookEntries().stream()
                .filter(entry -> entry.id().equals(entryId))
                .map(NotebookEntry::title)
                .findFirst()
                .or(() -> app.getNotebookEntriesForStage(completedLevel.getStageNumber()).stream()
                        .filter(entry -> entry.id().equals(entryId))
                .map(NotebookEntry::title)
                .findFirst())
                .orElse("Notebook Pattern");
    }

    private HBox createRouteRow(FocusRouteRecommendation recommendation) {
        Label copy = new Label(recommendation.headline() + " - " + recommendation.detail());
        copy.getStyleClass().add("modal-copy");
        copy.setWrapText(true);
        copy.setMaxWidth(560);

        Button action = new Button(recommendation.actionLabel());
        action.getStyleClass().add("pixel-button");
        action.setOnAction(event -> {
            SoundManager.play(SoundEffect.BUTTON);
            if (recommendation.kind() == FocusRouteKind.PRACTICE) {
                app.showPracticeRoute(recommendation.notebookEntryId(), recommendation.levelNumber());
            } else if (recommendation.levelNumber() != null) {
                app.startAtLevel(recommendation.levelNumber());
            }
        });

        HBox row = new HBox(10, copy, action);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}

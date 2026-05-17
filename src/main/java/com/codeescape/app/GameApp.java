package com.codeescape.app;

import com.codeescape.engine.GameState;
import com.codeescape.engine.LevelManager;
import com.codeescape.engine.LevelCompletionSummary;
import com.codeescape.engine.ConceptProgressSnapshot;
import com.codeescape.engine.FocusRouteKind;
import com.codeescape.engine.FocusRouteRecommendation;
import com.codeescape.engine.MedalRank;
import com.codeescape.engine.NotebookEntry;
import com.codeescape.engine.NotebookLibrary;
import com.codeescape.engine.PracticeLibrary;
import com.codeescape.engine.PracticePrompt;
import com.codeescape.engine.PlayerProgressProfile;
import com.codeescape.engine.AchievementId;
import com.codeescape.engine.ProgressPolicy;
import com.codeescape.engine.ProgressRun;
import com.codeescape.engine.ProgressSaveService;
import com.codeescape.engine.SavedProgress;
import com.codeescape.engine.StageMilestoneReward;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
import com.codeescape.ui.AdminView;
import com.codeescape.ui.BugFailureView;
import com.codeescape.ui.GameOverView;
import com.codeescape.ui.GameView;
import com.codeescape.ui.LevelCompleteView;
import com.codeescape.ui.MainMenuView;
import com.codeescape.ui.PracticeFocusView;
import com.codeescape.util.Constants;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GameApp extends Application {
    private final ProgressSaveService saveService;
    private Stage primaryStage;
    private GameState gameState;
    private LevelManager levelManager;
    private GameMode selectedGameMode = GameMode.NORMAL;
    private ProgressRun progressRun = ProgressRun.unsaved();
    private PlayerProgressProfile playerProfile = PlayerProgressProfile.empty();
    private LevelCompletionSummary lastCompletionSummary = new LevelCompletionSummary(MedalRank.BRONZE, List.of(), List.of(), List.of(), List.of());
    private Integer recoveryCandidateLevelNumber;
    private String recoveryCandidateEntryId;
    private boolean recoveryPracticeCompleted;
    private boolean standaloneSession;

    public GameApp() {
        this(new ProgressSaveService());
    }

    GameApp(ProgressSaveService saveService) {
        this.saveService = saveService;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle(Constants.APP_TITLE);
        primaryStage.setResizable(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        ensureLevelsLoaded();
        showMainMenu();
        primaryStage.setMaximized(true);
        primaryStage.show();
        Platform.runLater(() -> primaryStage.setFullScreen(true));
    }

    public void showMainMenu() {
        ensureLevelsLoaded();
        standaloneSession = false;
        playerProfile = loadProfileWithEarnedStageRewards();
        setScene(new MainMenuView(this).createView());
    }

    public void showAdminView() {
        ensureLevelsLoaded();
        setScene(new AdminView(this).createView());
    }

    public void startNewGame() {
        requestNewGame(selectedGameMode);
    }

    public void startNewGame(GameMode gameMode) {
        requestNewGame(gameMode);
    }

    public void requestNewGame(GameMode gameMode) {
        GameMode mode = normalizeGameMode(gameMode);
        selectedGameMode = mode;

        ProgressRun run = loadSavedProgress().isPresent()
                ? (confirmOverwriteSave() ? ProgressRun.saveEnabled(Optional.empty()) : ProgressRun.unsaved())
                : ProgressRun.saveEnabled(Optional.empty());
        startFreshRun(mode, run);
    }

    public void continueGame() {
        Optional<SavedProgress> savedProgress = loadSavedProgress()
                .filter(progress -> !progress.gameFinished());
        if (savedProgress.isEmpty()) {
            showMainMenu();
            return;
        }

        startSavedLevel(savedProgress.get(), savedProgress.get().currentLevelNumber());
    }

    public void retryCurrentLevel() {
        if (gameState == null || gameState.getCurrentLevel() == null) {
            startNewGame(selectedGameMode);
            return;
        }

        if (standaloneSession) {
            startStandaloneLevel(gameState.getCurrentLevel().getLevelNumber(), gameState.getGameMode());
            return;
        }

        startLevel(
                gameState.getCurrentLevel().getLevelNumber(),
                gameState.getGameMode(),
                progressRun == null ? ProgressRun.unsaved() : progressRun,
                0,
                gameState.hasSeenTutorial(),
                false
        );
    }

    public void startAtLevel(int levelNumber) {
        startAtLevel(levelNumber, selectedGameMode);
    }

    public void startAtLevel(int levelNumber, GameMode gameMode) {
        selectedGameMode = normalizeGameMode(gameMode);
        startSelectableLevel(levelNumber);
    }

    public void startAdminAtLevel(int levelNumber) {
        ensureLevelsLoaded();
        if (levelNumber < 1 || levelNumber > getFinalLevelNumber()) {
            return;
        }

        startLevel(levelNumber, selectedGameMode, ProgressRun.unsaved(), 0, true, false);
    }

    public GameMode getSelectedGameMode() {
        return selectedGameMode;
    }

    public void setSelectedGameMode(GameMode selectedGameMode) {
        this.selectedGameMode = normalizeGameMode(selectedGameMode);
    }

    public List<Level> getAvailableLevels() {
        ensureLevelsLoaded();
        return levelManager.getLevels();
    }

    public Optional<Level> getRevisionWingForStage(int stageNumber) {
        ensureLevelsLoaded();
        return levelManager.revisionWingForStage(stageNumber);
    }

    public Optional<Level> getStageBossForStage(int stageNumber) {
        ensureLevelsLoaded();
        return levelManager.bossLevelForStage(stageNumber);
    }

    public boolean canPlayStageBoss(int stageNumber) {
        StageMilestoneReward gateReward = goldGateForStage(stageNumber);
        return gateReward != null && playerProfile.hasStageReward(gateReward);
    }

    public String stageBossRequirementLabel(int stageNumber) {
        StageMilestoneReward gateReward = goldGateForStage(stageNumber);
        return gateReward == null ? "" : "Requires " + gateReward.title();
    }

    public Level getDailyChallengeLevel() {
        ensureLevelsLoaded();
        return levelManager.dailyChallengeFor(LocalDate.now());
    }

    public String getDailyChallengeLabel() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"));
    }

    public void startRevisionWing(int stageNumber) {
        getRevisionWingForStage(stageNumber)
                .ifPresent(level -> startStandaloneLevel(level.getLevelNumber(), selectedGameMode));
    }

    public void startStageBoss(int stageNumber) {
        if (!canPlayStageBoss(stageNumber)) {
            return;
        }
        getStageBossForStage(stageNumber)
                .ifPresent(level -> startStandaloneLevel(level.getLevelNumber(), selectedGameMode));
    }

    public void startDailyChallenge() {
        startStandaloneLevel(getDailyChallengeLevel().getLevelNumber(), selectedGameMode);
    }

    public boolean isStandaloneSession() {
        return standaloneSession;
    }

    public boolean hasContinuableSave() {
        return loadSavedProgress()
                .filter(progress -> !progress.gameFinished())
                .isPresent();
    }

    public int getHighestSelectableLevel() {
        return ProgressPolicy.highestSelectableLevel(loadSavedProgress(), getFinalLevelNumber());
    }

    public void showGameLevel(Level level) {
        gameState.setCurrentLevel(level);
        setScene(new GameView(this, gameState).createView());
    }

    public void showLevelComplete() {
        updateProfileForCompletedLevel(gameState.getCurrentLevel());
        checkpointCompletedLevel(gameState.getCurrentLevel());
        setScene(new LevelCompleteView(this, gameState.getCurrentLevel()).createView());
    }

    public void goToNextLevel() {
        if (levelManager.hasNextLevel()) {
            Level nextLevel = levelManager.goToNextLevel();
            gameState.resetForLevel(nextLevel);
            showGameLevel(nextLevel);
            return;
        }

        gameState.finishGame();
        showGameFinished();
    }

    public void advanceAfterLevelComplete() {
        if (standaloneSession) {
            showMainMenu();
            return;
        }
        goToNextLevel();
    }

    public void showGameFinished() {
        setScene(new GameOverView(this).createView());
    }

    public void showBugFailure() {
        recoveryCandidateLevelNumber = gameState == null || gameState.getCurrentLevel() == null
                ? null
                : gameState.getCurrentLevel().getLevelNumber();
        recoveryCandidateEntryId = conceptIdForLevel(gameState == null ? null : gameState.getCurrentLevel()).orElse(null);
        recoveryPracticeCompleted = false;
        setScene(new BugFailureView(this, gameState == null ? null : gameState.getCurrentLevel()).createView());
    }

    public PlayerProgressProfile getPlayerProfile() {
        return playerProfile;
    }

    public LevelCompletionSummary getLastCompletionSummary() {
        return lastCompletionSummary;
    }

    public List<NotebookEntry> getUnlockedNotebookEntries() {
        return NotebookLibrary.allEntries().stream()
                .filter(entry -> playerProfile.unlockedNotebookEntries().contains(entry.id()))
                .toList();
    }

    public List<NotebookEntry> getNotebookEntriesForStage(int stageNumber) {
        java.util.Set<Integer> stageLevelNumbers = getAvailableLevels().stream()
                .filter(level -> level.getStageNumber() == stageNumber)
                .map(Level::getLevelNumber)
                .collect(java.util.stream.Collectors.toSet());
        return NotebookLibrary.allEntries().stream()
                .filter(entry -> stageLevelNumbers.contains(entry.unlockLevelNumber()))
                .toList();
    }

    public Optional<PracticePrompt> getPracticePromptForLevel(Level level) {
        return conceptIdForLevel(level)
                .map(playerProfile::practiceCompletionsFor)
                .map(practiceCount -> PracticeLibrary.forLevel(level, practiceCount))
                .orElseGet(() -> PracticeLibrary.forLevel(level));
    }

    public Optional<PracticePrompt> getPracticePromptForNotebookEntry(String entryId) {
        return PracticeLibrary.forNotebookEntry(entryId, playerProfile.practiceCompletionsFor(entryId));
    }

    public void showPracticeRoute(String entryId, Integer replayLevelNumber) {
        getPracticePromptForNotebookEntry(entryId)
                .ifPresent(prompt -> setScene(new PracticeFocusView(this, prompt, replayLevelNumber).createView()));
    }

    public List<ConceptProgressSnapshot> getConceptProgressSnapshots() {
        return getUnlockedNotebookEntries().stream()
                .map(entry -> new ConceptProgressSnapshot(
                        entry,
                        bestMedalForEntry(entry.id()),
                        playerProfile.hintUsageFor(entry.id()),
                        playerProfile.bugCountFor(entry.id()),
                        playerProfile.practiceCompletionsFor(entry.id()),
                        playerProfile.hasRecoveryStamp(entry.id())
                ))
                .toList();
    }

    public List<FocusRouteRecommendation> getFocusRouteRecommendations() {
        return buildFocusRouteRecommendations(getUnlockedNotebookEntries());
    }

    public List<FocusRouteRecommendation> getFocusRouteRecommendationsForStage(int stageNumber) {
        return buildFocusRouteRecommendations(getNotebookEntriesForStage(stageNumber).stream()
                .filter(entry -> playerProfile.unlockedNotebookEntries().contains(entry.id()))
                .toList());
    }

    public List<StageMilestoneReward> getUnlockedStageRewardsForStage(int stageNumber) {
        return StageMilestoneReward.forStage(stageNumber).stream()
                .filter(playerProfile::hasStageReward)
                .toList();
    }

    public void recordHintUsageForLevel(Level level) {
        conceptIdForLevel(level).ifPresent(entryId -> {
            playerProfile = playerProfile.withHintUsage(entryId);
            saveService.saveProfile(playerProfile);
        });
    }

    public void recordBugForLevel(Level level) {
        conceptIdForLevel(level).ifPresent(entryId -> {
            playerProfile = playerProfile.withBugCount(entryId);
            saveService.saveProfile(playerProfile);
        });
    }

    public void recordPracticeCompletion(PracticePrompt prompt, boolean countsTowardRecovery) {
        if (prompt == null || prompt.notebookEntryId() == null || prompt.notebookEntryId().isBlank()) {
            return;
        }

        playerProfile = playerProfile.withPracticeCompletion(prompt.notebookEntryId());
        saveService.saveProfile(playerProfile);
        if (countsTowardRecovery
                && recoveryCandidateLevelNumber != null
                && prompt.notebookEntryId().equals(recoveryCandidateEntryId)) {
            recoveryPracticeCompleted = true;
        }
    }

    public boolean isStageFinalLevel(Level level) {
        return level != null && isStageFinalLevelInternal(level);
    }

    public void showExitThankYouAndClose() {
        Label title = new Label("Thank you for playing!");
        title.getStyleClass().add("level-complete-title");

        Label message = new Label("Code Escape will close now.");
        message.getStyleClass().add("level-complete-message");

        VBox root = new VBox(20, title, message);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("level-complete-screen");
        setScene(root);

        System.out.println("Thank you for playing Code Escape!");
        PauseTransition closeDelay = new PauseTransition(Duration.seconds(3));
        closeDelay.setOnFinished(event -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
            Platform.exit();
        });
        closeDelay.play();
    }

    private void setScene(Parent root) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        String stylesheet = getClass().getResource("/styles/style.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        primaryStage.setScene(scene);
        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(true);
        });
    }

    private void startSelectableLevel(int levelNumber) {
        ensureLevelsLoaded();
        Optional<SavedProgress> savedProgress = loadSavedProgress();
        if (levelNumber < 1 || levelNumber > ProgressPolicy.highestSelectableLevel(savedProgress, getFinalLevelNumber())) {
            return;
        }

        if (savedProgress.isPresent()) {
            startSavedLevel(savedProgress.get(), levelNumber);
            return;
        }

        startLevel(levelNumber, selectedGameMode, ProgressRun.saveEnabled(Optional.empty()), 0, false, false);
    }

    private void startSavedLevel(SavedProgress progress, int levelNumber) {
        startLevel(
                levelNumber,
                progress.gameMode(),
                ProgressRun.saveEnabled(Optional.of(progress)),
                progress.bugCount(),
                progress.tutorialSeen(),
                false
        );
    }

    private void startFreshRun(GameMode gameMode, ProgressRun run) {
        clearRecoveryCandidate();
        startLevel(1, gameMode, run, 0, false, false);
    }

    private void startStandaloneLevel(int levelNumber, GameMode gameMode) {
        clearRecoveryCandidate();
        startLevel(levelNumber, gameMode, ProgressRun.unsaved(), 0, true, true);
    }

    private void startLevel(
            int levelNumber,
            GameMode gameMode,
            ProgressRun run,
            int bugCount,
            boolean tutorialSeen,
            boolean standalone
    ) {
        ensureLevelsLoaded();
        GameMode mode = normalizeGameMode(gameMode);
        Level level = levelManager.goToLevel(levelNumber);
        selectedGameMode = mode;
        progressRun = run == null ? ProgressRun.unsaved() : run;
        standaloneSession = standalone;
        gameState = new GameState();
        gameState.restoreCheckpoint(level, mode, bugCount, tutorialSeen);
        playerProfile = loadProfileWithEarnedStageRewards();
        if (recoveryCandidateLevelNumber != null && recoveryCandidateLevelNumber != levelNumber) {
            clearRecoveryCandidate();
        }
        showGameLevel(level);
    }

    private void checkpointCompletedLevel(Level completedLevel) {
        if (progressRun == null || !progressRun.isSaveEnabled() || completedLevel == null) {
            return;
        }

        int finalLevelNumber = getFinalLevelNumber();
        SavedProgress progress = progressRun.getProgressBase()
                .orElse(new SavedProgress(
                        gameState.getGameMode(),
                        completedLevel.getLevelNumber(),
                        completedLevel.getLevelNumber(),
                        gameState.getBugCount(),
                        gameState.hasSeenTutorial(),
                        false,
                        playerProfile
                ));
        SavedProgress progressWithProfile = new SavedProgress(
                progress.gameMode(),
                progress.currentLevelNumber(),
                progress.highestUnlockedLevel(),
                progress.bugCount(),
                progress.tutorialSeen(),
                progress.gameFinished(),
                playerProfile
        );
        progressRun = ProgressRun.saveEnabled(Optional.of(progressWithProfile));
        progressRun.checkpointAfterLevelCompletion(
                        gameState.getGameMode(),
                        completedLevel.getLevelNumber(),
                        finalLevelNumber,
                        gameState.getBugCount(),
                        gameState.hasSeenTutorial()
                )
                .ifPresent(updatedProgress -> saveService.save(updatedProgress, finalLevelNumber));
    }

    private Optional<SavedProgress> loadSavedProgress() {
        ensureLevelsLoaded();
        return saveService.load(getFinalLevelNumber());
    }

    private PlayerProgressProfile loadProfileWithEarnedStageRewards() {
        PlayerProgressProfile loadedProfile = saveService.loadProfile();
        PlayerProgressProfile updatedProfile = loadedProfile;
        for (int stageNumber = 1; stageNumber <= 5; stageNumber++) {
            for (StageMilestoneReward reward : newlyUnlockedStageRewards(updatedProfile, stageNumber)) {
                updatedProfile = updatedProfile.withStageReward(reward);
            }
        }
        if (!updatedProfile.equals(loadedProfile)) {
            saveService.saveProfile(updatedProfile);
        }
        return updatedProfile;
    }

    private int getFinalLevelNumber() {
        ensureLevelsLoaded();
        return levelManager.getLevels().stream()
                .mapToInt(Level::getLevelNumber)
                .max()
                .orElse(1);
    }

    private void ensureLevelsLoaded() {
        if (levelManager == null) {
            levelManager = new LevelManager();
        }
        if (levelManager.getLevels().isEmpty()) {
            levelManager.loadLevels();
        }
    }

    private boolean confirmOverwriteSave() {
        ButtonType overwriteButton = new ButtonType("Overwrite Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType unsavedButton = new ButtonType("Play Unsaved", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Saved Game Found");
        alert.setHeaderText("Overwrite saved progress?");
        alert.setContentText("The old save will stay available until this new run completes a level.");
        alert.getButtonTypes().setAll(overwriteButton, unsavedButton);
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }

        return alert.showAndWait()
                .filter(overwriteButton::equals)
                .isPresent();
    }

    private GameMode normalizeGameMode(GameMode gameMode) {
        return gameMode == null ? GameMode.NORMAL : gameMode;
    }

    private void updateProfileForCompletedLevel(Level completedLevel) {
        if (completedLevel == null) {
            return;
        }

        PlayerProgressProfile updatedProfile = playerProfile;
        List<AchievementId> newAchievements = new java.util.ArrayList<>();
        List<NotebookEntry> newRecoveryStamps = new java.util.ArrayList<>();
        List<StageMilestoneReward> newStageRewards = new java.util.ArrayList<>();

        MedalRank medalRank = determineMedal(completedLevel);
        updatedProfile = updatedProfile.withLevelMedal(completedLevel.getLevelNumber(), medalRank);

        if (!gameState.hadMistakeOnCurrentLevel() && !updatedProfile.hasAchievement(AchievementId.CLEAN_CODER)) {
            updatedProfile = updatedProfile.withAchievement(AchievementId.CLEAN_CODER);
            newAchievements.add(AchievementId.CLEAN_CODER);
        }
        if (gameState.getGameMode().isHard()
                && isStageFinalLevelInternal(completedLevel)
                && !updatedProfile.hasAchievement(AchievementId.HARD_MODE_FINISHER)) {
            updatedProfile = updatedProfile.withAchievement(AchievementId.HARD_MODE_FINISHER);
            newAchievements.add(AchievementId.HARD_MODE_FINISHER);
        }
        if (completedLevel.getRoom().hasHiddenHelper()
                && completedLevel.getRoom().isHelperFound()
                && !updatedProfile.hasAchievement(AchievementId.HELPER_SCOUT)) {
            updatedProfile = updatedProfile.withAchievement(AchievementId.HELPER_SCOUT);
            newAchievements.add(AchievementId.HELPER_SCOUT);
        }

        PlayerProgressProfile profileBeforeNotebookUnlocks = updatedProfile;
        List<NotebookEntry> newNotebookEntries = standaloneSession
                ? List.of()
                : NotebookLibrary.unlockedThroughLevel(completedLevel.getLevelNumber()).stream()
                        .filter(entry -> !profileBeforeNotebookUnlocks.unlockedNotebookEntries().contains(entry.id()))
                        .toList();
        updatedProfile = updatedProfile.withNotebookEntries(
                newNotebookEntries.stream().map(NotebookEntry::id).toList()
        );

        if (recoveryRewardEarned(completedLevel)) {
            String entryId = recoveryCandidateEntryId;
            if (!updatedProfile.hasRecoveryStamp(entryId)) {
                updatedProfile = updatedProfile.withRecoveryStamp(entryId);
                NotebookLibrary.find(entryId).ifPresent(newRecoveryStamps::add);
            }
        }
        if (recoveryCandidateLevelNumber != null && recoveryCandidateLevelNumber == completedLevel.getLevelNumber()) {
            clearRecoveryCandidate();
        }

        for (StageMilestoneReward reward : newlyUnlockedStageRewards(updatedProfile, completedLevel.getStageNumber())) {
            updatedProfile = updatedProfile.withStageReward(reward);
            newStageRewards.add(reward);
        }

        StageMilestoneReward bossReward = bossRewardForLevel(completedLevel);
        if (bossReward != null && !updatedProfile.hasStageReward(bossReward)) {
            updatedProfile = updatedProfile.withStageReward(bossReward);
            newStageRewards.add(bossReward);
        }
        if (bossReward != null && !updatedProfile.hasAchievement(AchievementId.BOSS_BREAKER)) {
            updatedProfile = updatedProfile.withAchievement(AchievementId.BOSS_BREAKER);
            newAchievements.add(AchievementId.BOSS_BREAKER);
        }

        playerProfile = updatedProfile;
        saveService.saveProfile(playerProfile);
        lastCompletionSummary = new LevelCompletionSummary(medalRank, newAchievements, newNotebookEntries, newRecoveryStamps, newStageRewards);
    }

    private MedalRank determineMedal(Level completedLevel) {
        if (gameState.getGameMode().isHard() && !gameState.hadMistakeOnCurrentLevel()) {
            return MedalRank.GOLD;
        }
        if (!gameState.hadMistakeOnCurrentLevel()) {
            return MedalRank.SILVER;
        }
        return MedalRank.BRONZE;
    }

    private boolean isStageFinalLevelInternal(Level completedLevel) {
        return getAvailableLevels().stream()
                .filter(level -> level.getStageNumber() == completedLevel.getStageNumber())
                .mapToInt(Level::getStageLevelNumber)
                .max()
                .orElse(completedLevel.getStageLevelNumber()) == completedLevel.getStageLevelNumber();
    }

    private MedalRank bestMedalForEntry(String entryId) {
        return getAvailableLevels().stream()
                .filter(level -> conceptIdForLevel(level).filter(entryId::equals).isPresent())
                .map(level -> playerProfile.levelMedals().get(level.getLevelNumber()))
                .filter(java.util.Objects::nonNull)
                .max(java.util.Comparator.comparingInt(Enum::ordinal))
                .orElse(null);
    }

    private Optional<String> conceptIdForLevel(Level level) {
        return NotebookLibrary.entryForLevel(level)
                .or(() -> NotebookLibrary.recommendedForLevel(level))
                .map(NotebookEntry::id);
    }

    private boolean recoveryRewardEarned(Level completedLevel) {
        return completedLevel != null
                && recoveryCandidateLevelNumber != null
                && recoveryCandidateLevelNumber == completedLevel.getLevelNumber()
                && recoveryCandidateEntryId != null
                && recoveryPracticeCompleted
                && !gameState.hadMistakeOnCurrentLevel();
    }

    private void clearRecoveryCandidate() {
        recoveryCandidateLevelNumber = null;
        recoveryCandidateEntryId = null;
        recoveryPracticeCompleted = false;
    }

    private List<FocusRouteRecommendation> buildFocusRouteRecommendations(List<NotebookEntry> entries) {
        if (entries.isEmpty()) {
            return List.of();
        }

        List<FocusRouteRecommendation> recommendations = new java.util.ArrayList<>();
        FocusRouteRecommendation practice = buildPracticeRecommendation(entries);
        if (practice != null) {
            recommendations.add(practice);
        }

        FocusRouteRecommendation replay = buildReplayRecommendation(entries);
        if (replay != null && recommendations.stream().noneMatch(existing -> existing.notebookEntryId().equals(replay.notebookEntryId())
                && existing.kind() == replay.kind())) {
            recommendations.add(replay);
        }

        FocusRouteRecommendation push = buildPushRecommendation(entries);
        if (push != null) {
            recommendations.add(push);
        }

        return recommendations.stream()
                .sorted(Comparator.comparingInt(FocusRouteRecommendation::priorityScore).reversed())
                .limit(3)
                .toList();
    }

    private FocusRouteRecommendation buildPracticeRecommendation(List<NotebookEntry> entries) {
        return entries.stream()
                .map(entry -> {
                    ConceptProgressSnapshot snapshot = snapshotFor(entry.id()).orElse(null);
                    if (snapshot == null) {
                        return null;
                    }
                    int score = conceptPriority(snapshot, true);
                    if (score <= 0) {
                        return null;
                    }
                    int replayLevel = replayLevelForEntry(entry.id()).map(Level::getLevelNumber).orElse(0);
                    String detail = "Practice " + entry.title() + " next. Hints: " + snapshot.hintUsage()
                            + ", bugs: " + snapshot.bugCount()
                            + ", practice clears: " + snapshot.practiceCompletions() + ".";
                    return new FocusRouteRecommendation(
                            FocusRouteKind.PRACTICE,
                            "Focus Route: " + entry.title(),
                            detail,
                            "Start Drill",
                            entry.id(),
                            replayLevel == 0 ? null : replayLevel,
                            stageForEntry(entry.id()).orElse(1),
                            score
                    );
                })
                .filter(java.util.Objects::nonNull)
                .max(Comparator.comparingInt(FocusRouteRecommendation::priorityScore))
                .orElse(null);
    }

    private FocusRouteRecommendation buildReplayRecommendation(List<NotebookEntry> entries) {
        return entries.stream()
                .map(entry -> replayLevelForEntry(entry.id())
                        .map(level -> {
                            ConceptProgressSnapshot snapshot = snapshotFor(entry.id()).orElse(null);
                            int score = snapshot == null ? 0 : conceptPriority(snapshot, false);
                            if (score <= 0) {
                                return null;
                            }
                            return new FocusRouteRecommendation(
                                    FocusRouteKind.REPLAY,
                                    "Replay " + level.getDisplayId(),
                                    level.getName() + " is the cleanest place to rebuild " + entry.title() + ".",
                                    "Replay " + level.getDisplayId(),
                                    entry.id(),
                                    level.getLevelNumber(),
                                    level.getStageNumber(),
                                    score - 1
                            );
                        })
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .max(Comparator.comparingInt(FocusRouteRecommendation::priorityScore))
                .orElse(null);
    }

    private FocusRouteRecommendation buildPushRecommendation(List<NotebookEntry> entries) {
        int highestSelectableLevel = getHighestSelectableLevel();
        java.util.Set<String> allowedEntryIds = entries.stream()
                .map(NotebookEntry::id)
                .collect(java.util.stream.Collectors.toSet());
        return getAvailableLevels().stream()
                .filter(level -> level.getLevelNumber() <= highestSelectableLevel)
                .filter(level -> !playerProfile.levelMedals().containsKey(level.getLevelNumber()))
                .filter(level -> conceptIdForLevel(level).filter(allowedEntryIds::contains).isPresent())
                .min(Comparator.comparingInt(Level::getLevelNumber))
                .flatMap(level -> conceptIdForLevel(level).map(entryId -> {
                    String title = NotebookLibrary.find(entryId).map(NotebookEntry::title).orElse(level.getConcept());
                    return new FocusRouteRecommendation(
                            FocusRouteKind.PUSH,
                            "Push Into " + level.getDisplayId(),
                            "Advance the route with " + title + " in " + level.getName() + ".",
                            "Play " + level.getDisplayId(),
                            entryId,
                            level.getLevelNumber(),
                            level.getStageNumber(),
                            1
                    );
                }))
                .orElse(null);
    }

    private Optional<ConceptProgressSnapshot> snapshotFor(String entryId) {
        return getConceptProgressSnapshots().stream()
                .filter(snapshot -> snapshot.entry().id().equals(entryId))
                .findFirst();
    }

    private Optional<Level> replayLevelForEntry(String entryId) {
        return getAvailableLevels().stream()
                .filter(level -> conceptIdForLevel(level).filter(entryId::equals).isPresent())
                .sorted(Comparator
                        .comparing((Level level) -> playerProfile.levelMedals().containsKey(level.getLevelNumber()))
                        .thenComparing(level -> {
                            MedalRank medal = playerProfile.levelMedals().get(level.getLevelNumber());
                            return medal == null ? -1 : medal.ordinal();
                        })
                        .thenComparingInt(Level::getLevelNumber))
                .findFirst();
    }

    private Optional<Integer> stageForEntry(String entryId) {
        return getAvailableLevels().stream()
                .filter(level -> conceptIdForLevel(level).filter(entryId::equals).isPresent())
                .map(Level::getStageNumber)
                .findFirst();
    }

    private int conceptPriority(ConceptProgressSnapshot snapshot, boolean practiceBias) {
        MedalRank bestMedal = snapshot.bestMedal();
        int medalPenalty = bestMedal == null
                ? 5
                : switch (bestMedal) {
                    case GOLD -> 0;
                    case SILVER -> 2;
                    case BRONZE -> 4;
                };
        int score = snapshot.bugCount() * 5
                + snapshot.hintUsage() * 4
                + medalPenalty
                + (snapshot.practiceCompletions() == 0 ? 5 : 0)
                + (!snapshot.recoveryStamp() && snapshot.bugCount() > 0 ? 2 : 0);
        if (practiceBias) {
            score += snapshot.practiceCompletions() == 0 ? 3 : 0;
        }
        return score;
    }

    private List<StageMilestoneReward> newlyUnlockedStageRewards(PlayerProgressProfile profile, int stageNumber) {
        List<Level> stageLevels = getAvailableLevels().stream()
                .filter(level -> level.getStageNumber() == stageNumber)
                .toList();
        if (stageLevels.isEmpty()) {
            return List.of();
        }

        boolean allCleared = stageLevels.stream()
                .allMatch(level -> profile.levelMedals().containsKey(level.getLevelNumber()));
        boolean allGold = stageLevels.stream()
                .allMatch(level -> profile.levelMedals().get(level.getLevelNumber()) == MedalRank.GOLD);

        return StageMilestoneReward.forStage(stageNumber).stream()
                .filter(reward -> !profile.hasStageReward(reward))
                .filter(reward -> !reward.bossReward())
                .filter(reward -> reward.allGoldRequired() ? allGold : allCleared)
                .toList();
    }

    private StageMilestoneReward goldGateForStage(int stageNumber) {
        return switch (stageNumber) {
            case 1 -> StageMilestoneReward.STAGE_1_GOLD;
            case 2 -> StageMilestoneReward.STAGE_2_GOLD;
            case 3 -> StageMilestoneReward.STAGE_3_GOLD;
            case 4 -> StageMilestoneReward.STAGE_4_GOLD;
            case 5 -> StageMilestoneReward.STAGE_5_GOLD;
            default -> null;
        };
    }

    private StageMilestoneReward bossRewardForLevel(Level level) {
        if (level == null) {
            return null;
        }

        return switch (level.getDisplayId()) {
            case "BOSS-1" -> StageMilestoneReward.STAGE_1_BOSS;
            case "BOSS-2" -> StageMilestoneReward.STAGE_2_BOSS;
            case "BOSS-3" -> StageMilestoneReward.STAGE_3_BOSS;
            case "BOSS-4" -> StageMilestoneReward.STAGE_4_BOSS;
            case "BOSS-5" -> StageMilestoneReward.STAGE_5_BOSS;
            default -> null;
        };
    }
}

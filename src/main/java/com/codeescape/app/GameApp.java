package com.codeescape.app;

import com.codeescape.engine.GameState;
import com.codeescape.engine.LevelManager;
import com.codeescape.engine.LevelCompletionSummary;
import com.codeescape.engine.MedalRank;
import com.codeescape.engine.NotebookEntry;
import com.codeescape.engine.NotebookLibrary;
import com.codeescape.engine.PlayerProgressProfile;
import com.codeescape.engine.AchievementId;
import com.codeescape.engine.ProgressPolicy;
import com.codeescape.engine.ProgressRun;
import com.codeescape.engine.ProgressSaveService;
import com.codeescape.engine.SavedProgress;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
import com.codeescape.ui.AdminView;
import com.codeescape.ui.GameOverView;
import com.codeescape.ui.GameView;
import com.codeescape.ui.LevelCompleteView;
import com.codeescape.ui.MainMenuView;
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
    private LevelCompletionSummary lastCompletionSummary = new LevelCompletionSummary(MedalRank.BRONZE, List.of(), List.of());

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
        playerProfile = saveService.loadProfile();
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

    public void restartAfterBugFailure() {
        GameMode mode = gameState == null ? selectedGameMode : gameState.getGameMode();
        startFreshRun(mode, progressRun == null ? ProgressRun.unsaved() : progressRun);
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

        startLevel(levelNumber, selectedGameMode, ProgressRun.unsaved(), 0, true);
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

    public void showGameFinished() {
        setScene(new GameOverView(this).createView());
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

        startLevel(levelNumber, selectedGameMode, ProgressRun.saveEnabled(Optional.empty()), 0, false);
    }

    private void startSavedLevel(SavedProgress progress, int levelNumber) {
        startLevel(
                levelNumber,
                progress.gameMode(),
                ProgressRun.saveEnabled(Optional.of(progress)),
                progress.bugCount(),
                progress.tutorialSeen()
        );
    }

    private void startFreshRun(GameMode gameMode, ProgressRun run) {
        startLevel(1, gameMode, run, 0, false);
    }

    private void startLevel(
            int levelNumber,
            GameMode gameMode,
            ProgressRun run,
            int bugCount,
            boolean tutorialSeen
    ) {
        ensureLevelsLoaded();
        GameMode mode = normalizeGameMode(gameMode);
        Level level = levelManager.goToLevel(levelNumber);
        selectedGameMode = mode;
        progressRun = run == null ? ProgressRun.unsaved() : run;
        gameState = new GameState();
        gameState.restoreCheckpoint(level, mode, bugCount, tutorialSeen);
        playerProfile = run != null && run.getProgressBase().isPresent()
                ? run.getProgressBase().get().profile()
                : saveService.loadProfile();
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

        MedalRank medalRank = determineMedal(completedLevel);
        updatedProfile = updatedProfile.withLevelMedal(completedLevel.getLevelNumber(), medalRank);

        if (!gameState.hadMistakeOnCurrentLevel() && !updatedProfile.hasAchievement(AchievementId.CLEAN_CODER)) {
            updatedProfile = updatedProfile.withAchievement(AchievementId.CLEAN_CODER);
            newAchievements.add(AchievementId.CLEAN_CODER);
        }
        if (gameState.getGameMode().isHard()
                && isStageFinalLevel(completedLevel)
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
        List<NotebookEntry> newNotebookEntries = NotebookLibrary.unlockedThroughLevel(completedLevel.getLevelNumber()).stream()
                .filter(entry -> !profileBeforeNotebookUnlocks.unlockedNotebookEntries().contains(entry.id()))
                .toList();
        updatedProfile = updatedProfile.withNotebookEntries(
                newNotebookEntries.stream().map(NotebookEntry::id).toList()
        );

        playerProfile = updatedProfile;
        saveService.saveProfile(playerProfile);
        lastCompletionSummary = new LevelCompletionSummary(medalRank, newAchievements, newNotebookEntries);
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

    private boolean isStageFinalLevel(Level completedLevel) {
        return getAvailableLevels().stream()
                .filter(level -> level.getStageNumber() == completedLevel.getStageNumber())
                .mapToInt(Level::getStageLevelNumber)
                .max()
                .orElse(completedLevel.getStageLevelNumber()) == completedLevel.getStageLevelNumber();
    }
}

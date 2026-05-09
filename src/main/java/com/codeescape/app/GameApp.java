package com.codeescape.app;

import com.codeescape.engine.GameState;
import com.codeescape.engine.LevelManager;
import com.codeescape.engine.ProgressPolicy;
import com.codeescape.engine.ProgressRun;
import com.codeescape.engine.ProgressSaveService;
import com.codeescape.engine.SavedProgress;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
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
        setScene(new MainMenuView(this).createView());
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
        showGameLevel(level);
    }

    private void checkpointCompletedLevel(Level completedLevel) {
        if (progressRun == null || !progressRun.isSaveEnabled() || completedLevel == null) {
            return;
        }

        int finalLevelNumber = getFinalLevelNumber();
        progressRun.checkpointAfterLevelCompletion(
                        gameState.getGameMode(),
                        completedLevel.getLevelNumber(),
                        finalLevelNumber,
                        gameState.getBugCount(),
                        gameState.hasSeenTutorial()
                )
                .ifPresent(progress -> saveService.save(progress, finalLevelNumber));
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
}

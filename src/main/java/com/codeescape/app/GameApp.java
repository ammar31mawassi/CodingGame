package com.codeescape.app;

import com.codeescape.engine.GameState;
import com.codeescape.engine.LevelManager;
import com.codeescape.model.GameMode;
import com.codeescape.model.Level;
import com.codeescape.ui.GameOverView;
import com.codeescape.ui.GameView;
import com.codeescape.ui.LevelCompleteView;
import com.codeescape.ui.MainMenuView;
import com.codeescape.util.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import java.util.List;

public class GameApp extends Application {
    private Stage primaryStage;
    private GameState gameState;
    private LevelManager levelManager;
    private GameMode selectedGameMode = GameMode.NORMAL;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle(Constants.APP_TITLE);
        primaryStage.setResizable(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        levelManager = new LevelManager();
        levelManager.loadLevels();
        showMainMenu();
        primaryStage.setMaximized(true);
        primaryStage.show();
        Platform.runLater(() -> primaryStage.setFullScreen(true));
    }

    public void showMainMenu() {
        setScene(new MainMenuView(this).createView());
    }

    public void startNewGame() {
        startNewGame(selectedGameMode);
    }

    public void startNewGame(GameMode gameMode) {
        selectedGameMode = gameMode == null ? GameMode.NORMAL : gameMode;
        levelManager = new LevelManager();
        levelManager.loadLevels();
        gameState = new GameState();
        gameState.setGameMode(selectedGameMode);
        gameState.resetForLevel(levelManager.getCurrentLevel());
        showGameLevel(levelManager.getCurrentLevel());
    }

    public void startAtLevel(int levelNumber) {
        startAtLevel(levelNumber, selectedGameMode);
    }

    public void startAtLevel(int levelNumber, GameMode gameMode) {
        selectedGameMode = gameMode == null ? GameMode.NORMAL : gameMode;
        levelManager = new LevelManager();
        levelManager.loadLevels();
        Level level = levelManager.goToLevel(levelNumber);
        gameState = new GameState();
        gameState.setGameMode(selectedGameMode);
        gameState.resetForLevel(level);
        showGameLevel(level);
    }

    public GameMode getSelectedGameMode() {
        return selectedGameMode;
    }

    public void setSelectedGameMode(GameMode selectedGameMode) {
        this.selectedGameMode = selectedGameMode == null ? GameMode.NORMAL : selectedGameMode;
    }

    public List<Level> getAvailableLevels() {
        if (levelManager == null) {
            levelManager = new LevelManager();
            levelManager.loadLevels();
        }
        return levelManager.getLevels();
    }

    public void showGameLevel(Level level) {
        gameState.setCurrentLevel(level);
        setScene(new GameView(this, gameState).createView());
    }

    public void showLevelComplete() {
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
}

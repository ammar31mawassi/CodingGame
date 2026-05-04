package com.codeescape.app;

import com.codeescape.engine.GameState;
import com.codeescape.engine.LevelManager;
import com.codeescape.model.Level;
import com.codeescape.ui.GameOverView;
import com.codeescape.ui.GameView;
import com.codeescape.ui.LevelCompleteView;
import com.codeescape.ui.MainMenuView;
import com.codeescape.util.Constants;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApp extends Application {
    private Stage primaryStage;
    private GameState gameState;
    private LevelManager levelManager;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle(Constants.APP_TITLE);
        levelManager = new LevelManager();
        levelManager.loadLevels();
        showMainMenu();
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public void showMainMenu() {
        setScene(new MainMenuView(this).createView());
    }

    public void startNewGame() {
        levelManager = new LevelManager();
        levelManager.loadLevels();
        gameState = new GameState();
        gameState.resetForLevel(levelManager.getCurrentLevel());
        showGameLevel(levelManager.getCurrentLevel());
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
        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        String stylesheet = getClass().getResource("/styles/style.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        primaryStage.setScene(scene);
    }
}

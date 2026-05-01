package com.codeescape.engine;

import com.codeescape.model.Inventory;
import com.codeescape.model.Level;
import com.codeescape.model.Player;
import com.codeescape.util.Constants;

public class GameState {
    private Player player;
    private Inventory inventory;
    private Level currentLevel;
    private boolean gameFinished;

    public GameState() {
        player = createDefaultPlayer();
        inventory = new Inventory();
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Level level) {
        currentLevel = level;
    }

    public void resetForLevel(Level level) {
        currentLevel = level;
        player = createDefaultPlayer();
        inventory = new Inventory();
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void finishGame() {
        gameFinished = true;
    }

    private Player createDefaultPlayer() {
        return new Player(
                Constants.PLAYER_START_X,
                Constants.PLAYER_START_Y,
                Constants.PLAYER_SIZE,
                Constants.PLAYER_SIZE,
                Constants.PLAYER_SPEED
        );
    }
}

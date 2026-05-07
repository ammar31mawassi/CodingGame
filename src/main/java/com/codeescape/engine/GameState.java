package com.codeescape.engine;

import com.codeescape.model.Inventory;
import com.codeescape.model.Level;
import com.codeescape.model.Player;
import com.codeescape.util.Constants;
import com.codeescape.validation.VariableDeclarationValidator;

public class GameState {
    private Player player;
    private Inventory inventory;
    private Level currentLevel;
    private int bugCount;
    private boolean currentLevelHadMistake;
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
        currentLevelHadMistake = false;
        VariableDeclarationValidator.getInstance();
        VariableDeclarationValidator.resetVariables();
    }

    public int getBugCount() {
        return bugCount;
    }

    public void addBug() {
        currentLevelHadMistake = true;
        bugCount = Math.min(3, bugCount + 1);
    }

    public boolean hasTooManyBugs() {
        return bugCount >= 3;
    }

    public void rewardCleanLevel() {
        if (!currentLevelHadMistake && bugCount > 0) {
            bugCount--;
        }
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
                Constants.PLAYER_WIDTH,
                Constants.PLAYER_HEIGHT,
                Constants.PLAYER_SPEED
        );
    }
}

package com.codeescape.engine;

import com.codeescape.model.Inventory;
import com.codeescape.model.Level;
import com.codeescape.model.GameMode;
import com.codeescape.model.Player;
import com.codeescape.util.Constants;
import com.codeescape.validation.VariableDeclarationValidator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameState {
    private Player player;
    private Inventory inventory;
    private Level currentLevel;
    private GameMode gameMode = GameMode.NORMAL;
    private int bugCount;
    private boolean currentLevelHadMistake;
    private boolean gameFinished;
    private boolean tutorialSeen;
    private final Map<Integer, Integer> revealedHintCounts = new HashMap<>();

    public GameState() {
        player = createPlayerAtGridCellZero();
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

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode == null ? GameMode.NORMAL : gameMode;
    }

    public void setCurrentLevel(Level level) {
        currentLevel = level;
    }

    public void resetForLevel(Level level) {
        currentLevel = level;
        player = createPlayerForLevel(level);
        inventory = new Inventory();
        currentLevelHadMistake = false;
        gameFinished = false;
        VariableDeclarationValidator.getInstance();
        VariableDeclarationValidator.resetVariables();
    }

    public void restoreCheckpoint(Level level, GameMode gameMode, int bugCount, boolean tutorialSeen) {
        setGameMode(gameMode);
        resetForLevel(level);
        this.bugCount = Math.max(0, Math.min(bugCount, 3));
        this.tutorialSeen = tutorialSeen;
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

    public boolean hadMistakeOnCurrentLevel() {
        return currentLevelHadMistake;
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

    public boolean hasSeenTutorial() {
        return tutorialSeen;
    }

    public void markTutorialSeen() {
        tutorialSeen = true;
    }

    public Optional<String> revealNextHint() {
        if (currentLevel == null) {
            return Optional.empty();
        }

        int levelNumber = currentLevel.getLevelNumber();
        java.util.List<String> hints = HintLibrary.hintsFor(currentLevel);
        int nextIndex = revealedHintCounts.getOrDefault(levelNumber, 0);
        if (nextIndex >= hints.size()) {
            return Optional.empty();
        }

        revealedHintCounts.put(levelNumber, nextIndex + 1);
        return Optional.of(hints.get(nextIndex));
    }

    public int revealedHintCountForCurrentLevel() {
        if (currentLevel == null) {
            return 0;
        }
        return revealedHintCounts.getOrDefault(currentLevel.getLevelNumber(), 0);
    }

    private Player createPlayerForLevel(Level level) {
        return createPlayerAtGridCellZero();
    }

    private Player createPlayerAtGridCellZero() {
        return createPlayer(
                RoomLayoutBuilder.GRID_ORIGIN_X + (RoomLayoutBuilder.GRID_CELL_WIDTH - Constants.PLAYER_WIDTH) / 2.0,
                RoomLayoutBuilder.GRID_ORIGIN_Y + (RoomLayoutBuilder.GRID_CELL_HEIGHT - Constants.PLAYER_HEIGHT) / 2.0
        );
    }

    private Player createPlayer(double x, double y) {
        return new Player(
                x,
                y,
                Constants.PLAYER_WIDTH,
                Constants.PLAYER_HEIGHT,
                Constants.PLAYER_SPEED
        );
    }
}

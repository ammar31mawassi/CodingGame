package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.Door;
import com.codeescape.model.Level;
import com.codeescape.model.Player;
import com.codeescape.model.ProgrammableObject;
import com.codeescape.model.Token;
import com.codeescape.model.TokenType;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelManagerTest {
    @Test
    void loadsStagedOrderedLevels() {
        LevelManager levelManager = new LevelManager();

        levelManager.loadLevels();

        assertEquals(24, levelManager.getLevels().size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24), levelManager.getLevels().stream()
                .map(level -> level.getLevelNumber())
                .toList());
        assertEquals("1-1", levelManager.getLevels().get(0).getDisplayId());
        assertEquals("5-4", levelManager.getLevels().get(23).getDisplayId());
    }

    @Test
    void exposesStandaloneRevisionBossAndDailyRoutesWithoutChangingCampaignCount() {
        LevelManager levelManager = new LevelManager();

        levelManager.loadLevels();

        assertEquals(24, levelManager.getLevels().size());
        assertEquals(List.of("REV-1", "REV-2", "REV-3", "REV-4"), levelManager.getRevisionWingLevels().stream()
                .map(Level::getDisplayId)
                .toList());
        assertEquals(List.of("BOSS-1", "BOSS-2", "BOSS-3", "BOSS-4", "BOSS-5"), levelManager.getStageBossLevels().stream()
                .map(Level::getDisplayId)
                .toList());
        Level daily = levelManager.dailyChallengeFor(LocalDate.of(2026, 5, 17));
        assertEquals(12, daily.getLevelNumber());
        assertEquals("3-4", daily.getDisplayId());
    }

    @Test
    void replayingLevelCreatesFreshUnsolvedState() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level firstPlay = levelManager.goToLevel(1);
        Token token = firstPlay.getRoom().getTokens().get(0);
        token.collect();
        firstPlay.getRoom().getDoor().unlock();
        firstPlay.complete();

        Level replay = levelManager.goToLevel(1);

        assertNotSame(firstPlay, replay);
        assertFalse(replay.isCompleted());
        assertTrue(replay.getRoom().getDoor().isLocked());
        assertFalse(replay.getRoom().getTokens().get(0).isCollected());
    }

    @Test
    void catalogLevelsDoNotShareChestState() {
        LevelManager levelManager = new LevelManager();
        levelManager.loadLevels();

        Level firstCatalogLevel = levelManager.getLevel(5);
        Chest chest = firstCatalogLevel.getRoom().getChests().get(0);
        chest.open();

        Level secondCatalogLevel = levelManager.getLevel(5);

        assertNotSame(firstCatalogLevel, secondCatalogLevel);
        assertFalse(secondCatalogLevel.getRoom().getChests().get(0).isOpened());
    }

    @Test
    void savedLayoutOverrideChangesPlayableLevel(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        int[][] cells = new int[RoomLayoutBuilder.GRID_ROWS][RoomLayoutBuilder.GRID_COLUMNS];
        cells[0][0] = GridRoomBuilder.TOP;
        store.save(new LevelLayoutOverride(
                1,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                cells,
                List.of(
                        new LevelLayoutOverride.ChestPlacement(2, 0, false),
                        new LevelLayoutOverride.ChestPlacement(3, 0, false)
                ),
                List.of()
        ));

        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        Level editedLevel = levelManager.getLevel(1);

        assertEquals(1, editedLevel.getRoom().getWalls().size());
        assertEquals(2, editedLevel.getRoom().getChests().size());
    }

    @Test
    void savedContentOverrideChangesTokensGoalHelperAndAcceptedAnswers(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        int[][] cells = new int[RoomLayoutBuilder.GRID_ROWS][RoomLayoutBuilder.GRID_COLUMNS];
        store.save(new LevelLayoutOverride(
                1,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                cells,
                List.of(new LevelLayoutOverride.ChestPlacement(2, 0, false)),
                List.of(),
                "Edited Goal",
                "Build either edited answer.",
                "Edited helper",
                List.of("int x = 7;", "int y = 8;"),
                true,
                List.of(
                        new LevelLayoutOverride.TokenPlacement(
                                "int",
                                TokenType.CODE,
                                LevelLayoutOverride.TokenPlacementKind.VISIBLE,
                                1,
                                1,
                                0
                        ),
                        new LevelLayoutOverride.TokenPlacement(
                                "Goal",
                                TokenType.GOAL,
                                LevelLayoutOverride.TokenPlacementKind.CHEST,
                                0,
                                0,
                                0
                        )
                )
        ));

        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        Level editedLevel = levelManager.getLevel(1);

        assertEquals("Edited Goal", editedLevel.getRoom().getPuzzle().getTitle());
        assertEquals("Build either edited answer.", editedLevel.getRoom().getPuzzle().getInstructions());
        assertEquals("Edited helper", editedLevel.getGoalHelper());
        assertEquals(List.of("int"), editedLevel.getRoom().getTokens().stream().map(Token::getValue).toList());
        assertEquals(1, editedLevel.getRoom().getChestRewards().size());
        assertTrue(editedLevel.getRoom().getPuzzle().checkAnswer("int y=8;").isValid());
        assertFalse(editedLevel.getRoom().getPuzzle().checkAnswer("int x = 5;").isValid());
    }

    @Test
    void campaignAndCustomChallengeLevelsExposeMedalContracts(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        store.save(new LevelLayoutOverride(
                1,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                new int[RoomLayoutBuilder.GRID_ROWS][RoomLayoutBuilder.GRID_COLUMNS],
                List.of(),
                List.of()
        ));

        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        Level campaignLevel = levelManager.getLevel(1);
        Level customLevel = levelManager.getCustomChallengeLevels().get(0);

        assertNotNull(campaignLevel.getMedalContract());
        assertNotNull(customLevel.getMedalContract());
        assertEquals(campaignLevel.getMedalContract().title(), customLevel.getMedalContract().title());
    }

    @Test
    void medalContractsMatchAvailableRoomMechanics() {
        LevelManager levelManager = new LevelManager(LevelLayoutOverrideStore.disabled());
        levelManager.loadLevels();

        List<Level> allLevels = new ArrayList<>();
        allLevels.addAll(levelManager.getLevels());
        allLevels.addAll(levelManager.getRevisionWingLevels());
        allLevels.addAll(levelManager.getStageBossLevels());

        for (Level level : allLevels) {
            assertContractMatchesRoom(level);
        }
    }

    @Test
    void savedQuestionDoorOverrideChangesVariableThenIfLevel(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        store.save(new LevelLayoutOverride(
                5,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                new int[][]{
                        {9, 1, 1, 1, 1, 3, 5, 5, 5, 3, 1, 3},
                        {8, 6, 4, 6, 0, 2, 4, 2, 0, 6, 64, 6},
                        {8, 2, 0, 2, 0, 2, 0, 6, 4, 2, 4, 2},
                        {8, 2, 0, 6, 4, 2, 4, 2, 0, 6, 0, 2},
                        {8, 2, 0, 2, 0, 0, 0, 6, 4, 4, 2, 2},
                        {12, 4, 4, 6, 4, 4, 4, 4, 4, 4, 6, 6}
                },
                List.of(new LevelLayoutOverride.ChestPlacement(0, 1, false)),
                List.of(new LevelLayoutOverride.QuestionDoorPlacement(
                        10,
                        1,
                        GridRoomBuilder.Side.BOTTOM,
                        "Which type of variables can directly be used in an if-statement?",
                        "____ flag = true; if (flag) {}",
                        List.of("boolean", "int", "class", "String"),
                        "boolean",
                        "true"
                )),
                null,
                null,
                null,
                List.of(),
                true,
                List.of(new LevelLayoutOverride.TokenPlacement(
                        "Goal",
                        TokenType.GOAL,
                        LevelLayoutOverride.TokenPlacementKind.CHEST,
                        0,
                        0,
                        0
                ))
        ));

        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();

        Level editedLevel = levelManager.getLevel(5);

        assertTrue(editedLevel.getRoom().hasChallengeDoor());
        assertNotNull(editedLevel.getRoom().getChallengeDoor());
        assertEquals(
                RoomLayoutBuilder.GRID_ORIGIN_X + RoomLayoutBuilder.GRID_COLUMNS * RoomLayoutBuilder.GRID_CELL_WIDTH - 52,
                editedLevel.getRoom().getDoor().getX(),
                0.001
        );
        assertEquals("boolean", editedLevel.getRoom().getChallengeQuestion().getCorrectAnswer());
        assertEquals("true", editedLevel.getRoom().getChallengeQuestion().getReward().getValue());
        assertEquals(MedalContractType.SOLVE_QUESTION_DOOR, editedLevel.getMedalContract().type());
    }

    @Test
    void builtInVisibleRoomObjectsDoNotOverlap() {
        LevelManager levelManager = new LevelManager(LevelLayoutOverrideStore.disabled());
        levelManager.loadLevels();

        for (Level level : levelManager.getLevels()) {
            List<Rect> rects = new ArrayList<>();
            level.getRoom().getTokens().forEach(token -> rects.add(rect(level, "token " + token.getValue(), token)));
            level.getRoom().getChests().forEach(chest -> rects.add(rect(level, "chest", chest)));
            level.getRoom().getProgrammableObjects().forEach(object -> rects.add(rect(level, "object " + object.getDisplayName(), object)));
            rects.add(rect(level, "exit door", level.getRoom().getDoor()));
            if (level.getRoom().getChallengeDoor() != null) {
                rects.add(rect(level, "challenge door", level.getRoom().getChallengeDoor()));
            }

            for (int i = 0; i < rects.size(); i++) {
                for (int j = i + 1; j < rects.size(); j++) {
                    Rect first = rects.get(i);
                    Rect second = rects.get(j);
                    assertFalse(first.intersects(second), () -> first.name() + " overlaps " + second.name());
                }
            }
        }
    }

    @Test
    void builtInExitDoorsSitOnGridRightEdge() {
        LevelManager levelManager = new LevelManager(LevelLayoutOverrideStore.disabled());
        levelManager.loadLevels();

        double gridExitX = RoomLayoutBuilder.GRID_ORIGIN_X
                + RoomLayoutBuilder.GRID_COLUMNS * RoomLayoutBuilder.GRID_CELL_WIDTH
                - 52;

        for (Level level : levelManager.getLevels()) {
            assertEquals(gridExitX, level.getRoom().getDoor().getX(), 0.001, level.getDisplayId());
        }
    }

    @Test
    void visibleOverrideTokensDoNotOccupySpawnCell(@TempDir Path tempDir) {
        LevelLayoutOverrideStore store = new LevelLayoutOverrideStore(tempDir);
        store.save(new LevelLayoutOverride(
                1,
                RoomLayoutBuilder.GRID_COLUMNS,
                RoomLayoutBuilder.GRID_ROWS,
                new int[RoomLayoutBuilder.GRID_ROWS][RoomLayoutBuilder.GRID_COLUMNS],
                List.of(),
                List.of(),
                null,
                null,
                null,
                List.of(),
                true,
                List.of(new LevelLayoutOverride.TokenPlacement(
                        "int",
                        TokenType.CODE,
                        LevelLayoutOverride.TokenPlacementKind.VISIBLE,
                        0,
                        0,
                        0
                ))
        ));
        LevelManager levelManager = new LevelManager(store);
        levelManager.loadLevels();
        Level level = levelManager.getLevel(1);
        GameState gameState = new GameState();
        gameState.resetForLevel(level);
        Player player = gameState.getPlayer();

        assertFalse(level.getRoom().getTokens().get(0).intersects(player));
    }

    private Rect rect(Level level, String label, Token token) {
        return new Rect(level.getDisplayId() + " " + label, token.getX(), token.getY(), token.getWidth(), token.getHeight());
    }

    private Rect rect(Level level, String label, Chest chest) {
        return new Rect(level.getDisplayId() + " " + label, chest.getX(), chest.getY(), chest.getWidth(), chest.getHeight());
    }

    private Rect rect(Level level, String label, Door door) {
        return new Rect(level.getDisplayId() + " " + label, door.getX(), door.getY(), door.getWidth(), door.getHeight());
    }

    private Rect rect(Level level, String label, ProgrammableObject object) {
        return new Rect(level.getDisplayId() + " " + label, object.getX(), object.getY(), object.getWidth(), object.getHeight());
    }

    private void assertContractMatchesRoom(Level level) {
        MedalContractType type = level.getMedalContract().type();
        if (level.getRoom().hasChallengeDoor()) {
            assertEquals(MedalContractType.SOLVE_QUESTION_DOOR, type, level.getDisplayId());
            return;
        }
        if (level.getRoom().hasHiddenHelper()) {
            assertEquals(MedalContractType.NO_HELPER, type, level.getDisplayId());
            return;
        }
        assertEquals(MedalContractType.NO_HINTS, type, level.getDisplayId());
    }

    private record Rect(String name, double x, double y, double width, double height) {
        private boolean intersects(Rect other) {
            return x < other.x + other.width
                    && x + width > other.x
                    && y < other.y + other.height
                    && y + height > other.y;
        }
    }
}

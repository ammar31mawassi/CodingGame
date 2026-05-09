package com.codeescape.model;

import com.codeescape.validation.CodeValidator;
import com.codeescape.validation.ValidationResult;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {
    private final Puzzle puzzle = new Puzzle("Test", "Test goal", List.of(), alwaysValid());

    @Test
    void goalTokenHidesGoalUntilCollected() {
        Token goalToken = new Token("Goal", 0, 0, 20, 20, TokenType.GOAL);
        Room room = new Room(100, 100, List.of(goalToken), new Door(90, 0, 10, 10), puzzle);
        Inventory inventory = new Inventory();

        assertFalse(room.isGoalFound());

        room.collectToken(goalToken, inventory);

        assertTrue(room.isGoalFound());
        assertTrue(inventory.getTokens().isEmpty());
    }

    @Test
    void finalChestRewardIsFoundLast() {
        Room room = new Room(
                100,
                100,
                List.of(),
                List.of(),
                List.of(new Chest(0, 0, 20, 20), new Chest(30, 0, 20, 20)),
                List.of(ChestReward.code("int")),
                ChestReward.helper(),
                new Door(90, 0, 10, 10),
                puzzle
        );
        Inventory inventory = new Inventory();

        ChestReward firstReward = room.openChest(room.getChests().get(0), inventory);
        ChestReward finalReward = room.openChest(room.getChests().get(1), inventory);

        assertEquals(TokenType.CODE, firstReward.getType());
        assertEquals("int", inventory.getTokenValues().get(0));
        assertEquals(TokenType.HELPER, finalReward.getType());
        assertTrue(room.isHelperFound());
    }

    @Test
    void lockedChestsDoNotOpenUntilUnlocked() {
        Chest chest = new Chest(0, 0, 20, 20, true);
        Room room = new Room(
                100,
                100,
                List.of(),
                List.of(),
                List.of(chest),
                List.of(ChestReward.code("key")),
                null,
                new Door(90, 0, 10, 10),
                puzzle
        );
        Inventory inventory = new Inventory();

        assertEquals(null, room.openChest(chest, inventory));
        assertFalse(chest.isOpened());
        assertTrue(inventory.getTokens().isEmpty());

        chest.unlock();
        ChestReward reward = room.openChest(chest, inventory);

        assertTrue(chest.isOpened());
        assertEquals("key", reward.getValue());
        assertEquals(List.of("key"), inventory.getTokenValues());
    }

    @Test
    void programmableObjectActivationUnlocksTargetChest() {
        Chest chest = new Chest(0, 0, 20, 20, true);
        ProgrammableObject programmableObject = new ProgrammableObject(
                "ironChest",
                "ironChest",
                10,
                10,
                40,
                20,
                puzzle,
                chest
        );

        programmableObject.activate();

        assertTrue(programmableObject.isActivated());
        assertFalse(chest.isLocked());
    }

    private CodeValidator alwaysValid() {
        return code -> ValidationResult.success("ok");
    }
}

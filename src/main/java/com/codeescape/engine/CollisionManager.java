package com.codeescape.engine;

import com.codeescape.model.Chest;
import com.codeescape.model.ChestReward;
import com.codeescape.model.Door;
import com.codeescape.model.Inventory;
import com.codeescape.model.Player;
import com.codeescape.model.Room;
import com.codeescape.model.Token;
import com.codeescape.model.Wall;
import java.util.ArrayList;
import java.util.List;

public class CollisionManager {
    public boolean playerTouchesToken(Player player, Token token) {
        return token.intersects(player);
    }

    public boolean playerTouchesDoor(Player player, Door door) {
        return door.intersects(player);
    }

    public List<Token> handleTokenCollection(Player player, Room room, Inventory inventory) {
        List<Token> collectedTokens = new ArrayList<>();
        for (Token token : room.getTokens()) {
            if (!token.isCollected() && playerTouchesToken(player, token)) {
                room.collectToken(token, inventory);
                collectedTokens.add(token);
            }
        }
        return collectedTokens;
    }

    public ChestReward handleChestInteraction(Player player, Room room, Inventory inventory) {
        for (Chest chest : room.getChests()) {
            if (!chest.isOpened() && chest.intersects(player)) {
                return room.openChest(chest, inventory);
            }
        }
        return null;
    }

    public boolean playerTouchesWall(Player player, Wall wall) {
        return wall.intersects(player);
    }

    public boolean hasWallCollision(Player player, Room room) {
        return room.getWalls().stream().anyMatch(wall -> playerTouchesWall(player, wall));
    }

    public boolean canExitLevel(Player player, Door door) {
        return !door.isLocked() && playerTouchesDoor(player, door);
    }
}

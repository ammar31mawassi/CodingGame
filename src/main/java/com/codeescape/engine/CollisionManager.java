package com.codeescape.engine;

import com.codeescape.model.Door;
import com.codeescape.model.Inventory;
import com.codeescape.model.Player;
import com.codeescape.model.Room;
import com.codeescape.model.Token;

public class CollisionManager {
    public boolean playerTouchesToken(Player player, Token token) {
        return token.intersects(player);
    }

    public boolean playerTouchesDoor(Player player, Door door) {
        return door.intersects(player);
    }

    public void handleTokenCollection(Player player, Room room, Inventory inventory) {
        for (Token token : room.getTokens()) {
            if (!token.isCollected() && playerTouchesToken(player, token)) {
                room.collectToken(token, inventory);
            }
        }
    }

    public boolean canExitLevel(Player player, Door door) {
        return !door.isLocked() && playerTouchesDoor(player, door);
    }
}

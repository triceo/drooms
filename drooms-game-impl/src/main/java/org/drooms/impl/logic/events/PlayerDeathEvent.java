package org.drooms.impl.logic.events;

import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerRelated;

public class PlayerDeathEvent implements PlayerRelated {

    private final Player player;

    public PlayerDeathEvent(final Player p) {
        this.player = p;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}

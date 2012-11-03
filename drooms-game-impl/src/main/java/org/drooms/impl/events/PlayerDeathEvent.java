package org.drooms.impl.events;

import org.drooms.api.Player;

public class PlayerDeathEvent implements PlayerEvent {

    private final Player player;

    public PlayerDeathEvent(final Player p) {
        this.player = p;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}

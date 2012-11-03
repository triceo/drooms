package org.drooms.impl.events;

import org.drooms.api.Player;

public class PlayerLengthChangeEvent implements PlayerEvent {

    private final Player player;
    private final int newLength;

    public PlayerLengthChangeEvent(final Player p, final int length) {
        this.player = p;
        this.newLength = length;
    }

    public int getNewLength() {
        return this.newLength;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

}

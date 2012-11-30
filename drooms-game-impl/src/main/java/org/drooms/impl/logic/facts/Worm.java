package org.drooms.impl.logic.facts;

import org.drooms.api.Player;

public class Worm implements Positioned {

    private final Player player;

    private final int x, y;

    public Worm(final Player p, final int x, final int y) {
        this.player = p;
        this.x = x;
        this.y = y;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

}
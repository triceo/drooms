package org.drooms.impl.logic.facts;

import org.drooms.api.Player;

public class CurrentPlayer implements Positioned {

    private final Player player;

    private int x, y;

    public CurrentPlayer(final Player p, final int x, final int y) {
        this.player = p;
        this.x = x;
        this.y = y;
    }

    public Player get() {
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

    public void setX(final int x) {
        this.x = x;
    }

    public void setY(final int y) {
        this.y = y;
    }
}
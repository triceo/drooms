package org.drooms.impl.events;

import org.drooms.api.Player;

public class SurvivalRewardEvent implements RewardEvent, PlayerEvent {

    private final Player player;
    private final int points;

    public SurvivalRewardEvent(final Player p, final int points) {
        this.player = p;
        this.points = points;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public int getPoints() {
        return this.points;
    }

}

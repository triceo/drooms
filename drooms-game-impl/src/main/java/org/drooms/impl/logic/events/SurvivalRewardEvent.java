package org.drooms.impl.logic.events;

import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.RewardRelated;

public class SurvivalRewardEvent implements RewardRelated, PlayerRelated {

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

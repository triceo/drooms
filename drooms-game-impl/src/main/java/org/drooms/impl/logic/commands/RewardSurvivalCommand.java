package org.drooms.impl.logic.commands;

import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;
import org.drooms.impl.logic.PlayerLogic;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.RewardRelated;
import org.drooms.impl.logic.events.SurvivalRewardEvent;

public class RewardSurvivalCommand implements Command, PlayerRelated,
        RewardRelated {

    private final Player toReward;
    private final int rewardAmount;
    private final SurvivalRewardEvent event;

    public RewardSurvivalCommand(final Player p, final int amount) {
        this.toReward = p;
        this.rewardAmount = amount;
        this.event = new SurvivalRewardEvent(p, amount);
    }

    @Override
    public Player getPlayer() {
        return this.toReward;
    }

    @Override
    public int getPoints() {
        return this.rewardAmount;
    }

    @Override
    public void perform(final PlayerLogic logic) {
        logic.notifyOfSurvivalReward(this.event);
    }

    @Override
    public void report(final GameProgressListener report) {
        report.playerSurvived(this.toReward, this.rewardAmount);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RewardSurvivalCommand [toReward=")
                .append(this.toReward).append(", rewardAmount=")
                .append(this.rewardAmount).append("]");
        return builder.toString();
    }

}

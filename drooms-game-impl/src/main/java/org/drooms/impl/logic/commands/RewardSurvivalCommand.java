package org.drooms.impl.logic.commands;

import org.drooms.api.GameReport;
import org.drooms.api.Player;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.logic.CommandDistributor;
import org.drooms.impl.logic.DecisionMaker;
import org.drooms.impl.logic.PlayerRelated;
import org.drooms.impl.logic.RewardRelated;
import org.drooms.impl.logic.events.SurvivalRewardEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardSurvivalCommand implements Command<DefaultPlayground>, PlayerRelated, RewardRelated {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RewardSurvivalCommand.class);

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
    public boolean isValid(final CommandDistributor controller) {
        return controller.hasPlayer(this.toReward);
    }

    @Override
    public void perform(final DecisionMaker logic) {
        logic.notifyOfSurvivalReward(this.event);
    }

    @Override
    public void report(final GameReport<DefaultPlayground> report) {
        report.playerSurvived(this.toReward, this.rewardAmount);
        RewardSurvivalCommand.LOGGER
                .info("Player {} has been rewarded {} points for surviving another turn.",
                        this.toReward.getName(), this.rewardAmount);
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

package org.drooms.impl.logic;

import org.drooms.impl.logic.events.*;

public interface PlayerLogic {

    void notifyOfCollectibleAddition(final CollectibleAdditionEvent evt);

    void notifyOfCollectibleRemoval(final CollectibleRemovalEvent evt);

    void notifyOfCollectibleReward(final CollectibleRewardEvent evt);

    void notifyOfDeath(final PlayerDeathEvent evt);

    void notifyOfPlayerMove(final PlayerActionEvent evt);

    void notifyOfSurvivalReward(final SurvivalRewardEvent evt);


}

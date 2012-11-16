package org.drooms.impl;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Collectible;
import org.drooms.api.Move;
import org.drooms.api.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PlayerController.class);

    private final Map<Player, PlayerDecisionLogic> players = new LinkedHashMap<Player, PlayerDecisionLogic>();
    private final Set<Collectible> collectibles = new HashSet<Collectible>();

    public PlayerController(final DefaultPlayground playground,
            final List<Player> players) {
        for (final Player player : players) {
            this.players.put(player,
                    new PlayerDecisionLogic(player, playground));
        }
    }

    public void addCollectible(final Collectible c, final DefaultNode node) {
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfCollectibleAddition(c, node);
        }
        this.collectibles.add(c);
        PlayerController.LOGGER.info("New collectible {} added at node {}.", c,
                node);
    }

    public void collectCollectible(final Collectible c, final Player p) {
        this.validatePlayer(p);
        this.validateCollectible(c);
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfCollectibleReward(c, p, c.getPoints());
        }
        this.collectibles.remove(c);
        PlayerController.LOGGER.info("Player {} collected {}.", p.getName(), c);
    }

    public void crash(final Player p) {
        this.validatePlayer(p);
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfDeath(p);
        }
        this.players.remove(p);
        PlayerController.LOGGER.info(
                "Player {} crashed and will not see next turn.", p.getName());
    }

    public void deactivate(final Player p) {
        this.validatePlayer(p);
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfDeath(p);
        }
        this.players.remove(p);
        PlayerController.LOGGER.info(
                "Player {} was removed from the game due to inactivity.",
                p.getName());
    }

    public Map<Player, Move> execute() {
        PlayerController.LOGGER.info("Asking players to decide.");
        // determine the movements of players
        final Map<Player, Move> moves = new HashMap<Player, Move>();
        for (final Map.Entry<Player, PlayerDecisionLogic> entry : this.players
                .entrySet()) {
            final Move decision = entry.getValue().decideNextMove();
            final Player player = entry.getKey();
            moves.put(player, decision);
        }
        PlayerController.LOGGER.info("All players have decided.");
        return Collections.unmodifiableMap(moves);
    }

    public void movePlayer(final Player p, final Move m,
            final Deque<DefaultNode> position) {
        this.validatePlayer(p);
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfPlayerMove(p, m, position.getFirst(),
                    Collections.unmodifiableCollection(position));
        }
        PlayerController.LOGGER.info("Player {} is now located at {}.",
                p.getName(), position);
    }

    public void removeCollectible(final Collectible c) {
        this.validateCollectible(c);
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfCollectibleRemoval(c);
        }
        this.collectibles.remove(c);
        PlayerController.LOGGER.info(
                "Collectible {} removed from playground..", c);
    }

    public void rewardSurvival(final Player p, final int points) {
        this.validatePlayer(p);
        for (final PlayerDecisionLogic logic : this.players.values()) {
            logic.notifyOfSurvivalReward(p, points);
        }
        PlayerController.LOGGER.info(
                "Player {} survived another turn, awarded {} points.",
                p.getName(), points);
    }

    private void validateCollectible(final Collectible c) {
        if (!this.collectibles.contains(c)) {
            throw new IllegalArgumentException("Non-existent collectible: " + c);
        }
    }

    private void validatePlayer(final Player p) {
        if (!this.players.containsKey(p)) {
            throw new IllegalArgumentException("Non-existent player: " + p);
        }
    }

}

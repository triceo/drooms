package org.drooms.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drooms.api.Collectible;
import org.drooms.api.Game;
import org.drooms.api.GameReport;
import org.drooms.api.Move;
import org.drooms.api.Player;
import org.drooms.api.Strategy;
import org.drooms.impl.collectibles.CheapCollectible;
import org.drooms.impl.collectibles.ExtremeCollectible;
import org.drooms.impl.collectibles.GoodCollectible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstGame implements
        Game<DefaultSituation, DefaultPlayground, DefaultNode, DefaultEdge> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FirstGame.class);

    private enum CollectibleType {

        CHEAP("cheap"), GOOD("good"), EXTREME("extreme");

        private static final String COMMON_PREFIX = "collectible.";
        private static final String PROBABILITY_PREFIX = CollectibleType.COMMON_PREFIX
                + "probability.";
        private static final String PRICE_PREFIX = CollectibleType.COMMON_PREFIX
                + "price.";
        private static final String EXPIRATION_PREFIX = CollectibleType.COMMON_PREFIX
                + "expiration.";
        private final String collectibleName;

        CollectibleType(final String propertyName) {
            this.collectibleName = propertyName;
        }

        public int getExpiration(final Properties config) {
            final String price = config.getProperty(
                    CollectibleType.EXPIRATION_PREFIX + this.collectibleName,
                    "1");
            return Integer.valueOf(price);
        }

        public int getPoints(final Properties config) {
            final String price = config.getProperty(
                    CollectibleType.PRICE_PREFIX + this.collectibleName, "1");
            return Integer.valueOf(price);
        }

        public BigDecimal getProbabilityOfAppearance(final Properties config) {
            final String probability = config.getProperty(
                    CollectibleType.PROBABILITY_PREFIX + this.collectibleName,
                    "0.1");
            return new BigDecimal(probability);
        }
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(final String[] args) {
        try (Reader gameConfigFile = new FileReader(args[0]);
                Reader playerConfigFile = new FileReader(args[1]); FileOutputStream fos = new FileOutputStream(new File(args[2]))) {
            // prepare configs
            final Properties gameConfig = new Properties();
            gameConfig.load(gameConfigFile);
            final Properties playerConfig = new Properties();
            playerConfig.load(playerConfigFile);
            // play and report
            GameReport<DefaultSituation, DefaultPlayground, DefaultNode, DefaultEdge> report = new FirstGame().play(gameConfig, playerConfig);
            report.write(fos);
        } catch (final IOException e) {
            System.out.println(e);
            System.exit(1);
        }

    }

    private final Set<Collectible> collectibles = new HashSet<Collectible>();

    private final Map<Player, Integer> playerPoints = new HashMap<Player, Integer>();
    private final Map<URL, ClassLoader> strategyClassloaders = new HashMap<URL, ClassLoader>();

    private final Map<String, Strategy> strategyInstances = new HashMap<String, Strategy>();
    
    private char getCharPerNumber(int number) {
        if (number >= 0 && number < 10) {
            // for first 10 players, we have numbers 0 - 9
            return (char)(48 + number);
        } else if (number > 9 && number < 36){
            // for next 25 players, we have capital letters
            return (char)(55 + number);
        } else {
            throw new IllegalArgumentException("Invalid number of a player: " + number);
        }
    }
    
    private List<Player> constructPlayers(final Properties config,
            final Properties playerConfig) {
        // parse a list of players
        final Map<String, String> playerStrategies = new HashMap<String, String>();
        final Map<String, URL> strategyJars = new HashMap<String, URL>();
        for (final String playerName : playerConfig.stringPropertyNames()) {
            final String strategyDescr = playerConfig.getProperty(playerName);
            final String[] parts = strategyDescr.split("\\Q@\\E");
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid strategy descriptor: " + strategyDescr);
            }
            final String strategyClass = parts[0];
            URL strategyJar;
            try {
                strategyJar = new URL(parts[1]);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(
                        "Invalid URL in the strategy descriptor: "
                                + strategyDescr, e);
            }
            playerStrategies.put(playerName, strategyClass);
            strategyJars.put(strategyClass, strategyJar);
        }
        // load strategies for players
        final List<Player> players = new ArrayList<Player>();
        int playerNum = 0;
        for (final Map.Entry<String, String> entry : playerStrategies
                .entrySet()) {
            final String playerName = entry.getKey();
            final String strategyClass = entry.getValue();
            final URL strategyJar = strategyJars.get(strategyClass);
            Strategy strategy;
            try {
                strategy = this.loadStrategy(strategyClass, strategyJar);
            } catch (final Exception e) {
                throw new IllegalArgumentException("Failed loading: "
                        + strategyClass, e);
            }
            KnowledgeBuilder kb = strategy.getKnowledgeBuilder(this.loadJar(strategyJar));
            try {
                KnowledgeBase kbase = kb.newKnowledgeBase();
                players.add(new DefaultPlayer(playerName, getCharPerNumber(playerNum), kbase));
                playerNum++;
            } catch (Exception ex) {
                for (KnowledgeBuilderError error: kb.getErrors()) {
                    LOGGER.error(error.toString());
                }
                throw new IllegalStateException("Cannot create knowledge base for strategy: " + strategy.getName(), ex);
            }
        }
        return players;
    }

    private ClassLoader loadJar(final URL strategyJar) {
        if (!this.strategyClassloaders.containsKey(strategyJar)) {
            final ClassLoader loader = URLClassLoader
                    .newInstance(new URL[] { strategyJar }, this.getClass()
                            .getClassLoader());
            this.strategyClassloaders.put(strategyJar, loader);
        }
        return this.strategyClassloaders.get(strategyJar);
    }

    private Strategy loadStrategy(final String strategyClass,
            final URL strategyJar) throws Exception {
        if (!this.strategyInstances.containsKey(strategyClass)) {
            final Class<?> clz = Class.forName(strategyClass, true,
                    this.loadJar(strategyJar));
            final Strategy strategy = (Strategy) clz.newInstance();
            this.strategyInstances.put(strategyClass, strategy);
        }
        return this.strategyInstances.get(strategyClass);
    }

    private DefaultNode pickRandomUnusedNode(final DefaultSituation situation,
            final Collection<Player> players) {
        final DefaultPlayground p = situation.getPlayground();
        final List<DefaultNode> nodes = new LinkedList<DefaultNode>();
        // locate available nodes
        for (int x = 0; x < p.getWidth(); x++) {
            for (int y = 0; y < p.getHeight(); y++) {
                if (p.isAvailable(x, y)) {
                    nodes.add(p.getNode(x, y));
                }
            }
        }
        // exclude nodes where worms are
        for (final Player player : players) {
            nodes.removeAll(situation.getPositions(player));
        }
        // exclude nodes where collectibles are
        final List<DefaultNode> nodesCopy = new LinkedList<DefaultNode>(nodes);
        for (final DefaultNode n : nodesCopy) {
            if (situation.getCollectible(n) != null) {
                nodes.remove(n);
            }
        }
        if (nodes.size() == 0) {
            return null;
        } else {
            return nodes.get(FirstGame.RANDOM.nextInt(nodes.size()));
        }
    }

    @Override
    public GameReport<DefaultSituation, DefaultPlayground, DefaultNode, DefaultEdge> play(
            final Properties gameConfig, final Properties playerConfig) {
        Reporter reporter = new Reporter();
        final List<Player> players = this.constructPlayers(gameConfig,
                playerConfig);
        DefaultPlayground playground;
        try {
            playground = DefaultPlayground.read(new File(gameConfig
                    .getProperty("playground.file")));
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                    "Playground file cannot be read!", e);
        }
        final int wormLength = Integer.valueOf(gameConfig.getProperty(
                "worm.length.start", "1"));
        // prepare situation
        DefaultSituation currentSituation = new DefaultSituation(playground,
                players, wormLength);
        final Set<Player> currentPlayers = new HashSet<Player>(players);
        for (Player p: currentPlayers) { // initialize player points
            reward(p, 0);
        }
        do {
            // expire uncollected collectibles
            final Set<Collectible> removeCollectibles = new HashSet<Collectible>();
            for (final Collectible c : this.collectibles) {
                if (currentSituation.getTurnNumber() >= c.expiresInTurn()) {
                    currentSituation.removeCollectible(c);
                    removeCollectibles.add(c);
                }
            }
            this.collectibles.removeAll(removeCollectibles);
            // add points for collected collectibles
            for (final Player p : currentPlayers) {
                final DefaultNode headPosition = currentSituation
                        .getHeadPosition(p);
                final Collectible c = currentSituation
                        .getCollectible(headPosition);
                if (c != null) { // successfully collected
                    currentSituation.collectCollectible(c, p);
                    currentSituation.setPlayerLength(p,
                            currentSituation.getPlayerLength(p) + 1);
                    this.reward(p, c.getPoints());
                    this.collectibles.remove(c);
                }
            }
            // distribute new collectibles
            for (final CollectibleType ct : CollectibleType.values()) {
                final BigDecimal probability = ct
                        .getProbabilityOfAppearance(gameConfig);
                final BigDecimal chosen = BigDecimal.valueOf(FirstGame.RANDOM
                        .nextDouble());
                if (probability.compareTo(chosen) > 0) {
                    final int expiration = currentSituation.getTurnNumber()
                            + ct.getExpiration(gameConfig);
                    final int points = ct.getPoints(gameConfig);
                    Collectible c = null;
                    switch (ct) {
                        case CHEAP:
                            c = new CheapCollectible(points, expiration);
                            break;
                        case GOOD:
                            c = new GoodCollectible(points, expiration);
                            break;
                        case EXTREME:
                            c = new ExtremeCollectible(points, expiration);
                            break;
                        default:
                            throw new IllegalStateException(
                                    "Unknown collectible type!");
                    }
                    this.collectibles.add(c);
                    currentSituation.addCollectible(c, this
                            .pickRandomUnusedNode(currentSituation,
                                    currentPlayers));
                }
            }
            // make the move
            reporter.addTurn(currentSituation, playerPoints);
            currentSituation = currentSituation.move();
            // remove inactive worms
            final int allowedInactiveTurns = Integer.valueOf(gameConfig
                    .getProperty("worm.max.inactive.turns", "3"));
            final Set<Player> inactiveWorms = new HashSet<Player>();
            if (currentSituation.getTurnNumber() > allowedInactiveTurns) {
                for (final Player p : currentPlayers) {
                    final Move[] moves = currentSituation.getDecisionRecord(p)
                            .toArray(new Move[] {});
                    boolean active = false;
                    for (int i = moves.length - allowedInactiveTurns - 1; i < moves.length; i++) {
                        if (moves[i] != Move.STAY) {
                            // the worm has been active
                            active = true;
                            break;
                        }
                    }
                    if (!active) {
                        inactiveWorms.add(p);
                    }
                }
            }
            // resolve worms colliding
            final Set<Player> oneCollidedWorms = new HashSet<Player>();
            final Set<Pair<Player, Player>> bothCollidedWorms = new HashSet<Pair<Player, Player>>();
            for (final Player p1 : currentPlayers) {
                final DefaultNode firstPosition = currentSituation
                        .getHeadPosition(p1);
                if (!playground.isAvailable(firstPosition.getX(), firstPosition.getY())) {
                    oneCollidedWorms.add(p1);
                    continue;
                } else {
                    // make sure the worm didn't crash into itself
                    Collection<DefaultNode> positions = currentSituation.getPositions(p1);
                    Set<DefaultNode> nodes = new HashSet<DefaultNode>(positions);
                    if (nodes.size() < positions.size()) {
                        // a worm occupies one node twice = a crash into itself
                        oneCollidedWorms.add(p1);
                    }
                }
                for (final Player p2 : currentPlayers) {
                    if (p1 == p2) {
                        // the same worm
                        continue;
                    }
                    final DefaultNode secondPosition = currentSituation
                            .getHeadPosition(p2);
                    if (firstPosition.equals(secondPosition)) {
                        // head-on-head collision
                        bothCollidedWorms.add(Pair.of(p1, p2));
                    } else if (currentSituation.getPositions(p1).contains(
                            secondPosition)) {
                        // head-on-body collision
                        oneCollidedWorms.add(p2);
                    }
                }
            }
            for (final Player player : inactiveWorms) {
                currentSituation.deactivate(player);
                currentPlayers.remove(player);
            }
            for (final Pair<Player, Player> pair : bothCollidedWorms) {
                if (!currentPlayers.contains(pair.getLeft())
                        && !currentPlayers.contains(pair.getRight())) {
                    // make sure we don't count the same pair twice
                    continue;
                }
                currentSituation.collide(pair.getLeft(), pair.getRight());
                currentPlayers.remove(pair.getLeft());
                currentPlayers.remove(pair.getRight());
            }
            for (final Player player : oneCollidedWorms) {
                if (!currentPlayers.contains(player)) {
                    // prevent double removal in case worm is already removed
                    continue;
                }
                currentSituation.crash(player);
                currentPlayers.remove(player);
            }
            // reward surviving worms
            for (final Player p : currentPlayers) {
                this.reward(p, 1);
                currentSituation.rewardSurvival(p, Integer.valueOf(gameConfig
                        .getProperty("worm.survival.bonus", "1")));
            }
        } while (currentPlayers.size() > 1);
        return reporter;
    }

    private void reward(final Player p, final int points) {
        if (!this.playerPoints.containsKey(p)) {
            this.playerPoints.put(p, 0);
        }
        this.playerPoints.put(p, this.playerPoints.get(p) + points);
    }

}

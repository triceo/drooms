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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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

public class GameController implements
        Game<DefaultPlayground, DefaultNode, DefaultEdge> {

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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GameController.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(final String[] args) {
        try (Reader gameConfigFile = new FileReader(args[0]);
                Reader playerConfigFile = new FileReader(args[1]);
                FileOutputStream fos = new FileOutputStream(new File(args[2]))) {
            // prepare configs
            final Properties gameConfig = new Properties();
            gameConfig.load(gameConfigFile);
            final Properties playerConfig = new Properties();
            playerConfig.load(playerConfigFile);
            // play and report
            new GameController().play(gameConfig, playerConfig);
        } catch (final IOException e) {
            System.out.println(e);
            System.exit(1);
        }

    }

    private final Set<Collectible> collectibles = new HashSet<Collectible>();

    private final Map<Player, Integer> playerPoints = new HashMap<Player, Integer>();
    private final Map<URL, ClassLoader> strategyClassloaders = new HashMap<URL, ClassLoader>();

    private final Map<String, Strategy> strategyInstances = new HashMap<String, Strategy>();

    private final Map<Player, Integer> lengths = new HashMap<Player, Integer>();

    private final Map<Player, Deque<DefaultNode>> positions = new HashMap<Player, Deque<DefaultNode>>();

    private final Map<Collectible, DefaultNode> nodesByCollectible = new HashMap<Collectible, DefaultNode>();

    private final Map<DefaultNode, Collectible> collectiblesByNode = new HashMap<DefaultNode, Collectible>();

    private final Map<Player, SortedMap<Integer, Move>> decisionRecord = new HashMap<Player, SortedMap<Integer, Move>>();

    private void addCollectible(final Collectible c, final DefaultNode n) {
        this.collectiblesByNode.put(n, c);
        this.nodesByCollectible.put(c, n);
    }

    private void addDecision(final Player p, final Move m, final int turnNumber) {
        if (!this.decisionRecord.containsKey(p)) {
            this.decisionRecord.put(p, new TreeMap<Integer, Move>());
        }
        this.decisionRecord.get(p).put(turnNumber, m);
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
            final KnowledgeBuilder kb = strategy.getKnowledgeBuilder(this
                    .loadJar(strategyJar));
            try {
                final KnowledgeBase kbase = kb.newKnowledgeBase();
                players.add(new DefaultPlayer(playerName, this
                        .getCharPerNumber(playerNum), kbase));
                playerNum++;
            } catch (final Exception ex) {
                for (final KnowledgeBuilderError error : kb.getErrors()) {
                    GameController.LOGGER.error(error.toString());
                }
                throw new IllegalStateException(
                        "Cannot create knowledge base for strategy: "
                                + strategy.getName(), ex);
            }
        }
        return players;
    }

    private Collectible getCollectible(final DefaultNode n) {
        return this.collectiblesByNode.get(n);
    }

    private Deque<Move> getDecisionRecord(final Player p) {
        final LinkedList<Move> moves = new LinkedList<Move>();
        for (final SortedMap.Entry<Integer, Move> entry : this.decisionRecord
                .get(p).entrySet()) {
            moves.add(entry.getKey(), entry.getValue());
        }
        return moves;
    }

    private char getCharPerNumber(final int number) {
        if (number >= 0 && number < 10) {
            // for first 10 players, we have numbers 0 - 9
            return (char) (48 + number);
        } else if (number > 9 && number < 36) {
            // for next 25 players, we have capital letters
            return (char) (55 + number);
        } else {
            throw new IllegalArgumentException("Invalid number of a player: "
                    + number);
        }
    }

    private int getPlayerLength(final Player p) {
        if (!this.lengths.containsKey(p)) {
            throw new IllegalStateException(
                    "Player doesn't have any length assigned: " + p);
        }
        return this.lengths.get(p);
    }

    private Deque<DefaultNode> getPlayerPosition(final Player p) {
        if (!this.positions.containsKey(p)) {
            throw new IllegalStateException(
                    "Player doesn't have any position assigned: " + p);
        }
        return this.positions.get(p);
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

    private Map<Collectible, Player> performCollectibleCollection(
            final Collection<Player> players) {
        final Map<Collectible, Player> collections = new HashMap<Collectible, Player>();
        for (final Player p : players) {
            final DefaultNode headPosition = this.getPlayerPosition(p)
                    .getFirst();
            final Collectible c = this.getCollectible(headPosition);
            if (c != null) { // successfully collected
                collections.put(c, p);
                this.setPlayerLength(p, this.getPlayerLength(p) + 1);
            }
        }
        return Collections.unmodifiableMap(collections);
    }

    private Map<Collectible, DefaultNode> performCollectibleDistribution(
            final Properties gameConfig, final DefaultPlayground playground,
            final Collection<Player> players, final int currentTurnNumber) {
        final Map<Collectible, DefaultNode> collectibles = new HashMap<Collectible, DefaultNode>();
        for (final CollectibleType ct : CollectibleType.values()) {
            final BigDecimal probability = ct
                    .getProbabilityOfAppearance(gameConfig);
            final BigDecimal chosen = BigDecimal.valueOf(GameController.RANDOM
                    .nextDouble());
            if (probability.compareTo(chosen) > 0) {
                final int expiration = currentTurnNumber
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
                collectibles.put(c,
                        this.pickRandomUnusedNode(playground, players));
            }
        }
        return Collections.unmodifiableMap(collectibles);
    }

    private Set<Player> performCollisionDetection(
            final DefaultPlayground playground,
            final Collection<Player> currentPlayers) {
        final Set<Player> collisions = new HashSet<Player>();
        for (final Player p1 : currentPlayers) {
            final Deque<DefaultNode> position = this.getPlayerPosition(p1);
            final DefaultNode firstPosition = position.getFirst();
            if (!playground.isAvailable(firstPosition.getX(),
                    firstPosition.getY())) {
                collisions.add(p1);
                continue;
            } else {
                // make sure the worm didn't crash into itself
                final Set<DefaultNode> nodes = new HashSet<DefaultNode>(
                        position);
                if (nodes.size() < position.size()) {
                    // a worm occupies one node twice = a crash into itself
                    collisions.add(p1);
                }
            }
            for (final Player p2 : currentPlayers) {
                if (p1 == p2) {
                    // the same worm
                    continue;
                }
                final DefaultNode secondPosition = this.getPlayerPosition(p2)
                        .getFirst();
                if (firstPosition.equals(secondPosition)) {
                    // head-on-head collision
                    collisions.add(p1);
                    collisions.add(p2);
                } else if (position.contains(secondPosition)) {
                    // head-on-body collision
                    collisions.add(p2);
                }
            }
        }
        return Collections.unmodifiableSet(collisions);
    }

    private Set<Player> performInactivityDetection(
            final Collection<Player> currentPlayers,
            final int currentTurnNumber, final int allowedInactiveTurns) {
        final Set<Player> inactiveWorms = new HashSet<Player>();
        if (currentTurnNumber > allowedInactiveTurns) {
            for (final Player p : currentPlayers) {
                final Move[] moves = this.getDecisionRecord(p).toArray(
                        new Move[] {});
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
        return Collections.unmodifiableSet(inactiveWorms);
    }

    private Deque<DefaultNode> performPlayerMove(final Player player,
            final Move decision) {
        // move the head of the worm
        final Deque<DefaultNode> currentPos = this.getPlayerPosition(player);
        final DefaultNode currentHeadPos = currentPos.getFirst();
        DefaultNode newHeadPos;
        switch (decision) {
            case UP:
                newHeadPos = new DefaultNode(currentHeadPos.getX(),
                        currentHeadPos.getY() + 1);
                break;
            case DOWN:
                newHeadPos = new DefaultNode(currentHeadPos.getX(),
                        currentHeadPos.getY() - 1);
                break;
            case LEFT:
                newHeadPos = new DefaultNode(currentHeadPos.getX() - 1,
                        currentHeadPos.getY());
                break;
            case RIGHT:
                newHeadPos = new DefaultNode(currentHeadPos.getX() + 1,
                        currentHeadPos.getY());
                break;
            case STAY:
                newHeadPos = currentHeadPos;
                break;
            default:
                throw new IllegalStateException("Unknown move!");
        }
        // move the head of the snake
        final Deque<DefaultNode> newPosition = new LinkedList<DefaultNode>(
                currentPos);
        if (decision != Move.STAY) {
            newPosition.push(newHeadPos);
        }
        // make sure the snake is as long as it should be
        while (newPosition.size() > this.getPlayerLength(player)) {
            newPosition.removeLast();
        }
        // notify
        return newPosition;
    }

    private DefaultNode pickRandomUnusedNode(final DefaultPlayground p,
            final Collection<Player> players) {
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
            nodes.removeAll(this.getPlayerPosition(player));
        }
        // exclude nodes where collectibles are
        final List<DefaultNode> nodesCopy = new LinkedList<DefaultNode>(nodes);
        for (final DefaultNode n : nodesCopy) {
            if (this.getCollectible(n) != null) {
                nodes.remove(n);
            }
        }
        if (nodes.size() == 0) {
            return null;
        } else {
            return nodes.get(GameController.RANDOM.nextInt(nodes.size()));
        }
    }

    @Override
    public GameReport<DefaultPlayground, DefaultNode, DefaultEdge> play(
            final Properties gameConfig, final Properties playerConfig) {
        // prepare the playground
        DefaultPlayground playground;
        try {
            playground = DefaultPlayground.read(new File(gameConfig
                    .getProperty("playground.file")));
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                    "Playground file cannot be read!", e);
        }
        // prepare the players
        final List<Player> players = this.constructPlayers(gameConfig,
                playerConfig);
        final int wormLength = Integer.valueOf(gameConfig.getProperty(
                "worm.length.start", "1"));
        final int allowedInactiveTurns = Integer.valueOf(gameConfig
                .getProperty("worm.max.inactive.turns", "3"));
        final int wormSurvivalBonus = Integer.valueOf(gameConfig.getProperty(
                "worm.survival.bonus", "1"));
        // prepare starting positions
        final List<DefaultNode> startingPositions = playground
                .getStartingPositions();
        final int playersSupported = startingPositions.size();
        final int playersAvailable = players.size();
        if (playersSupported < playersAvailable) {
            throw new IllegalArgumentException(
                    "The playground doesn't support " + playersAvailable
                            + " players, only " + playersSupported + "! ");
        }
        int i = 0;
        for (final Player player : players) {
            final Deque<DefaultNode> pos = new LinkedList<DefaultNode>();
            pos.push(startingPositions.get(i));
            this.setPlayerPosition(player, pos);
            this.setPlayerLength(player, wormLength);
            GameController.LOGGER.info("Player {} assigned position {}.",
                    player.getName(), i);
            i++;
        }
        // prepare situation
        final PlayerController playerControl = new PlayerController(playground,
                players);
        final Set<Player> currentPlayers = new HashSet<Player>(players);
        Map<Player, Move> decisions = new HashMap<Player, Move>();
        for (final Player p : currentPlayers) { // initialize players
            decisions.put(p, Move.STAY);
            this.reward(p, 0);
        }
        // start the game
        int turnNumber = 0;
        do {
            GameController.LOGGER.info("Starting turn no. {}.", turnNumber);
            // remove inactive worms
            for (final Player player : this.performInactivityDetection(
                    currentPlayers, turnNumber, allowedInactiveTurns)) {
                currentPlayers.remove(player);
                playerControl.deactivate(player);
            }
            // move the worms
            for (final Player p : currentPlayers) {
                final Move m = decisions.get(p);
                this.addDecision(p, m, turnNumber);
                final Deque<DefaultNode> newPosition = this.performPlayerMove(
                        p, m);
                this.setPlayerPosition(p, newPosition);
                playerControl.movePlayer(p, m, newPosition);
            }
            // resolve worms colliding
            for (final Player player : this.performCollisionDetection(
                    playground, currentPlayers)) {
                playerControl.crash(player);
                currentPlayers.remove(player);
            }
            // reward surviving worms
            for (final Player p : currentPlayers) {
                this.reward(p, 1);
                playerControl.rewardSurvival(p, wormSurvivalBonus);
            }
            // expire uncollected collectibles
            final Set<Collectible> removeCollectibles = new HashSet<Collectible>();
            for (final Collectible c : this.collectibles) {
                if (c.expiresInTurn() >= 0 && turnNumber >= c.expiresInTurn()) {
                    playerControl.removeCollectible(c);
                    removeCollectibles.add(c);
                }
            }
            for (final Collectible c : removeCollectibles) {
                this.removeCollectible(c);
                playerControl.removeCollectible(c);
            }
            // add points for collected collectibles
            for (final Map.Entry<Collectible, Player> entry : this
                    .performCollectibleCollection(currentPlayers).entrySet()) {
                final Collectible c = entry.getKey();
                final Player p = entry.getValue();
                this.reward(p, c.getPoints());
                this.removeCollectible(c);
                playerControl.collectCollectible(c, p);
            }
            if (currentPlayers.size() < 2) {
                continue;
            }
            // distribute new collectibles
            for (final Map.Entry<Collectible, DefaultNode> entry : this
                    .performCollectibleDistribution(gameConfig, playground,
                            currentPlayers, turnNumber).entrySet()) {
                final Collectible c = entry.getKey();
                final DefaultNode n = entry.getValue();
                this.addCollectible(c, n);
                playerControl.addCollectible(c, n);
            }
            // make the move decision
            decisions = playerControl.execute();
            turnNumber++;
        } while (currentPlayers.size() > 1);
        return null;
    }

    private void removeCollectible(final Collectible c) {
        final DefaultNode n = this.nodesByCollectible.remove(c);
        this.collectiblesByNode.remove(n);
    }

    private void reward(final Player p, final int points) {
        if (!this.playerPoints.containsKey(p)) {
            this.playerPoints.put(p, 0);
        }
        this.playerPoints.put(p, this.playerPoints.get(p) + points);
    }

    private void setPlayerLength(final Player p, final int length) {
        this.lengths.put(p, length);
    }

    private void setPlayerPosition(final Player p,
            final Deque<DefaultNode> position) {
        this.positions.put(p, position);
    }

}

package org.drooms.impl.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drooms.api.Player;
import org.drooms.api.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerAssembly {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PlayerAssembly.class);
    private final Properties config;

    private final Map<URL, ClassLoader> strategyClassloaders = new HashMap<URL, ClassLoader>();

    private final Map<String, Strategy> strategyInstances = new HashMap<String, Strategy>();

    public PlayerAssembly(final Properties config) {
        this.config = config;
    }

    public List<Player> assemblePlayers() {
        // parse a list of players
        final Map<String, String> playerStrategies = new HashMap<String, String>();
        final Map<String, URL> strategyJars = new HashMap<String, URL>();
        for (final String playerName : this.config.stringPropertyNames()) {
            final String strategyDescr = this.config.getProperty(playerName);
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
                players.add(new Player(playerName, kbase));
            } catch (final Exception ex) {
                for (final KnowledgeBuilderError error : kb.getErrors()) {
                    PlayerAssembly.LOGGER.error(error.toString());
                }
                throw new IllegalStateException(
                        "Cannot create knowledge base for strategy: "
                                + strategy.getName(), ex);
            }
        }
        return Collections.unmodifiableList(players);
    }

    private ClassLoader loadJar(final URL strategyJar) {
        if (!this.strategyClassloaders.containsKey(strategyJar)) {
            @SuppressWarnings("resource")
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

}

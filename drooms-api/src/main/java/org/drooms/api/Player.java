package org.drooms.api;

import java.util.Collection;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.conf.EventProcessingOption;
import org.drools.conf.PermGenThresholdOption;
import org.drools.definition.KnowledgePackage;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.graph.Graph;

/**
 * Represents a worm in the {@link Game} on the {@link Playground}.
 */
public class Player {

    private final String name;
    private final CustomPathBasedStrategy strategy;
    private final Collection<KnowledgePackage> packages;
    private final ClassLoader classLoader;

    /**
     * Create a player instance.
     * 
     * @param name
     *            Name of the player.
     * @param strategy
     *            Strategy of the player.
     * @param strategyClassLoader
     *            Class loader used to load player's strategy.
     */
    public Player(final String name, final CustomPathBasedStrategy strategy,
            final ClassLoader strategyClassLoader) {
        if (name == null || strategy == null || strategyClassLoader == null) {
            throw new IllegalArgumentException(
                    "None of the parameters can be null.");
        }
        final KnowledgeBuilder kb = strategy
                .getKnowledgeBuilder(strategyClassLoader);
        this.packages = kb.getKnowledgePackages();
        this.strategy = strategy;
        this.name = name;
        this.classLoader = strategyClassLoader;
    }

    /**
     * Retrieve the player's strategy.
     * 
     * @return The strategy.
     */
    public KnowledgeBase constructKnowledgeBase() {
        final KnowledgeBaseConfiguration kbconf = KnowledgeBaseFactory
                .newKnowledgeBaseConfiguration(null, this.classLoader);
        kbconf.setOption(PermGenThresholdOption.get(0)); // workaround for
                                                         // https://github.com/triceo/drooms/issues/3
        kbconf.setOption(EventProcessingOption.STREAM);
        final KnowledgeBase kbase = KnowledgeBaseFactory
                .newKnowledgeBase(kbconf);
        kbase.addKnowledgePackages(this.packages);
        return kbase;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Retrieve the player's name.
     * 
     * @return The name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * See {@link CustomPathBasedStrategy#getShortestPathAlgorithm(Graph)}. This
     * method just relays there.
     */
    public ShortestPath<Node, Edge> getShortestPathAlgorithm(
            final Graph<Node, Edge> graph) {
        return this.strategy.getShortestPathAlgorithm(graph);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Player [name=").append(this.name).append("]");
        return builder.toString();
    }

}

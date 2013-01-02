package org.drooms.api;

import java.util.Collection;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.conf.PermGenThresholdOption;
import org.drools.definition.KnowledgePackage;

/**
 * Represents a worm in the {@link Game} on the {@link Playground}.
 */
public class Player {

    private final String name;
    private final Collection<KnowledgePackage> packages;
    private final ClassLoader classLoader;

    /**
     * Create a player instance.
     * 
     * @param name
     *            Name of the player.
     * @param knowledgePackages
     *            Rules that implement the player's strategy.
     * @param strategyClassLoader
     *            Class loader used to load player's strategy.
     */
    public Player(final String name, final Collection<KnowledgePackage> knowledgePackages, final ClassLoader strategyClassLoader) {
        if (name == null || knowledgePackages == null || strategyClassLoader == null) {
            throw new IllegalArgumentException(
                    "None of the parameters can be null.");
        }
        this.name = name;
        this.packages = knowledgePackages;
        this.classLoader = strategyClassLoader;
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
     * Retrieve the player's strategy.
     * 
     * @return The strategy.
     */
    public KnowledgeBase constructKnowledgeBase() {
        KnowledgeBaseConfiguration kbconf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, classLoader);
        kbconf.setOption(PermGenThresholdOption.get(0)); // workaround for https://github.com/triceo/drooms/issues/3
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(kbconf);
        kbase.addKnowledgePackages(packages);
        return kbase;
    }

    /**
     * Retrieve the player's name.
     * 
     * @return The name.
     */
    public String getName() {
        return this.name;
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

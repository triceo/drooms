package org.drooms.api;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;

/**
 * Represents a worm in the {@link Game} on the {@link Playground}.
 */
public class Player {

    private final String name;
    private final ReleaseId strategy;

    /**
     * Create a player instance.
     * 
     * @param name
     *            Name of the player.
     * @param strategy
     *            Strategy of the player, in the form of a kjar.
     */
    public Player(final String name, final ReleaseId strategy) {
        if (name == null || strategy == null) {
            throw new IllegalArgumentException("None of the parameters can be null.");
        }
        this.strategy = strategy;
        this.name = name;
    }

    /**
     * Retrieve the player's strategy.
     * 
     * @return The strategy.
     */
    public KieBase constructKieBase() {
        final KieServices ks = KieServices.Factory.get();
        final KieBaseConfiguration config = ks.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        final KieContainer kc = ks.newKieContainer(this.strategy);
        return kc.newKieBase(config);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Player [name=").append(this.name).append("]");
        return builder.toString();
    }

}

package org.drooms.impl.util;

import org.drooms.api.Player;
import org.drooms.impl.GameController;
import org.kie.api.builder.ReleaseId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collector;

/**
 * A helper class to load Strategy implementations for all requested {@link Player}s.
 */
public class PlayerProperties {

    private static final String GAV_PARTS_SEPARATOR = ":";

    private final File config;

    /**
     * Initialize the class.
     * 
     * @param f
     *            Game config as described in{@link GameController#play(org.drooms.api.Playground, Collection, File)}.
     */
    public PlayerProperties(final File f) {
        this.config = f;
    }

    private static <T>Collector<T, List<T>, List<T>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableList);
    }

    /**
     * Perform all the strategy resolution and return a list of fully
     * initialized players.
     * 
     * @return The unmodifiable collection of players, in the order in which
     *         they come up in the data file.
     */
    public List<Player> read() {
        Properties props = new Properties();
        try (FileReader fr = new FileReader(this.config)) {
            props.load(fr);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot read player config file.", e);
        }
        return props.stringPropertyNames().parallelStream().map(playerName -> {
            final String gav = props.getProperty(playerName);
            final String[] gavParts = gav.split("\\Q" + PlayerProperties.GAV_PARTS_SEPARATOR + "\\E");
            if (gavParts.length != 3) {
                throw new IllegalStateException("Player " + playerName + " has a wrong Maven GAV " + gav + ".");
            }
            return new Player(playerName, gavParts[0], gavParts[1], gavParts[2]);
        }).collect(toImmutableList());
    }

    /**
     * Write players to a property file.
     *
     * @param players Collection of players, in the order in which they should be written into the property file.
     */
    public void write(final Collection<Player> players) {
        final Properties props = new Properties();
        players.stream().forEach(player -> {
            final ReleaseId id = player.getStrategyReleaseId();
            final String result = id.getGroupId() + PlayerProperties.GAV_PARTS_SEPARATOR + id.getArtifactId() +
                    PlayerProperties.GAV_PARTS_SEPARATOR + id.getVersion();
            props.setProperty(player.getName(), result);
        });
        try (final BufferedWriter fw = new BufferedWriter(new FileWriter(this.config))) {
            props.store(fw, Instant.now().toString());
        } catch (final Exception e) {
            throw new IllegalArgumentException("Cannot write player config file.", e);
        }
    }
}

package org.drooms.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.drooms.api.Game;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.DefaultPlayground;
import org.drooms.impl.DroomsTournament;

/**
 * Prepares the tournament properties by reading them from a property file.
 * 
 * <p>
 * Mandatory properties are:
 * </p>
 * 
 * <dl>
 * <dt>game.class</dt>
 * <dd>Which {@link Game} implementation the {@link DroomsTournament} should
 * use. Fully qualified name.</dd>
 * <dt>players</dt>
 * <dd>Path to the file containing descriptions of the {@link Player}s that will
 * participate in the tournament. See {@link PlayerAssembly}. Relative to the
 * current working directory.</dd>
 * <dt>playgrounds</dt>
 * <dd>Comma-separated list of playground on which to play the tournament.</dd>
 * </dl>
 * 
 * <p>
 * Optional properties are:
 * </p>
 * 
 * <dl>
 * <dt>runs</dt>
 * <dd>How many times should each playground be played. Number > 0, default
 * value is 1.</dd>
 * <dt>folder.resources</dt>
 * <dd>Where to load all input files from, relative to the current working
 * directory. If it doesn't exist, it is created. Default value is
 * "src/main/resources".</dd>
 * <dt>folder.target</dt>
 * <dd>Where to write all output data, relative to the current working
 * directory. If it doesn't exist, it is created. Default value is
 * "target/drooms".</dd>
 * </dl>
 * 
 * FIXME document player config file format.
 * 
 * FIXME document playgrounds dependencies.
 */
public class TournamentProperties {

    @SuppressWarnings("unchecked")
    private static Class<? extends Game> getGameImpl(final String id) {
        try {
            return (Class<? extends Game>) Class.forName(id);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot instantiate game class.", e);
        }
    }

    private static String getMandatoryProperty(final Properties p, final String key) {
        final String value = p.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Mandatory property not found: " + key);
        }
        return value;
    }

    private static String getOptionalProperty(final Properties p, final String key, final String defaultValue) {
        return p.getProperty(key, defaultValue);
    }

    private static Playground loadPlayground(final File source, final String name) {
        final File playgroundFile = new File(source, name + ".playground");
        try (InputStream is = new FileInputStream(playgroundFile)) {
            return DefaultPlayground.read(name, is);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot read playground file " + playgroundFile, e);
        }
    }

    private static Properties loadPlaygroundProperties(final File source, final String name) {
        final File propsFile = new File(source, name + ".cfg");
        final Properties gameProps = TournamentProperties.loadPropertiesFromFile(propsFile);
        if (gameProps == null) {
            throw new IllegalStateException("Failed reading game config file for playground: " + name);
        }
        return gameProps;
    }

    private static Properties loadPropertiesFromFile(final File f) {
        try (InputStream is = new FileInputStream(f)) {
            final Properties props = new Properties();
            props.load(is);
            return props;
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed reading properties from file: " + f);
        }
    }

    public static TournamentProperties read(final File f) {
        return new TournamentProperties(TournamentProperties.loadPropertiesFromFile(f));
    }

    private final Class<? extends Game> gameClass;
    private final File resourceFolder;
    private final File targetFolder;
    private final Collection<ImmutablePair<Playground, Properties>> playgrounds;

    private final int numberOfRunsPerPlayground;

    private final Collection<Player> players;

    private TournamentProperties(final Properties p) {
        this.gameClass = TournamentProperties.getGameImpl(TournamentProperties.getMandatoryProperty(p, "game.class"));
        this.numberOfRunsPerPlayground = Integer.valueOf(TournamentProperties.getOptionalProperty(p, "runs", "1"));
        // prepare folders
        this.resourceFolder = new File(TournamentProperties.getOptionalProperty(p, "folder.resources",
                "src/main/resources"));
        if (!this.resourceFolder.exists()) {
            this.resourceFolder.mkdirs();
        }
        this.targetFolder = new File(TournamentProperties.getOptionalProperty(p, "folder.target", "target/drooms"));
        if (!this.targetFolder.exists()) {
            this.targetFolder.mkdirs();
        }
        // prepare a list of players
        final File playerConfigFile = new File(TournamentProperties.getMandatoryProperty(p, "players"));
        this.players = Collections.unmodifiableList(new PlayerAssembly(playerConfigFile).assemblePlayers());
        // parse the playgrounds
        final Collection<ImmutablePair<Playground, Properties>> playgrounds = new ArrayList<>();
        for (final String playgroundName : TournamentProperties.getMandatoryProperty(p, "playgrounds").split("\\Q,\\E")) {
            final Playground playground = TournamentProperties.loadPlayground(this.resourceFolder, playgroundName);
            final Properties props = TournamentProperties.loadPlaygroundProperties(this.resourceFolder, playgroundName);
            playgrounds.add(new ImmutablePair<Playground, Properties>(playground, props));
        }
        this.playgrounds = Collections.unmodifiableCollection(playgrounds);
    }

    public Class<? extends Game> getGameClass() {
        return this.gameClass;
    }

    public int getNumberOfRunsPerPlayground() {
        return this.numberOfRunsPerPlayground;
    }

    public Collection<Player> getPlayers() {
        return this.players;
    }

    public Collection<ImmutablePair<Playground, Properties>> getPlaygrounds() {
        return this.playgrounds;
    }

    public File getResourceFolder() {
        return this.resourceFolder;
    }

    public File getTargetFolder() {
        return this.targetFolder;
    }

}

package org.drooms.launcher.tournament;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.drooms.api.Game;
import org.drooms.api.Player;
import org.drooms.impl.util.PlayerAssembly;
import org.drooms.util.CommonProperties;

/**
 * Prepares the tournament properties by reading them from a property file.
 * 
 * <p>
 * Mandatory properties are:
 * </p>
 * 
 * <dl>
 * <dt>game.class</dt>
 * <dd>Which {@link Game} implementation the {@link DroomsTournament} should use. Fully qualified name.</dd>
 * <dt>players</dt>
 * <dd>Path to the file containing descriptions of the {@link Player}s that will participate in the tournament. See
 * {@link PlayerAssembly}. Relative to the current working directory.</dd>
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
 * <dd>How many times should each playground be played. Number > 0, default value is 1.</dd>
 * <dt>folder.resources</dt>
 * <dd>Where to load all input files from, relative to the current working directory. If it doesn't exist, it is
 * created. Default value is "src/main/resources".</dd>
 * <dt>folder.target</dt>
 * <dd>Where to write all output data, relative to the current working directory. If it doesn't exist, it is created.
 * Default value is "target/drooms".</dd>
 * </dl>
 * 
 * FIXME document player config file format.
 */
public class TournamentProperties extends CommonProperties {

    @SuppressWarnings("unchecked")
    private static Class<? extends Game> getGameImpl(final String id) {
        try {
            return (Class<? extends Game>) Class.forName(id);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot instantiate game class.", e);
        }
    }

    public static TournamentProperties read(final File f) {
        return new TournamentProperties(CommonProperties.loadPropertiesFromFile(f));
    }

    private final Class<? extends Game> gameClass;
    private final File resourceFolder;
    private final File targetFolder;
    private final Collection<ImmutablePair<File, File>> playgrounds;

    private final int numberOfRunsPerPlayground;

    private final Collection<Player> players;

    private TournamentProperties(final Properties p) {
        super(p);
        this.gameClass = TournamentProperties.getGameImpl(this.getMandatoryProperty("game.class"));
        this.numberOfRunsPerPlayground = Integer.valueOf(this.getOptionalProperty("runs", "1"));
        // prepare folders
        this.resourceFolder = new File(this.getOptionalProperty("folder.resources", "src/main/resources"));
        if (!this.resourceFolder.exists()) {
            this.resourceFolder.mkdirs();
        }
        this.targetFolder = new File(this.getOptionalProperty("folder.target", "target/drooms"));
        if (!this.targetFolder.exists()) {
            this.targetFolder.mkdirs();
        }
        // prepare a list of players
        final File playerConfigFile = new File(this.resourceFolder, this.getMandatoryProperty("players"));
        this.players = Collections.unmodifiableList(new PlayerAssembly(playerConfigFile).assemblePlayers());
        // parse the playgrounds
        final Collection<ImmutablePair<File, File>> playgrounds = new ArrayList<>();
        for (final String playgroundName : this.getMandatoryProperty("playgrounds").split("\\Q,\\E")) {
            final File playground = new File(this.resourceFolder, playgroundName + ".playground");
            final File config = new File(this.resourceFolder, playgroundName + ".cfg");
            playgrounds.add(new ImmutablePair<File, File>(playground, config));
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

    public Collection<ImmutablePair<File, File>> getPlaygrounds() {
        return this.playgrounds;
    }

    public File getResourceFolder() {
        return this.resourceFolder;
    }

    public File getTargetFolder() {
        return this.targetFolder;
    }

}

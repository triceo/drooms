package org.drooms.impl.util;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drooms.impl.GameController;

/**
 * Command-line interface for the application. It enforces two options on the
 * command line:
 * 
 * <dl>
 * <dt>-p &lt;file&gt;</dt>
 * <dd>Provides a player configuration file, as described in
 * {@link GameController#play(String, java.util.Properties, java.util.Properties)}
 * .</dd>
 * <dt>-g &lt;file&gt;</dt>
 * <dd>Provides a game configuration file, as described in
 * {@link GameController#play(String, java.util.Properties, java.util.Properties)}
 * .</dd>
 * </dl>
 * 
 * Not providing any of those or pointing to unreadable (non-existent) files
 * should result in a help message being printed out and the application being
 * terminated.
 */
public class CLI {

    private static final CLI INSTANCE = new CLI();

    /**
     * Return the single instance of this class.
     * 
     * @return The instance.
     */
    public static CLI getInstance() {
        return CLI.INSTANCE;
    }

    private final Options options = new Options();

    private final Option players = new Option("p", "players", true,
            "A path to the player config file.");
    private final Option game = new Option("g", "game", true,
            "A path to the game config file.");

    private String errorMessage = null;
    private boolean isError = false;

    public String datasetLocation = null, solutionLocation = null;
    public long solverSeed = -1;

    /**
     * The constructor is hidden, as should be with the singleton pattern.
     */
    private CLI() {
        this.game.setRequired(true);
        this.options.addOption(this.game);
        this.players.setRequired(true);
        this.options.addOption(this.players);
    }

    /**
     * Prints a help message, describing the usage of the app from the
     * command-line.
     */
    public void printHelp() {
        if (this.isError) {
            System.out.println(this.errorMessage);
        }
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar drooms.jar", this.options, true);
    }

    /**
     * Process the command-line arguments.
     * 
     * @param args
     *            The arguments.
     * @return A pair of config files. Game config is the first, player config
     *         the second.
     */
    public Pair<File, File> process(final String[] args) {
        final CommandLineParser parser = new GnuParser();
        try {
            final CommandLine cli = parser.parse(this.options, args);
            final File gameConfig = new File(cli.getOptionValue(this.game
                    .getOpt()));
            if (!gameConfig.exists() || !gameConfig.canRead()) {
                this.setError("Provided game config file cannot be read!");
                return null;
            }
            final File playerConfig = new File(cli.getOptionValue(this.players
                    .getOpt()));
            if (!playerConfig.exists() || !playerConfig.canRead()) {
                this.setError("Provided player config file cannot be read!");
                return null;
            }
            return ImmutablePair.of(gameConfig, playerConfig);
        } catch (final ParseException e) {
            this.setError(e.getMessage());
            return null;
        }
    }

    private boolean setError(final String message) {
        if (!this.isError) {
            this.isError = true;
            this.errorMessage = message;
            return true;
        }
        return false;
    }

}

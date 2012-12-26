package org.drooms.impl.util;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.drooms.api.Playground;
import org.drooms.impl.GameController;

/**
 * Command-line interface for the application. It enforces following options on
 * the command line:
 * 
 * <dl>
 * <dt>-s &lt;scenario&gt;</dt>
 * <dd>Provides a {@link Playground} description on which the game is to be
 * played out..</dd>
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
public class GameCLI {

    private static final GameCLI INSTANCE = new GameCLI();

    /**
     * Return the single instance of this class.
     * 
     * @return The instance.
     */
    public static GameCLI getInstance() {
        return GameCLI.INSTANCE;
    }

    private final Options options = new Options();

    private final Option reports = new Option("r", "reports", true,
            "A folder to store reports in.");
    private final Option playground = new Option("s", "scenario", true,
            "A path to the playground config file.");
    private final Option players = new Option("p", "players", true,
            "A path to the player config file.");
    private final Option game = new Option("g", "game", true,
            "A path to the game config file.");

    private String errorMessage = null;
    private boolean isError = false;

    /**
     * The constructor is hidden, as should be with the singleton pattern.
     */
    private GameCLI() {
        this.options.addOption(this.reports);
        this.playground.setRequired(true);
        this.options.addOption(this.playground);
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
     * @return Files passed from the command line. First is the scenario, second
     *         is the game config, third is the player config, fourth
     *         (optionally) the directory in which to store reports.
     */
    public File[] process(final String[] args) {
        this.isError = false;
        final CommandLineParser parser = new GnuParser();
        try {
            final CommandLine cli = parser.parse(this.options, args);
            final File scenario = new File(cli.getOptionValue(this.playground
                    .getOpt()));
            if (!scenario.exists() || !scenario.canRead()) {
                this.setError("Provided scenario file cannot be read!");
                return null;
            }
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
            final String reports = cli.getOptionValue(this.reports.getOpt());
            if (reports == null) {
                return new File[] { scenario, gameConfig, playerConfig };
            } else {
                final File reportsDir = new File(reports + File.separator);
                reportsDir.mkdirs();
                return new File[] { scenario, gameConfig, playerConfig,
                        reportsDir };
            }
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

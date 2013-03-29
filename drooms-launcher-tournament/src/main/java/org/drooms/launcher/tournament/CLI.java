package org.drooms.launcher.tournament;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Command-line interface for the application. It enforces following options on
 * the command line:
 * 
 * <dl>
 * <dt>-t &lt;file&gt;</dt>
 * <dd>Provides a tournament configuration file, as described in
 * {@link DroomsTournament}.</dd>
 * </dl>
 * 
 * Not providing any of those or pointing to unreadable (non-existent) files
 * should result in a help message being printed out and the application being
 * terminated.
 */
class CLI {

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

    private final Option game = new Option("t", "tournament", true, "A path to the tournament config file.");

    private String errorMessage = null;
    private boolean isError = false;

    /**
     * The constructor is hidden, as should be with the singleton pattern.
     */
    private CLI() {
        this.game.setRequired(true);
        this.options.addOption(this.game);
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
     * @return The tournament config to read.
     */
    public File process(final String[] args) {
        this.isError = false;
        final CommandLineParser parser = new GnuParser();
        try {
            final CommandLine cli = parser.parse(this.options, args);
            final File gameConfig = new File(cli.getOptionValue(this.game.getOpt()));
            if (!gameConfig.exists() || !gameConfig.canRead()) {
                this.setError("Provided game config file cannot be read!");
                return null;
            } else {
                return gameConfig;
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

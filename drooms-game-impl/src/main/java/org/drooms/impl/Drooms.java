package org.drooms.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;
import org.drooms.api.Game;
import org.drooms.api.GameProgressListener;
import org.drooms.impl.util.CLI;

/**
 * Main class of the application, used to launch a particular game.
 */
public class Drooms {

    private static Game getGameImpl(final String id) {
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends Game> cls = (Class<? extends Game>) Class
                    .forName(id);
            return cls.newInstance();
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find game implementation: "
                    + id);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(
                    "Cannot instantiate game implementation: " + id, e);
        }
    }

    private static String getTimestamp() {
        final Date date = new java.util.Date();
        return new Timestamp(date.getTime()).toString();
    }

    /**
     * Run the application from the command-line. For a description of the
     * command line interface, see {@link CLI}.
     * 
     * This method expects couple properties to come out of
     * {@link CLI#process(String[])}'s game config {@link Properties}:
     * 
     * <dl>
     * <dt>game.class</dt>
     * <dd>A fully qualified name of a class on the classpath that will be used
     * as the game implementation. If not specified, {@link DefaultGame} will be
     * used.</dd>
     * <dt>reports.dir</dt>
     * <dd>A directory in which to write the report file of that game. If not
     * specified, it will be "reports". A subdirectory will be created, named as
     * a timestamp for the current game, and that is the effective target
     * directory for reports.</dd>
     * <dt>report.file</dt>
     * <dd>Where to write the report file at the end of that game. If not
     * specified, it will be "${reports.dir}/report.xml".</dd>
     * </dl>
     * 
     * @param args
     *            Command-line arguments.
     */
    public static void main(final String[] args) {
        final CLI cli = CLI.getInstance();
        final Pair<File, File> configs = cli.process(args);
        if (configs == null) {
            cli.printHelp();
            System.exit(-1);
        }
        // play the game
        GameProgressListener report = null;
        final Properties gameConfig = new Properties();
        try (Reader gameConfigFile = new FileReader(configs.getLeft());
                Reader playerConfigFile = new FileReader(configs.getRight())) {
            // prepare configs
            gameConfig.load(gameConfigFile);
            final Properties playerConfig = new Properties();
            playerConfig.load(playerConfigFile);
            // play and report
            final Game g = Drooms.getGameImpl(gameConfig.getProperty(
                    "game.class", "org.drooms.impl.DefaultGame"));
            report = g.play(Drooms.getTimestamp(), gameConfig, playerConfig);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed reading config files.", e);
        }
        // report
        final File reportFolder = report.getTargetFolder();
        try (Writer w = new FileWriter(new File(reportFolder,
                gameConfig.getProperty("report.file", "report.xml")))) {
            report.write(w);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed writing report file.", e);
        }
    }

}

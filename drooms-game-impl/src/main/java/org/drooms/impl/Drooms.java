package org.drooms.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import org.drooms.api.Game;
import org.drooms.api.GameProgressListener;
import org.drooms.impl.util.GameCLI;

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
     * command line interface, see {@link GameCLI}.
     * 
     * This method expects couple properties to come out of
     * {@link GameCLI#process(String[])}'s game config {@link Properties}:
     * 
     * <dl>
     * <dt>game.class</dt>
     * <dd>A fully qualified name of a class on the classpath that will be used
     * as the game implementation. If not specified, {@link DefaultGame} will be
     * used.</dd>
     * </dl>
     * 
     * @param args
     *            Command-line arguments.
     */
    public static void main(final String[] args) {
        final GameCLI cli = GameCLI.getInstance();
        final File[] configs = cli.process(args);
        if (configs == null) {
            cli.printHelp();
            System.exit(-1);
        }
        // play the game
        final File reportFolder = (configs.length == 4) ? configs[3]
                : new File("reports/");
        GameProgressListener report = null;
        final Properties gameConfig = new Properties();
        // FIXME standardize on InputStream or Reader
        try (InputStream playgroundFile = new FileInputStream(configs[0]);
                Reader gameConfigFile = new FileReader(configs[1]);
                Reader playerConfigFile = new FileReader(configs[2])) {
            // prepare configs
            gameConfig.load(gameConfigFile);
            final Properties playerConfig = new Properties();
            playerConfig.load(playerConfigFile);
            // play and report
            final Game g = Drooms.getGameImpl(gameConfig.getProperty(
                    "game.class", "org.drooms.impl.DefaultGame"));
            report = g.play(Drooms.getTimestamp(),
                    DefaultPlayground.read(playgroundFile), gameConfig,
                    playerConfig, reportFolder);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed reading config files.", e);
        }
        // report
        try (Writer w = new FileWriter(new File(reportFolder, "report.xml"))) {
            report.write(w);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed writing report file.", e);
        }
    }

}

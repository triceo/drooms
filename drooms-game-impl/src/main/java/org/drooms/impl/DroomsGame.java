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
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.drooms.api.Game;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.util.GameCLI;
import org.drooms.impl.util.PlayerAssembly;

/**
 * Main class of the application, used to launch a particular game.
 */
public class DroomsGame {

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
     * Run the game from the command-line. For a description of the command line
     * interface, see {@link GameCLI}.
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
            final DroomsGame d = new DroomsGame(configs[0].getName(),
                    DefaultPlayground.read(playgroundFile), new PlayerAssembly(
                            playerConfig).assemblePlayers(), gameConfig,
                    reportFolder);
            d.play();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed reading config files.", e);
        }
    }

    private final Playground p;
    private final String n;
    private final Properties c;
    private final Collection<Player> players;
    private final File f;

    public DroomsGame(final String name, final Playground p,
            final Collection<Player> players, final Properties gameConfig,
            final File reportFolder) {
        this.c = gameConfig;
        this.p = p;
        this.f = reportFolder;
        this.n = name;
        this.players = players;
    }

    public boolean play() {
        final Game g = DroomsGame.getGameImpl(this.c.getProperty("game.class",
                "org.drooms.impl.DefaultGame"));
        final File f = new File(this.f, this.n + "-"
                + DroomsGame.getTimestamp());
        if (!f.exists()) {
            f.mkdirs();
        }
        final GameProgressListener gpl = g
                .play(this.p, this.c, this.players, f);
        // report
        try (Writer w = new FileWriter(new File(f, "report.xml"))) {
            gpl.write(w);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

}
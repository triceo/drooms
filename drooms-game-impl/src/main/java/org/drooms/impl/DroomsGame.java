package org.drooms.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.drooms.api.Game;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.util.GameCLI;
import org.drooms.impl.util.GameProperties;
import org.drooms.impl.util.PlayerAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the application, used to launch a particular game.
 */
public class DroomsGame {

    private static String getTimestamp() {
        final Date date = new java.util.Date();
        return new Timestamp(date.getTime()).toString();
    }

    /**
     * Run the {@link DefaultGame} from the command-line. For a description of
     * the command line interface, see {@link GameCLI}.
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
        final File reportFolder = (configs.length == 4) ? configs[3] : new File("reports/");
        try (InputStream playgroundFile = new FileInputStream(configs[0]);) {
            // play and report
            // FIXME configs[0].getName() will return file name with extension
            final DroomsGame d = new DroomsGame(DefaultGame.class, DefaultPlayground.read(configs[0].getName(),
                    playgroundFile), new PlayerAssembly(configs[2]).assemblePlayers(), GameProperties.read(configs[1]),
                    reportFolder);
            d.play(configs[0].getName());
        } catch (final IOException e) {
            throw new IllegalStateException("Failed reading config files.", e);
        }
    }

    private final Playground p;
    private final GameProperties c;
    private final Collection<Player> players;
    private final File f;
    private final Class<? extends Game> cls;

    private static final Logger LOGGER = LoggerFactory.getLogger(DroomsGame.class);

    public DroomsGame(final Class<? extends Game> game, final Playground p, final List<Player> players,
            final GameProperties gameConfig, final File reportFolder) {
        this.c = gameConfig;
        this.p = p;
        this.f = reportFolder;
        this.cls = game;
        this.players = players;
    }

    public Map<Player, Integer> play(final String name) {
        Game g;
        try {
            g = this.cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            throw new IllegalStateException("Cannot find game class.", e1);
        }
        final File f = new File(this.f, name + "-" + DroomsGame.getTimestamp());
        if (!f.exists()) {
            f.mkdirs();
        }
        g.setContext(this.c);
        final Map<Player, Integer> result = g.play(this.p, this.players, f);
        // report
        try (Writer w = new FileWriter(new File(f, "report.xml"))) {
            g.getReport().write(w);
        } catch (final IOException e) {
            DroomsGame.LOGGER.info("Failed writing report for game: {}.", name);
        }
        return result;
    }

}
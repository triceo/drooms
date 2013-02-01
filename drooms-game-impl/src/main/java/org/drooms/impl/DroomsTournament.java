package org.drooms.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import org.drooms.api.Game;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.util.DroomsTournamentResults;
import org.drooms.impl.util.TournamentCLI;
import org.drooms.impl.util.TournamentProperties;
import org.drooms.impl.util.TournamentResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroomsTournament {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroomsTournament.class);

    private static String getTimestamp() {
        final Date date = new java.util.Date();
        return new Timestamp(date.getTime()).toString();
    }

    private static Properties loadFromFile(final File f) {
        try (InputStream is = new FileInputStream(f)) {
            final Properties props = new Properties();
            props.load(is);
            return props;
        } catch (final IOException e) {
            return null;
        }
    }

    public static void main(final String[] args) {
        // load the CLI
        final TournamentCLI cli = TournamentCLI.getInstance();
        final File config = cli.process(args);
        if (config == null) {
            cli.printHelp();
            System.exit(-1);
        }
        // load players
        final TournamentProperties props = TournamentProperties.read(config);
        if (props == null) {
            throw new IllegalStateException("Failed reading tournament config file.");
        }
        // load report folder
        final String id = DroomsTournament.getTimestamp();
        final File reports = new File("target/reports/tournaments/" + id);
        if (!reports.exists()) {
            reports.mkdirs();
        }
        // load game class
        final Class<? extends Game> game = props.getGameClass();
        final Collection<Player> players = props.getPlayers();
        // prepare a result tracker
        final TournamentResults result = new DroomsTournamentResults(id, players);
        // for each playground...
        for (final String playgroundName : props.getPlaygroundNames()) {
            // load playground
            final File playgroundFile = new File("src/main/resources", playgroundName + ".playground");
            Playground p = null;
            try (InputStream is = new FileInputStream(playgroundFile)) {
                p = DefaultPlayground.read(is);
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot read playground file " + playgroundFile, e);
            }
            // load game properties
            final File propsFile = new File("src/main/resources", playgroundName + ".cfg");
            final Properties gameProps = DroomsTournament.loadFromFile(propsFile);
            if (gameProps == null) {
                throw new IllegalStateException("Failed reading game config file for playgrond: " + playgroundName);
            }
            // run N games on the playground
            DroomsTournament.LOGGER.info("Starting games on playground {}.", playgroundName);
            for (int i = 1; i <= Integer.valueOf(props.getNumberOfRunsPerPlayground()); i++) {
                DroomsTournament.LOGGER.info("Starting game #{} on playground {}.", i, playgroundName);
                // randomize player order
                final List<Player> randomPlayers = new ArrayList<>(players);
                Collections.shuffle(randomPlayers);
                // play the game
                final DroomsGame dg = new DroomsGame(game, p, randomPlayers, gameProps, reports);
                result.addResults(playgroundName, dg.play(playgroundName + "_" + i));
            }
        }
        System.out.println("Tournament results:");
        int i = 1;
        for (final SortedMap.Entry<Long, Collection<Player>> entry : result.evaluate().entrySet()) {
            System.out.println("#" + i + " with " + entry.getKey() + " points: " + entry.getValue());
            i++;
        }
        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(reports, "report.html")))) {
            result.write(w);
        } catch (final IOException e) {
            // FIXME do something here
        }
    }

}
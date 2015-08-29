package org.drooms.impl;

import org.drooms.api.Game;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.util.DroomsStrategyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Convenient class used to create Drooms game based on the specified parameters.
 */
public class DroomsGame {

    private static String getTimestamp() {
        final Date date = new java.util.Date();
        return new Timestamp(date.getTime()).toString();
    }

    private final File p;
    private final File c;
    private final Collection<Player> players;
    private final File f;
    private final Class<? extends Game> cls;
    private final Set<GameProgressListener> listeners = new HashSet<GameProgressListener>();

    private static final Logger LOGGER = LoggerFactory.getLogger(DroomsGame.class);

    public DroomsGame(final Class<? extends Game> game, final File p, final List<Player> players,
            final File gameConfig, final File reportFolder) {
        this.c = gameConfig;
        this.p = p;
        this.f = reportFolder;
        this.cls = game;
        this.players = players;
        // validate players
        List<Player> invalidPlayers = this.players.stream().filter(player -> {
            boolean isInvalid = false;
            final DroomsStrategyValidator validator = new DroomsStrategyValidator(player.getStrategyReleaseId());
            if (!validator.isValid()) {
                isInvalid = true;
                DroomsGame.LOGGER.error("Player {} has malformed strategy:", player.getName());
                validator.getErrors().forEach(message -> DroomsGame.LOGGER.error("    {}", message));
            } else if (!validator.isClean()) {
                DroomsGame.LOGGER.warn("Player {} has an incomplete strategy:", player.getName());
                validator.getWarnings().forEach(message -> DroomsGame.LOGGER.warn("    {}", message));
            }
            return isInvalid;
        }).collect(Collectors.toList());
        if (invalidPlayers.size() > 0) {
            throw new IllegalArgumentException("Some players have invalid strategies. Check log for details.");
        }
    }

    public boolean addListener(final GameProgressListener listener) {
        return this.listeners.add(listener);
    }

    public Playground getPlayground() {
        try (InputStream is = new FileInputStream(this.p)) {
            return this.cls.newInstance().buildPlayground(this.p.getName(), is);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot find game class.", e);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot read playground.", e);
        }
    }

    public Map<Player, Integer> play(final String name) {
        final File f = new File(this.f, name + "-" + DroomsGame.getTimestamp());
        if (!f.exists()) {
            f.mkdirs();
        }
        try (InputStream contextFis = new FileInputStream(this.c);
                InputStream playgroundFis = new FileInputStream(this.p)) {
            final Game g = this.cls.newInstance();
            g.setContext(contextFis);
            for (final GameProgressListener listener : this.listeners) {
                g.addListener(listener);
            }
            final Map<Player, Integer> result = g.play(g.buildPlayground(name, playgroundFis), this.players, f);
            // report
            try (Writer w = new FileWriter(new File(f, "report.xml"))) {
                g.getReport().write(w);
            } catch (final IOException e) {
                DroomsGame.LOGGER.info("Failed writing report for game: {}.", name);
            }
            return result;
        } catch (final Exception ex) {
            throw new IllegalStateException("Cannot play the game.", ex);
        }
    }

    public boolean removeListener(final GameProgressListener listener) {
        return this.listeners.remove(listener);
    }
}

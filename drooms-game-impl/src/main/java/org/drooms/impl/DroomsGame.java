package org.drooms.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Game;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    public boolean addListener(final GameProgressListener listener) {
        return this.listeners.add(listener);
    }

    public Playground getPlayground() {
        Game g;
        try {
            g = this.cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            throw new IllegalStateException("Cannot find game class.", e1);
        }
        return g.buildPlayground(p.getName(), this.p);
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
        try (FileInputStream fis = new FileInputStream(this.c)) {
            g.setContext(fis);
        } catch (final Exception ex) {
            throw new IllegalStateException("Cannot read game properties from " + this.c);
        }
        for (final GameProgressListener listener : this.listeners) {
            g.addListener(listener);
        }
        final Map<Player, Integer> result = g.play(g.buildPlayground(name, this.p), this.players, f);
        // report
        try (Writer w = new FileWriter(new File(f, "report.xml"))) {
            g.getReport().write(w);
        } catch (final IOException e) {
            DroomsGame.LOGGER.info("Failed writing report for game: {}.", name);
        }
        return result;
    }

    public boolean removeListener(final GameProgressListener listener) {
        return this.listeners.remove(listener);
    }
}

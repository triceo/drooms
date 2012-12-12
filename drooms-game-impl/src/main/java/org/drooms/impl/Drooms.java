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

import org.drooms.api.GameReport;

public class Drooms {

    private static String getTimestamp() {
        final Date date = new java.util.Date();
        return new Timestamp(date.getTime()).toString();
    }

    public static void main(final String[] args) {
        GameReport report = null;
        final Properties gameConfig = new Properties();
        final Properties playerConfig = new Properties();
        // play the game
        try (Reader gameConfigFile = new FileReader(args[0]);
                Reader playerConfigFile = new FileReader(args[1])) {
            // prepare configs
            gameConfig.load(gameConfigFile);
            playerConfig.load(playerConfigFile);
            // play and report
            final DefaultGame g = new DefaultGame();
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

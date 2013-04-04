package org.drooms.launcher.game;

import java.io.File;

import org.drooms.impl.DefaultGame;
import org.drooms.impl.DroomsGame;
import org.drooms.impl.util.PlayerAssembly;

/**
 * Main class of the application, used to launch a particular game.
 */
public class DroomsGameLauncher {

    /**
     * Run the {@link DefaultGame} from the command-line. For a description of
     * the command line interface, see {@link CLI}.
     * 
     * @param args
     *            Command-line arguments.
     */
    public static void main(final String[] args) {
        final CLI cli = CLI.getInstance();
        final File[] configs = cli.process(args);
        if (configs == null) {
            cli.printHelp();
            System.exit(-1);
        }
        // play the game
        final File reportFolder = (configs.length == 4) ? configs[3] : new File("reports/");
        final DroomsGame d = new DroomsGame(DefaultGame.class, configs[0], new PlayerAssembly(configs[2]).assemblePlayers(), configs[1], reportFolder);
        d.play(configs[0].getName());
    }
}

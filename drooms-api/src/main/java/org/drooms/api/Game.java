package org.drooms.api;

import java.util.Properties;

public interface Game {

    public GameReport play(Properties config, Properties players);

}

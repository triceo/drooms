package org.drooms.api;

import java.util.Properties;

public interface Game {

    public GameReport play(String id, Properties config, Properties players);

}

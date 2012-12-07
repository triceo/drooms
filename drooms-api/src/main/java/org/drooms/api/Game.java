package org.drooms.api;

import java.util.Properties;

public interface Game<P extends Playground> {

    public GameReport<P> play(Properties config, Properties players);

}

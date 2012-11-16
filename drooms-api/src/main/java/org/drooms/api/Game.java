package org.drooms.api;

import java.util.Properties;

public interface Game<P extends Playground<N, E>, N extends Node, E extends Edge<N>> {

    public GameReport<P, N, E> play(Properties config, Properties players);

}

package org.drooms.api;

import java.util.Properties;

public interface Game<S extends Situation<P, N, E>, P extends Playground<N, E>, N extends Node, E extends Edge<N>> {

    public GameReport<S, P, N, E> play(Properties config, Properties players);

}

package org.drooms.api;

import java.util.List;

import edu.uci.ics.jung.graph.Graph;

public interface Playground<N extends Node, E extends Edge<N>> {

    public Graph<N, E> getGraph();

    public int getHeight();

    public List<E> getShortestPath(final N start, final N end);

    public List<N> getStartingPositions();

    public int getWidth();

    public boolean isAvailable(int x, int y);
    
    public N getNode(int x, int y);

}

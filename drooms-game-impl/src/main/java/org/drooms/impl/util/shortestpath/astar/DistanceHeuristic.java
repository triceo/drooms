package org.drooms.impl.util.shortestpath.astar;

import org.drooms.api.Node;

public interface DistanceHeuristic<V extends Node> {

    public double estimateDistance(V source, V target);

}

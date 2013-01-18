package org.drooms.impl.util.shortestpath.astar;

import org.drooms.api.Node;

class ChebyshevDistanceHeuristic<V extends Node> implements
        DistanceHeuristic<V> {

    @Override
    public double estimateDistance(final V source, final V target) {
        final int xDistance = Math.abs(source.getX() - target.getX());
        final int yDistance = Math.abs(source.getY() - target.getY());
        return Math.max(xDistance, yDistance);
    }

}

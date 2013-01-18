package org.drooms.impl.util.shortestpath.astar;

import org.drooms.api.Node;

class EuclideanDistanceHeuristic<V extends Node> implements
        DistanceHeuristic<V> {

    @Override
    public double estimateDistance(final V source, final V target) {
        final double xDistance = Math.pow(source.getX() - target.getX(), 2);
        final double yDistance = Math.pow(source.getY() - target.getY(), 2);
        return Math.sqrt(xDistance + yDistance);
    }

}

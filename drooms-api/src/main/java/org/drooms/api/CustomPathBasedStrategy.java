package org.drooms.api;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.Graph;

/**
 * A strategy that provides its own path-finding algorithm to be used by the
 * game. The time that this path-finding algorithm takes will be counted into
 * the total decision run time limit.
 */
public interface CustomPathBasedStrategy extends Strategy {

    /**
     * Return a fresh instance of the path-finding algorithm to find paths
     * between nodes in a given graph.
     * 
     * @param graph
     *            The graph which the algo should operate on. This graph only
     *            contains edges between nodes where the worm can move. Nodes
     *            where there are walls, or nodes where there's other worms, are
     *            not included in this graph.
     * @return The algorithm to be used for
     *         {@link ShortestPathUtils#getPath(Graph, ShortestPath, Object, Object)}
     *         .
     */
    public ShortestPath<Node, Edge> getShortestPathAlgorithm(
            final Graph<Node, Edge> graph);

}

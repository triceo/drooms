package org.drooms.api;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;

/**
 * A strategy that provides its own path-finding algorithm to be used by the
 * game. The time that this path-finding algorithm takes will be counted into
 * the total decision run time limit.
 * 
 * There are various available options for your strategies to choose from:
 * 
 * <dl>
 * <dt>{@link UnweightedShortestPath} from the JUNG library.</dt>
 * <dd>The default.</dd>
 * <dt>UnweightedAStarShortestPath from the game implementation package.</dt>
 * <dd>This is an experimental implementation of an algorithm that is supposed
 * to be the best.</dd>
 * <dt>Your own.</dt>
 * <dd>Just make sure that when no path is found, the implementation returns
 * empty collection. {@link DijkstraShortestPath} throws exception in such case
 * and that will mess up your rules.</dd>
 * </dl>
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

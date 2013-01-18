package org.drooms.impl.util.shortestpath.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.drooms.api.Edge;
import org.drooms.api.Node;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.graph.Graph;

public class UnweightedAStarShortestPath<V extends Node, E extends Edge>
        implements ShortestPath<V, E> {

    public enum VertexDistanceHeuristics {

        MANHATTAN, EUCLIDEAN, CHEBYSHEV;
    }

    private static final int DISTANCE_BETWEEN_NEIGHBORS = 1;

    private final Graph<V, E> graph;

    private final Map<V, AStarNode<V>> nodeCache = new HashMap<>();

    private final DistanceHeuristic<V> heuristics;

    private final Map<V, Map<V, List<V>>> shortestPaths = new HashMap<>();

    public UnweightedAStarShortestPath(final Graph<V, E> graph,
            final VertexDistanceHeuristics heuristicType) {
        this.graph = graph;
        switch (heuristicType) {
            case MANHATTAN:
                this.heuristics = new ManhattanDistanceHeuristic<V>();
                break;
            case EUCLIDEAN:
                this.heuristics = new EuclideanDistanceHeuristic<V>();
                break;
            case CHEBYSHEV:
                this.heuristics = new ChebyshevDistanceHeuristic<V>();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown distance heuristic: " + heuristicType);
        }
    }

    public List<V> find(final V source, final V target) {
        // prepare the data structures
        final Set<V> closedSet = new HashSet<>();
        final SortedSet<AStarNode<V>> openSet = new TreeSet<>();
        openSet.add(this.getAStarNode(
                source,
                new AStarNode<V>(source, 0, this.getHeuristicCostEstimate(
                        source, target))));
        final Map<AStarNode<V>, AStarNode<V>> cameFrom = new HashMap<>();
        // start the lookup
        while (!openSet.isEmpty()) {
            final AStarNode<V> current = openSet.first();
            if (current.getNode() == target) {
                return this.reconstructPath(cameFrom, current);
            }
            openSet.remove(current);
            closedSet.add(current.getNode());
            for (final V neighborNode : this.graph.getNeighbors(current
                    .getNode())) {
                if (closedSet.contains(neighborNode)) {
                    continue;
                }
                final double tentativeGScore = current.getG()
                        + UnweightedAStarShortestPath.DISTANCE_BETWEEN_NEIGHBORS;
                final AStarNode<V> neighbor = this.getAStarNode(neighborNode);
                final boolean isNeighborOpen = openSet.contains(neighbor);
                if (!isNeighborOpen || tentativeGScore <= neighbor.getG()) {
                    cameFrom.put(neighbor, current);
                    neighbor.setG(tentativeGScore);
                    neighbor.setF(tentativeGScore
                            + this.getHeuristicCostEstimate(neighbor.getNode(),
                                    target));
                    if (!isNeighborOpen) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<V>());
    }

    private AStarNode<V> getAStarNode(final V node) {
        return this.getAStarNode(node, new AStarNode<V>(node, 0, 0));
    }

    private AStarNode<V> getAStarNode(final V node,
            final AStarNode<V> returnIfNonexistent) {
        if (!this.nodeCache.containsKey(node)) {
            this.nodeCache.put(node, returnIfNonexistent);
        }
        return this.nodeCache.get(node);
    }

    private double getHeuristicCostEstimate(final V source, final V target) {
        return this.heuristics.estimateDistance(source, target);
    }

    @Override
    public Map<V, E> getIncomingEdgeMap(final V source) {
        if (!this.shortestPaths.containsKey(source)) {
            this.shortestPaths.put(source, new HashMap<V, List<V>>());
        }
        final Map<V, List<V>> relevantPaths = this.shortestPaths.get(source);
        final Map<V, E> result = new HashMap<>();
        for (final V target : this.graph.getVertices()) {
            if (target == source) {
                continue;
            }
            if (!relevantPaths.containsKey(target)) {
                relevantPaths.put(target, this.find(source, target));
            }
            final List<V> path = relevantPaths.get(target);
            if (path.size() == 0) {
                continue;
            }
            final V next = path.get(1);
            result.put(next, this.graph.findEdge(source, next));
        }
        return Collections.unmodifiableMap(result);
    }

    private List<V> reconstructPath(
            final Map<AStarNode<V>, AStarNode<V>> cameFrom,
            final AStarNode<V> currentNode) {
        if (cameFrom.containsKey(currentNode)) {
            final List<V> path = this.reconstructPath(cameFrom,
                    cameFrom.get(currentNode));
            path.add(currentNode.getNode());
            return path;
        } else {
            final List<V> result = new ArrayList<>();
            result.add(currentNode.getNode());
            return result;
        }
    }

}

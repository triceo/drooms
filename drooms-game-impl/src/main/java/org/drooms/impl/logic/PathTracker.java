package org.drooms.impl.logic;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Graphs;
import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;

import java.util.*;

/**
 * A helper class for the strategies to be able to quickly and easily find paths
 * from one {@link Node} to another.
 */
public class PathTracker {

    protected static <V, E> List<E> getPath(final Graph<V, E> graph, final V start, final Set<V> otherNodeSet) {
        return PathTracker.getPath(graph, start, otherNodeSet, new UnweightedShortestPath<>(graph));
    }

    // synchronize access to the shortest path algorithm, otherwise it breaks down horribly in parallel streams
    private static synchronized <V, E> List<E> getPath(final Graph<V, E> graph, final ShortestPath<V, E>
            shortestPathAlgorithm, final V start, final V end) {
        return ShortestPathUtils.getPath(graph, shortestPathAlgorithm, start, end);
    }

    /**
     * Finds the shortest path through given nodes. This method assumes that all nodes exist in the graph and will
     * not function properly otherwise.
     *
     * @param graph                 Graph to look inside of.
     * @param start                 The starting node for the path.
     * @param otherNodeSet          All the nodes to visit.
     * @param shortestPathAlgorithm The algorithm for constructing shortest path. Must be thread-safe.
     * @param <V>                   Type of node in the graph.
     * @param <E>                   Type of edge in the graph.
     * @return List of edges on the path, or empty if no path.
     */
    private static <V, E> List<E> getPath(final Graph<V, E> graph, final V start, final Set<V> otherNodeSet,
                                          final ShortestPath<V, E> shortestPathAlgorithm) {
        if (start == null || otherNodeSet == null || otherNodeSet.size() == 0) {
            throw new IllegalArgumentException("Please provide both a start node and a set of other nodes.");
        }
        /*
         * a brute-force algorithm to recursively find the shortest path. this algorithm picks all the other nodes,
         * one after another, and will call this algorithm again with the chosen node as the starting node and the
         * remaining nodes as the other nodes. it will try to be smart and not include among the other nodes the
         * nodes that it already went through earlier in the recursion.
         */
        final List<E> path = otherNodeSet.parallelStream().filter(node -> !node.equals(start)).map(newStart -> {
                // path to this node
                final List<E> totalPath = PathTracker.getPath(graph, shortestPathAlgorithm, start, newStart);
                // if any of the other nodes are in the above path already, don't go through them again
                final Set<V> nodesToTraverse = new LinkedHashSet<>(otherNodeSet);
                totalPath.forEach(edge -> nodesToTraverse.removeAll(graph.getIncidentVertices(edge)));
                if (nodesToTraverse.size() > 0) {
                    // find the new path and merge it
                    totalPath.addAll(PathTracker.getPath(graph, newStart, Collections.unmodifiableSet(nodesToTraverse),
                            shortestPathAlgorithm));
                }
                return totalPath;
            }).min((edges, edges2) -> {
                final int size1 = edges.size();
                final int size2 = edges2.size();
                if (size1 > size2) {
                    return 1;
                } else if (size1 == size2) {
                    return 0;
                } else {
                    return -1;
                }
            }).orElseGet(Collections::emptyList);
        return Collections.unmodifiableList(path);
    }

    /**
     * Create a clone of the original graph with certain nodes removed.
     *
     * @param src         Original graph.
     * @param removeNodes Nodes to remove from the original graph.
     * @return Original graph, except all the edges with specified incident nodes are removed.
     */
    private static UndirectedGraph<Node, Edge> cloneGraph(final Graph<Node, Edge> src,
                                                          final Collection<Node> removeNodes) {
        final UndirectedGraph<Node, Edge> clone = new UndirectedSparseGraph<>();
        src.getEdges().forEach(e -> {
            final boolean isEdgeAdded = clone.addEdge(e, src.getIncidentVertices(e));
            if (!isEdgeAdded) {
                throw new IllegalStateException("Failed cloning graph. This surely is a bug in Drooms.");
            }
        });
        removeNodes.forEach(node -> clone.removeVertex(node));
        return clone;
    }

    private Graph<Node, Edge> currentGraph;
    private ShortestPath<Node, Edge> currentPath;
    private Node currentPosition;

    private final Player player;
    private final Playground playground;

    /**
     * Initialize the class.
     *
     * @param playground The playground to base the path-finding algos on.
     * @param p          The player that will be using this particular instance.
     */
    public PathTracker(final Playground playground, final Player p) {
        this.playground = playground;
        this.player = p;
    }

    /**
     * Retrieve the current position of the player's worm's head, that is the
     * one found during the last {@link #updatePlayerPositions(Map)} call.
     *
     * @return The position, or null if {@link #updatePlayerPositions(Map)} had never been
     * called before.
     */
    public Node getCurrentPosition() {
        return this.currentPosition;
    }

    protected List<Edge> getPath(final Node start, final Set<Node> otherNodeSet) {
        if (!this.currentGraph.containsVertex(start)) {
            throw new IllegalArgumentException("Starting node not in the graph.");
        } else if (otherNodeSet.stream().filter(node -> this.currentGraph.containsVertex(node)).count() > 0) {
            throw new IllegalArgumentException("Some of the other nodes are not in the graph.");
        }
        return PathTracker.getPath(this.currentGraph, start, otherNodeSet, this.currentPath);
    }

    /**
     * Find the shortest path from the start node that leads through the other nodes regardless of their order.
     * <p>
     * This is effectively TSP, so try to keep the amount of nodes very, very small. :-)
     *
     * @param start      Beginning of the path.
     * @param otherNodes All the other nodes to go through. If this includes the start node, it will be ignored.
     * @return Unmodifiable list of nodes on the path, ordered from start to end. Empty if path cannot be found.
     */
    public List<Edge> getPath(final Node start, final Node... otherNodes) {
        return this.getPath(start, Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(otherNodes))));
    }

    public Player getPlayer() {
        return this.player;
    }

    public Playground getPlayground() {
        return this.playground;
    }

    /**
     * Update the internal state of this class so that future paths can avoid
     * places where the worms currently reside.
     *
     * @param newPositions New current positions of all the worms.
     */
    protected void updatePlayerPositions(final Map<Player, Deque<Node>> newPositions) {
        if (newPositions.isEmpty()) {
            this.currentGraph = Graphs.unmodifiableGraph(this.playground.getGraph());
        } else {
            // enumerate all the nodes occupied by worms at this point
            final Set<Node> unavailable = new LinkedHashSet<>();
            newPositions.forEach((player, nodes) -> unavailable.addAll(nodes));
            // make sure we keep the head node, since otherwise there is no path from the current position to any other
            this.currentPosition = newPositions.get(this.player).getFirst();
            unavailable.remove(this.currentPosition);
            // update internal structures
            this.currentGraph = Graphs.unmodifiableUndirectedGraph(PathTracker.cloneGraph(this.playground.getGraph(),
                    unavailable));
        }
        this.currentPath = new UnweightedShortestPath<>(this.currentGraph);
    }

}

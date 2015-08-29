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

    /**
     * Create a clone of the original graph with certain nodes removed.
     *
     * @param src Original graph.
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
     * @param playground
     *            The playground to base the path-finding algos on.
     * @param p
     *            The player that will be using this particular instance.
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
     *         called before.
     */
    public Node getCurrentPosition() {
        return this.currentPosition;
    }

    /**
     * Find the shortest path between two nodes.
     * 
     * @param start
     *            Beginning of the path.
     * @param end
     *            End of the path.
     * @return Unmodifiable list of nodes on the path, ordered from start to
     *         end. Empty if path cannot be found.
     */
    public List<Edge> getPath(final Node start, final Node end) {
        return Collections.unmodifiableList(ShortestPathUtils.getPath(this.currentGraph, this.currentPath, start, end));
    }

    /**
     * Find the shortest path from the start node that leads through the other
     * two nodes in any order.
     * 
     * @param start
     *            Beginning of the path.
     * @param node2
     *            Any of the other two nodes.
     * @param node3
     *            Any of the other two nodes.
     * @return Unmodifiable list of nodes on the path, ordered from start to
     *         end. Empty if path cannot be found.
     */
    public List<Edge> getPath(final Node start, final Node node2, final Node node3) {
        final List<Edge> path2 = this.getPath(start, node2);
        final List<Edge> path3 = this.getPath(start, node3);
        final List<Edge> path23 = new ArrayList<Edge>(this.getPath(node2, node3));
        final List<Edge> result = new ArrayList<Edge>();
        if (path2.size() > path3.size()) {
            result.addAll(path3);
            Collections.reverse(path23);
        } else {
            result.addAll(path2);
        }
        result.addAll(path23);
        return Collections.unmodifiableList(result);
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
     * @param newPositions
     *            New current positions of all the worms.
     */
    protected void updatePlayerPositions(final Map<Player, Deque<Node>> newPositions) {
        if (newPositions.isEmpty()) {
            this.currentGraph = Graphs.unmodifiableGraph(this.playground.getGraph());
        } else {
            // enumerate all the nodes occupied by worms at this point
            final Set<Node> unavailable = new HashSet<>();
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

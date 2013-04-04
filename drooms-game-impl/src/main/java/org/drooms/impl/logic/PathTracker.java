package org.drooms.impl.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Graphs;

/**
 * A helper class for the strategies to be able to quickly and easily find paths
 * from one {@link Node} to another.
 */
public class PathTracker {

    private static UndirectedGraph<Node, Edge> cloneGraph(final Graph<Node, Edge> src,
            final Collection<Node> removeNodes) {
        final UndirectedGraph<Node, Edge> clone = new UndirectedSparseGraph<>();
        for (final Edge e : src.getEdges()) {
            final boolean isEdgeAdded = clone.addEdge(e, src.getIncidentVertices(e));
            if (!isEdgeAdded) {
                throw new IllegalStateException("Failed cloning graph. This surely is a bug in Drooms.");
            }
        }
        for (final Node node : removeNodes) {
            clone.removeVertex(node);
        }
        return Graphs.unmodifiableUndirectedGraph(clone);
    }

    private final Playground playground;
    private final Player player;
    private UndirectedGraph<Node, Edge> currentGraph;

    private ShortestPath<Node, Edge> currentPath;
    private Node currentPosition;

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
     * one found during the last {@link #movePlayers(Map)} call.
     * 
     * @return The position, or null if {@link #movePlayers(Map)} had never been
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
    protected void movePlayers(final Map<Player, Deque<Node>> newPositions) {
        final Set<Node> unavailable = new HashSet<>();
        for (final Map.Entry<Player, Deque<Node>> entry : newPositions.entrySet()) {
            final Deque<Node> playerNodes = entry.getValue();
            unavailable.addAll(playerNodes);
            if (entry.getKey() == this.player) {
                /*
                 * the head node needs to remain, since otherwise there would be
                 * no path between the current position and any other position.
                 */
                unavailable.remove(playerNodes.getFirst());
            }
        }
        final Graph<Node, Edge> graphWithoutPlayers = this.playground.getGraph();
        this.currentGraph = PathTracker.cloneGraph(graphWithoutPlayers, unavailable);
        this.currentPath = this.player.getShortestPathAlgorithm(this.currentGraph);
        this.currentPosition = newPositions.get(this.player).getFirst();
    }

}

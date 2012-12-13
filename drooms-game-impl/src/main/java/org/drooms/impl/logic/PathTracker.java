package org.drooms.impl.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * A helper class for the strategies to be able to quickly and easily find paths
 * from one {@link Node} to another.
 */
public class PathTracker {

    private static Graph<Node, Edge> cloneGraph(final Graph<Node, Edge> src,
            final Collection<Node> removeNodes) {
        final Graph<Node, Edge> clone = new UndirectedSparseGraph<>();
        for (final Node v : src.getVertices()) {
            clone.addVertex(v);
        }
        for (final Edge e : src.getEdges()) {
            clone.addEdge(e, src.getIncidentVertices(e));
        }
        for (final Node node : removeNodes) {
            clone.removeVertex(node);
        }
        return clone;
    }

    private final Playground playground;
    private final Player player;
    private Graph<Node, Edge> currentGraph;

    private ShortestPath<Node, Edge> currentPath;

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
        return Collections.unmodifiableList(ShortestPathUtils.getPath(
                this.currentGraph, this.currentPath, start, end));
    }

    /**
     * Find the shortest path from the start node that leads through the other
     * two nodes in any order.
     * 
     * @param start
     *            Beginning of the path.
     * @param node1
     *            Any of the other two nodes.
     * @param node2
     *            Any of the other two nodes.
     * @return Unmodifiable list of nodes on the path, ordered from start to
     *         end. Empty if path cannot be found.
     */
    public List<Edge> getPath(final Node start, final Node node2,
            final Node node3) {
        final List<Edge> path2 = this.getPath(start, node2);
        final List<Edge> path3 = this.getPath(start, node3);
        final List<Edge> path23 = new ArrayList<Edge>(
                this.getPath(node2, node3));
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
    // TODO Strategies should never be able to call this method.
    protected void movePlayers(final Map<Player, Deque<Node>> newPositions) {
        final Set<Node> unavailable = new HashSet<>();
        for (final Map.Entry<Player, Deque<Node>> entry : newPositions
                .entrySet()) {
            if (entry.getKey() == this.player) {
                /*
                 * remove all the nodes occupied by the current player, except
                 * for the one node with the player's head. that node needs to
                 * remain, so that we can always calculate the path to other
                 * nodes.
                 */
                final Deque<Node> player = new LinkedList<>(entry.getValue());
                player.pop();
                unavailable.addAll(player);
            } else {
                // remove all the nodes occupied by other players
                unavailable.addAll(entry.getValue());
            }
        }
        final Graph<Node, Edge> graphWithoutPlayers = this.playground
                .getGraph();
        this.currentGraph = PathTracker.cloneGraph(graphWithoutPlayers,
                unavailable);
        this.currentPath = new UnweightedShortestPath<Node, Edge>(
                this.currentGraph);
    }

}

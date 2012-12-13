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

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

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

    public PathTracker(final Playground playground, final Player p) {
        this.playground = playground;
        this.player = p;
    }

    public List<Edge> getPath(final Node start, final Node end) {
        return ShortestPathUtils.getPath(this.currentGraph, this.currentPath,
                start, end);
    }

    public List<Edge> getPath(final Node start, final Node node2,
            final Node node3) {
        final List<Edge> path2 = this.getPath(start, node2);
        final List<Edge> path3 = this.getPath(start, node3);
        final List<Edge> path23 = this.getPath(node2, node3);
        final List<Edge> result = new ArrayList<Edge>();
        if (path2.size() > path3.size()) {
            result.addAll(path3);
            Collections.reverse(path23);
        } else {
            result.addAll(path2);
        }
        result.addAll(path23);
        return result;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Playground getPlayground() {
        return this.playground;
    }

    // FIXME Strategies should never be able to call this method.
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
        this.currentPath = new DijkstraShortestPath<Node, Edge>(
                this.currentGraph);
    }

}

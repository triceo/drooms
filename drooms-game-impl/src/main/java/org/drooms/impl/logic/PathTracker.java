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

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class PathTracker<P extends Playground<N, E>, N extends Node, E extends Edge<N>> {

    private final P playground;
    private Graph<N, E> currentGraph;
    private ShortestPath<N, E> currentPath;

    public PathTracker(final P playground, final Collection<Player> players) {
        this.playground = playground;
    }

    private Graph<N, E> cloneGraph(final Graph<N, E> src,
            final Collection<N> removeNodes) {
        final Graph<N, E> clone = new UndirectedSparseGraph<>();
        for (final N v : src.getVertices()) {
            clone.addVertex(v);
        }
        for (final E e : src.getEdges()) {
            clone.addEdge(e, src.getIncidentVertices(e));
        }
        for (final N node : removeNodes) {
            clone.removeVertex(node);
        }
        return clone;
    }

    public List<E> getPath(final N start, final N end) {
        return ShortestPathUtils.getPath(this.currentGraph, this.currentPath,
                start, end);
    }

    public List<E> getPath(final N start, final N node2, final N node3) {
        final List<E> path2 = this.getPath(start, node2);
        final List<E> path3 = this.getPath(start, node3);
        final List<E> path23 = this.getPath(node2, node3);
        final List<E> result = new ArrayList<E>();
        if (path2.size() > path3.size()) {
            result.addAll(path3);
            Collections.reverse(path23);
        } else {
            result.addAll(path2);
        }
        result.addAll(path23);
        return result;
    }

    public P getPlayground() {
        return this.playground;
    }

    // FIXME Strategies should never be able to call this method.
    protected void movePlayers(final Map<Player, Deque<N>> newPositions) {
        final Set<N> unavailable = new HashSet<N>();
        for (final Map.Entry<Player, Deque<N>> entry : newPositions.entrySet()) {
            unavailable.addAll(entry.getValue());
        }
        final Graph<N, E> graphWithoutPlayers = this.playground.getGraph();
        this.currentGraph = this.cloneGraph(graphWithoutPlayers, unavailable);
        this.currentPath = new DijkstraShortestPath<N, E>(this.currentGraph);
    }

}

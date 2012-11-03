package org.drooms.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.drooms.api.Playground;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Graphs;

public class DefaultPlayground implements Playground<DefaultNode, DefaultEdge> {

    private static final char WALL_SIGN = '#';

    public static DefaultPlayground read(final File f) throws IOException {
        return DefaultPlayground.read(new FileReader(f));
    }

    public static DefaultPlayground read(final InputStream s)
            throws IOException {
        return new DefaultPlayground(IOUtils.readLines(s));
    }

    public static DefaultPlayground read(final Reader r) throws IOException {
        return new DefaultPlayground(IOUtils.readLines(r));
    }

    private final Set<DefaultNode> nodes = new HashSet<DefaultNode>();

    private final List<DefaultNode[]> nodeLocations = new ArrayList<DefaultNode[]>();

    private static final DefaultNode WALL_NODE = new DefaultNode(-1, -1);

    private final Graph<DefaultNode, DefaultEdge> graph = new UndirectedSparseGraph<DefaultNode, DefaultEdge>();
    private final UnweightedShortestPath<DefaultNode, DefaultEdge> shortestPath;
    private final SortedMap<Character, DefaultNode> startingNodes = new TreeMap<Character, DefaultNode>();
    private final int width;

    private DefaultPlayground(final List<String> lines) {
        // assemble nodes
        int maxX = Integer.MIN_VALUE;
        for (final String line : lines) {
            int y = this.nodeLocations.size();
            final DefaultNode[] locations = new DefaultNode[line.length()];
            for (int x = 0; x < line.length(); x++) {
                final char nodeLabel = line.charAt(x);
                DefaultNode n;
                switch (nodeLabel) {
                    case WALL_SIGN: // wall node
                        n = DefaultPlayground.WALL_NODE;
                        break;
                    case ' ': // regular node
                        n = new DefaultNode(x, y);
                        break;
                    default: // starting point for a worm
                        n = new DefaultNode(x, y);
                        this.startingNodes.put(nodeLabel, n);
                        break;
                }
                this.nodes.add(n);
                locations[x] = n;
                maxX = Math.max(maxX, x);
            }
            this.nodeLocations.add(locations);
            y++;
        }
        this.width = maxX + 1;
        // link nodes
        for (final DefaultNode n : this.nodes) {
            if (n == DefaultPlayground.WALL_NODE) {
                // don't link wall node to any other node
                continue;
            }
            final int y = n.getY();
            final int x = n.getX();
            // link upwards
            if (y > 0) {
                this.link(x, y, x, y - 1);
            }
            // link downwards
            if (y < this.nodeLocations.size() - 1) {
                this.link(x, y, x, y + 1);
            }
            final DefaultNode[] nodes = this.nodeLocations.get(y);
            // link to the left
            if (x > 0) {
                this.link(x, y, x - 1, y);
            }
            // link to the right
            if (x < nodes.length - 1) {
                this.link(x, y, x + 1, y);
            }
        }
        this.shortestPath = new UnweightedShortestPath<>(this.graph);
    }

    @Override
    public Graph<DefaultNode, DefaultEdge> getGraph() {
        return Graphs.unmodifiableGraph(this.graph);
    }

    @Override
    public int getHeight() {
        return this.nodeLocations.size();
    }

    private DefaultNode getNode(final int x, final int y) {
        if (y < 0 || y >= this.nodeLocations.size()) {
            throw new IllegalArgumentException(
                    "There are no nodes with coordinates [x, " + y + "]");
        }
        final DefaultNode[] nodes = this.nodeLocations.get(y);
        if (x < 0 || x >= nodes.length) {
            throw new IllegalArgumentException(
                    "There are no nodes with coordinates [" + x + ", " + y
                            + "]");
        }
        return this.nodeLocations.get(y)[x];
    }

    @Override
    public List<DefaultEdge> getShortestPath(final DefaultNode start,
            final DefaultNode end) {
        return ShortestPathUtils.getPath(this.graph, this.shortestPath, start,
                end);
    }

    @Override
    public List<DefaultNode> getStartingPositions() {
        final List<DefaultNode> nodes = new ArrayList<DefaultNode>(
                this.startingNodes.values());
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public boolean isAvailable(final DefaultNode node) {
        return this.isAvailable(node.getX(), node.getY());
    }

    @Override
    public boolean isAvailable(final int x, final int y) {
        try {
            return (this.getNode(x, y) == DefaultPlayground.WALL_NODE) ? false
                    : true;
        } catch (final IllegalArgumentException ex) {
            return false;
        }
    }

    private DefaultEdge link(final int x, final int y, final int otherX,
            final int otherY) {
        final DefaultNode node1 = this.getNode(x, y);
        if (node1 == DefaultPlayground.WALL_NODE) {
            return null;
        }
        final DefaultNode node2 = this.getNode(otherX, otherY);
        if (node2 == DefaultPlayground.WALL_NODE) {
            return null;
        }
        DefaultEdge e = this.graph.findEdge(node1, node2);
        if (e == null) {
            e = new DefaultEdge(node1, node2);
            this.graph.addEdge(e, node1, node2);
        }
        return e;
    }

    public void write(final File f) throws IOException {
        this.write(new FileWriter(f));
    }

    public void write(final Writer w) throws IOException {
        final BufferedWriter bw = new BufferedWriter(w);
        for (final DefaultNode[] line : this.nodeLocations) {
            for (final DefaultNode n : line) {
                if (n == DefaultPlayground.WALL_NODE) {
                    bw.append(DefaultPlayground.WALL_SIGN);
                } else if (this.startingNodes.containsValue(n)) {
                    for (final SortedMap.Entry<Character, DefaultNode> entry : this.startingNodes
                            .entrySet()) {
                        if (!entry.getValue().equals(n)) {
                            continue;
                        }
                        bw.append(entry.getKey());
                        break;
                    }
                } else {
                    bw.append(' ');
                }
            }
            bw.newLine();
        }
        bw.close();
    }

}

package org.drooms.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.api.Playground;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Graphs;

public class DefaultPlayground implements Playground {

    private static final char WALL_SIGN = '#';

    /**
     * Build the playground from an input stream. Each line in that stream
     * represents one row on the playground. Each '#' in that line represents a
     * wall node, ' ' represents a node where the worm can move and any other
     * character represents a possible starting position for a worm. (Starting
     * positions also can be moved into.)
     * 
     * @param s
     *            Stream in question.
     * @return Playground constructed from that stream.
     * @throws IOException
     *             In case the stream cannot be read.
     */
    public static DefaultPlayground read(final InputStream s)
            throws IOException {
        return new DefaultPlayground(IOUtils.readLines(s));
    }

    private final Set<Node> nodes = new HashSet<Node>();

    private final List<Node[]> nodeLocations = new ArrayList<Node[]>();

    private static final Node WALL_NODE = Node.getNode(-1, -1);

    private final Graph<Node, Edge> graph = new UndirectedSparseGraph<Node, Edge>();
    private final SortedMap<Character, Node> startingNodes = new TreeMap<Character, Node>();
    private final int width;

    private DefaultPlayground(final List<String> lines) {
        // assemble nodes
        int maxX = Integer.MIN_VALUE;
        for (final String line : lines) {
            int y = this.nodeLocations.size();
            final Node[] locations = new Node[line.length()];
            for (int x = 0; x < line.length(); x++) {
                final char nodeLabel = line.charAt(x);
                Node n;
                switch (nodeLabel) {
                    case WALL_SIGN: // wall node
                        n = DefaultPlayground.WALL_NODE;
                        break;
                    case ' ': // regular node
                        n = Node.getNode(x, y);
                        break;
                    default: // starting point for a worm
                        n = Node.getNode(x, y);
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
        for (final Node n : this.nodes) {
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
            final Node[] nodes = this.nodeLocations.get(y);
            // link to the left
            if (x > 0) {
                this.link(x, y, x - 1, y);
            }
            // link to the right
            if (x < nodes.length - 1) {
                this.link(x, y, x + 1, y);
            }
        }
    }

    @Override
    public Graph<Node, Edge> getGraph() {
        return Graphs.unmodifiableGraph(this.graph);
    }

    @Override
    public int getHeight() {
        return this.nodeLocations.size();
    }

    private Node getNode(final int x, final int y) {
        if (y < 0 || y >= this.nodeLocations.size()) {
            throw new IllegalArgumentException(
                    "There are no nodes with coordinates [x, " + y + "]");
        }
        final Node[] nodes = this.nodeLocations.get(y);
        if (x < 0 || x >= nodes.length) {
            throw new IllegalArgumentException(
                    "There are no nodes with coordinates [" + x + ", " + y
                            + "]");
        }
        return this.nodeLocations.get(y)[x];
    }

    @Override
    public List<Node> getStartingPositions() {
        final List<Node> nodes = new ArrayList<Node>(
                this.startingNodes.values());
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public int getWidth() {
        return this.width;
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

    private Edge link(final int x, final int y, final int otherX,
            final int otherY) {
        if (!this.isAvailable(x, y) || !this.isAvailable(otherX, otherY)) {
            return null;
        }
        final Node node1 = this.getNode(x, y);
        final Node node2 = this.getNode(otherX, otherY);
        Edge e = this.graph.findEdge(node1, node2);
        if (e == null) {
            e = new Edge(node1, node2);
            this.graph.addEdge(e, node1, node2);
        }
        return e;
    }

    /**
     * Write out the playground into a stream, according to the spec described
     * in {@link #read(InputStream)}.
     * 
     * @param s
     *            The stream
     * @throws IOException
     *             In case the stream cannot be written.
     */
    public void write(final OutputStream s) throws IOException {
        try (final BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(s))) {
            for (final Node[] line : this.nodeLocations) {
                for (final Node n : line) {
                    if (n == DefaultPlayground.WALL_NODE) {
                        bw.append(DefaultPlayground.WALL_SIGN);
                    } else if (this.startingNodes.containsValue(n)) {
                        for (final SortedMap.Entry<Character, Node> entry : this.startingNodes
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
        }
    }

}

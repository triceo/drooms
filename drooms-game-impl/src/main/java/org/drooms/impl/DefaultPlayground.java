package org.drooms.impl;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Graphs;
import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.api.Node.Type;
import org.drooms.api.Playground;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

class DefaultPlayground implements Playground {

    private static final char WALL_SIGN = '#';
    private static final char PLAYER_SIGN = '@';

    private final Map<Node, Character> portals = new HashMap<>();

    private final List<Node[]> nodeLocations = new ArrayList<>();

    private final Graph<Node, Edge> graph = new UndirectedSparseGraph<>();
    private final List<Node> startingNodes = new ArrayList<>();
    private final int width;
    private final String name;

    DefaultPlayground(final String name, final List<String> lines) {
        this.name = name;
        // portal data
        final Map<Character, Node> portalEntries = new TreeMap<>();
        final Map<Character, Node> portalExits = new TreeMap<>();
        // assemble nodes
        int maxX = Integer.MIN_VALUE;
        final Collection<Node> identifiedNodes = new HashSet<>();
        for (final String line : lines) {
            int y = this.nodeLocations.size();
            final Node[] locations = new Node[line.length()];
            for (int x = 0; x < line.length(); x++) {
                final char nodeLabel = line.charAt(x);
                Node n;
                switch (nodeLabel) {
                    case WALL_SIGN: // wall node
                        n = new DefaultNode(Type.WALL, x, y);
                        break;
                    case PLAYER_SIGN: // player starting position
                        n = new DefaultNode(Type.STARTING_POSITION, x, y);
                        this.startingNodes.add(n);
                        break;
                    case ' ': // regular node
                        n = new DefaultNode(x, y);
                        break;
                    default: // any other character is a portal
                        n = new DefaultNode(Type.PORTAL, x, y);
                        if (portalEntries.containsKey(nodeLabel)) {
                            if (portalExits.containsKey(nodeLabel)) {
                                throw new IllegalStateException("Portal " + nodeLabel + " appears more than twice!");
                            } else {
                                portalExits.put(nodeLabel, n);
                            }
                        } else {
                            portalEntries.put(nodeLabel, n);
                        }
                }
                identifiedNodes.add(n);
                locations[x] = n;
                maxX = Math.max(maxX, x);
            }
            this.nodeLocations.add(locations);
            y++;
        }
        this.width = maxX + 1;
        // link nodes
        for (final Node n : identifiedNodes) {
            if (n.getType() == Type.WALL) {
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
        // link portals
        for (final Map.Entry<Character, Node> entries : portalEntries.entrySet()) {
            final Character key = entries.getKey();
            if (!portalExits.containsKey(key)) {
                throw new IllegalStateException("Portal " + key + " has no opposite end.");
            }
            final Node entry = entries.getValue();
            final Node exit = portalExits.get(key);
            this.link(entry.getX(), entry.getY(), exit.getX(), exit.getY());
            this.portals.put(entry, key);
            this.portals.put(exit, key);
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

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Node> getStartingPositions() {
        return Collections.unmodifiableList(this.startingNodes);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public boolean isAvailable(final int x, final int y) {
        return (this.getNodeAt(x, y).getType() == Type.WALL) ? false : true;
    }

    private Edge link(final int x, final int y, final int otherX, final int otherY) {
        if (!this.isAvailable(x, y) || !this.isAvailable(otherX, otherY)) {
            return null;
        }
        final Node node1 = this.getNodeAt(x, y);
        final Node node2 = this.getNodeAt(otherX, otherY);
        Edge e = this.graph.findEdge(node1, node2);
        if (e == null) {
            e = new DefaultEdge(node1, node2);
            this.graph.addEdge(e, node1, node2);
        }
        return e;
    }

    /**
     * Write out the playground into a stream.
     * 
     * @param s
     *            The stream
     * @throws IOException
     *             In case the stream cannot be written.
     */
    public void write(final OutputStream s) throws IOException {
        try (final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s, "UTF-8"))) {
            for (final Node[] line : this.nodeLocations) {
                for (final Node n : line) {
                    switch (n.getType()) {
                        case WALL:
                            bw.append(DefaultPlayground.WALL_SIGN);
                            break;
                        case STARTING_POSITION:
                            bw.append(DefaultPlayground.PLAYER_SIGN);
                            break;
                        case PORTAL:
                            bw.append(this.portals.get(n));
                            break;
                        default:
                            bw.append(' ');
                    }
                }
                bw.newLine();
            }
        }
    }

    @Override
    public Node getNodeAt(final int x, final int y) {
        if (y < 0 || y >= this.nodeLocations.size()) {
            return new DefaultNode(Type.WALL, x, y);
        }
        final Node[] nodes = this.nodeLocations.get(y);
        if (x < 0 || x >= nodes.length) {
            return new DefaultNode(Type.WALL, x, y);
        }
        return this.nodeLocations.get(y)[x];
    }

    @Override
    public Node getOtherEndOfPortal(final Node portal) {
        if (portal.getType() != Type.PORTAL) {
            throw new IllegalArgumentException("Node not a portal: " + portal);
        } else if (!this.portals.containsKey(portal)) {
            throw new IllegalArgumentException("Unknown portal:" + portal);
        }
        final Character portalId = this.portals.get(portal);
        final List<Node> possiblePortalEnds = this.portals.keySet().stream().filter(p -> portalId.equals(this.portals.get(p)))
                .filter(p -> !p.equals(portal)).collect(Collectors.toList());
        if (possiblePortalEnds.size() != 1) {
            throw new IllegalStateException("Cannot find portal. This should not be possible at this point.");
        } else {
            return possiblePortalEnds.get(0);
        }
    }

}

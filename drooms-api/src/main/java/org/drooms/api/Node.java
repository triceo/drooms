package org.drooms.api;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A single indivisible place on the {@link Playground}, where (part of) a worm
 * or a wall can be located.
 */
public class Node implements Comparable<Node> {

    private final int x, y;

    private static final SortedMap<Integer, SortedMap<Integer, Node>> nodes = new TreeMap<Integer, SortedMap<Integer, Node>>();

    /**
     * Get a node with particular co-ordinates.
     * 
     * @param x
     *            Horizontal co-ordinate.
     * @param y
     *            Vertical co-ordinate.
     * @return The node.
     */
    public static synchronized Node getNode(final int x, final int y) {
        if (!Node.nodes.containsKey(x)) {
            Node.nodes.put(x, new TreeMap<Integer, Node>());
        }
        final SortedMap<Integer, Node> ys = Node.nodes.get(x);
        if (!ys.containsKey(y)) {
            ys.put(y, new Node(x, y));
        }
        return ys.get(y);
    }

    private Node(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * A {@link Node} is considered larger than the other if it has a bigger
     * {@link #getY()}. In case these equal, larger {@link #getX()} wins.
     * Otherwise the nodes are equal.
     */
    @Override
    public int compareTo(final Node arg0) {
        if (this.getY() > arg0.getY()) {
            return 1;
        } else if (this.getY() == arg0.getY()) {
            if (this.getX() > arg0.getX()) {
                return 1;
            } else if (this.getX() == arg0.getX()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }

    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "Node [" + this.x + ", " + this.y + "]";
    }

}
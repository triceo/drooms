package org.drooms.api;

import java.util.SortedMap;
import java.util.TreeMap;

public class Node {

    private final int x, y;

    private static final SortedMap<Integer, SortedMap<Integer, Node>> nodes = new TreeMap<Integer, SortedMap<Integer, Node>>();

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

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.x;
        result = prime * result + this.y;
        return result;
    }

    @Override
    public String toString() {
        return "Node [" + this.x + ", " + this.y + "]";
    }

}
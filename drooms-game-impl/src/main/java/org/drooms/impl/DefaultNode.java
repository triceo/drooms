package org.drooms.impl;

import java.util.SortedMap;
import java.util.TreeMap;

import org.drooms.api.Node;

public class DefaultNode implements Node {

    private final int x, y;

    private static final SortedMap<Integer, SortedMap<Integer, DefaultNode>> nodes = new TreeMap<Integer, SortedMap<Integer, DefaultNode>>();

    public static synchronized DefaultNode getNode(final int x, final int y) {
        if (!DefaultNode.nodes.containsKey(x)) {
            DefaultNode.nodes.put(x, new TreeMap<Integer, DefaultNode>());
        }
        final SortedMap<Integer, DefaultNode> ys = DefaultNode.nodes.get(x);
        if (!ys.containsKey(y)) {
            ys.put(y, new DefaultNode(x, y));
        }
        return ys.get(y);
    }

    private DefaultNode(final int x, final int y) {
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
        final DefaultNode other = (DefaultNode) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
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
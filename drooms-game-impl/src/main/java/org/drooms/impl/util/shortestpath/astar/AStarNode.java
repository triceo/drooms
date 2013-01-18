package org.drooms.impl.util.shortestpath.astar;

final class AStarNode<V> implements Comparable<AStarNode<V>> {

    private final V node;

    private double g;

    private double f;

    public AStarNode(final V node, final double g, final double f) {
        this.node = node;
        this.g = g;
        this.f = f;
    }

    @Override
    public int compareTo(final AStarNode<V> arg0) {
        if (this.getF() < arg0.getF()) {
            return -1;
        } else if (this.getF() > arg0.getF()) {
            return 1;
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AStarNode)) {
            return false;
        }
        final AStarNode<V> other = (AStarNode<V>) obj;
        if (this.node == null) {
            if (other.node != null) {
                return false;
            }
        } else if (!this.node.equals(other.node)) {
            return false;
        }
        return true;
    }

    public double getF() {
        return this.f;
    }

    public double getG() {
        return this.g;
    }

    public V getNode() {
        return this.node;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.node == null) ? 0 : this.node.hashCode());
        return result;
    }

    public void setF(final double f) {
        this.f = f;
    }

    public void setG(final double g) {
        this.g = g;
    }

}

package org.drooms.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.drooms.api.Edge;
import org.drooms.api.Node;

class DefaultEdge implements Edge {

    private final ImmutablePair<Node, Node> nodes;

    /**
     * A {@link Node} is considered larger than the other if it has a bigger {@link #getY()}. In case these equal,
     * larger {@link #getX()} wins. Otherwise the nodes are equal.
     */
    private static boolean isNodeLarger(final Node isLarger, final Node target) {
        if (isLarger.getY() > target.getY()) {
            return true;
        } else if (isLarger.getY() == target.getY()) {
            if (isLarger.getX() > target.getX()) {
                return true;
            } else if (isLarger.getX() == target.getX()) {
                return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Make two nodes immediately adjacent.
     * 
     * @param firstNode
     *            One node.
     * @param secondNode
     *            The other.
     */
    public DefaultEdge(final Node firstNode, final Node secondNode) {
        if (firstNode == null || secondNode == null) {
            throw new IllegalArgumentException("Neither nodes can be null.");
        } else if (firstNode.equals(secondNode)) {
            throw new IllegalArgumentException(
                    "Edges between the same node make no sense.");
        }
        if (!DefaultEdge.isNodeLarger(firstNode, secondNode)) {
            this.nodes = ImmutablePair.of(firstNode, secondNode);
        } else {
            this.nodes = ImmutablePair.of(secondNode, firstNode);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultEdge)) {
            return false;
        }
        final DefaultEdge other = (DefaultEdge) obj;
        if (this.nodes == null) {
            if (other.nodes != null) {
                return false;
            }
        } else if (!this.nodes.equals(other.nodes)) {
            return false;
        }
        return true;
    }

    @Override
    public ImmutablePair<Node, Node> getNodes() {
        return this.nodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.nodes == null) ? 0 : this.nodes.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Edge [nodes=").append(this.nodes).append("]");
        return builder.toString();
    }

}
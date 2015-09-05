package org.drooms.impl;

import org.drooms.api.Edge;
import org.drooms.api.Node;

class DefaultEdge implements Edge {

    @Override
    public Node getFirstNode() {
        return firstNode;
    }

    @Override
    public Node getSecondNode() {
        return secondNode;
    }

    private final Node firstNode, secondNode;

    /**
     * A {@link Node} is considered larger than the other if it has a bigger {@link Node#getY()}. In case these equal,
     * larger {@link Node#getX()} wins. Otherwise the nodes are equal.
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
    protected DefaultEdge(final Node firstNode, final Node secondNode) {
        if (firstNode == null || secondNode == null) {
            throw new IllegalArgumentException("Neither nodes can be null.");
        } else if (firstNode.equals(secondNode)) {
            throw new IllegalArgumentException(
                    "Edges between the same node make no sense.");
        }
        if (!DefaultEdge.isNodeLarger(firstNode, secondNode)) {
            this.firstNode = firstNode;
            this.secondNode = secondNode;
        } else {
            this.firstNode = secondNode;
            this.secondNode = firstNode;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DefaultEdge that = (DefaultEdge) o;
        if (this.firstNode != null ? !this.firstNode.equals(that.firstNode) : that.firstNode != null) return false;
        return !(this.secondNode != null ? !this.secondNode.equals(that.secondNode) : that.secondNode != null);

    }

    @Override
    public int hashCode() {
        int result = this.firstNode != null ? this.firstNode.hashCode() : 0;
        result = 31 * result + (this.secondNode != null ? this.secondNode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultEdge [firstNode=" + this.firstNode + ", secondNode=" + this.secondNode + ']';
    }

}

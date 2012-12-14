package org.drooms.api;

/**
 * Represents a connection (a route) between two immediately adjacent
 * {@link Node}s in a {@link Playground}. Worms can move from one {@link Node}
 * to anoter only by using an {@link Edge}. The connection is always
 * bi-directional.
 */
public class Edge {

    private final Node firstNode, secondNode;

    /**
     * Make two nodes immediately adjacent.
     * 
     * @param firstNode
     *            One node.
     * @param secondNode
     *            The other.
     */
    public Edge(final Node firstNode, final Node secondNode) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
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
        final Edge other = (Edge) obj;
        if (this.firstNode == null) {
            if (other.firstNode != null) {
                return false;
            }
        } else if (!this.firstNode.equals(other.firstNode)) {
            return false;
        }
        if (this.secondNode == null) {
            if (other.secondNode != null) {
                return false;
            }
        } else if (!this.secondNode.equals(other.secondNode)) {
            return false;
        }
        return true;
    }

    public Node getFirstNode() {
        return this.firstNode;
    }

    public Node getSecondNode() {
        return this.secondNode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (this.firstNode == null ? 0 : this.firstNode.hashCode());
        result = prime * result
                + (this.secondNode == null ? 0 : this.secondNode.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Edge [" + this.firstNode + "<->" + this.secondNode + "]";
    }

}
package org.drooms.impl;

import org.drooms.api.Edge;

public class DefaultEdge implements Edge<DefaultNode> {

    private final DefaultNode firstNode, secondNode;

    public DefaultEdge(final DefaultNode firstNode, final DefaultNode secondNode) {
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
        final DefaultEdge other = (DefaultEdge) obj;
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

    @Override
    public DefaultNode getFirstNode() {
        return this.firstNode;
    }

    @Override
    public DefaultNode getSecondNode() {
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
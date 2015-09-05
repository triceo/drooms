package org.drooms.impl;

import org.drooms.api.Node;

class DefaultNode implements Node {

    @Override
    public Node.Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + this.x;
        result = prime * result + this.y;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultNode)) {
            return false;
        }
        final DefaultNode other = (DefaultNode) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    private final Node.Type type;
    private final int x, y;

    protected DefaultNode(final int x, final int y) {
        this(Node.Type.REGULAR, x, y);
    }

    protected DefaultNode(final Node.Type type, final int x, final int y) {
        this.type = type;
        this.x = x;
        this.y = y;
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
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DefaultNode [");
        if (this.type != null) {
            builder.append("type=").append(this.type).append(", ");
        }
        builder.append("x=").append(this.x).append(", y=").append(this.y).append("]");
        return builder.toString();
    }

}

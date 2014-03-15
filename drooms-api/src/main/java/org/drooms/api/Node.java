package org.drooms.api;

/**
 * A single indivisible unit of real estate on the {@link Playground}.
 */
public class Node {

    public Type getType() {
        return this.type;
    }

    public static enum Type {

        REGULAR, WALL, STARTING_POSITION, PORTAL;

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
        if (!(obj instanceof Node)) {
            return false;
        }
        final Node other = (Node) obj;
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

    private final Type type;
    private final int x, y;

    public Node(final int x, final int y) {
        this(Type.REGULAR, x, y);
    }

    public Node(final Type type, final int x, final int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Node [");
        if (this.type != null) {
            builder.append("type=").append(this.type).append(", ");
        }
        builder.append("x=").append(this.x).append(", y=").append(this.y).append("]");
        return builder.toString();
    }

}
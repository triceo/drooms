package org.drooms.api;

import org.drools.KnowledgeBase;

public class Player {

    private final String name;
    private final KnowledgeBase kbase;

    public Player(final String name, final KnowledgeBase knowledgeBase) {
        this.name = name;
        this.kbase = knowledgeBase;
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
        final Player other = (Player) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public KnowledgeBase getKnowledgeBase() {
        return this.kbase;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DefaultPlayer [name=").append(this.name).append("]");
        return builder.toString();
    }

}

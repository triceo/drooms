package org.drooms.impl;

import org.drools.KnowledgeBase;
import org.drooms.api.Player;

public class DefaultPlayer implements Player {

    private final char sign;
    private final String name;
    private final KnowledgeBase kbase;

    public DefaultPlayer(final String name, final char sign, final KnowledgeBase knowledgeBase) {
        this.name = name;
        this.kbase = knowledgeBase;
        this.sign = sign;
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
        final DefaultPlayer other = (DefaultPlayer) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public KnowledgeBase getKnowledgeBase() {
        return this.kbase;
    }

    @Override
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

    @Override
    public char getSign() {
        return this.sign;
    }

}

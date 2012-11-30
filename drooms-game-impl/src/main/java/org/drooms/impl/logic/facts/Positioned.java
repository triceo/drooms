package org.drooms.impl.logic.facts;

import org.drooms.api.Node;

public interface Positioned<N extends Node> {

    public N getNode();

    public int getX();

    public int getY();

}
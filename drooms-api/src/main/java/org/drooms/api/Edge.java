package org.drooms.api;

public interface Edge<T extends Node> {

    public T getFirstNode();

    public T getSecondNode();

}

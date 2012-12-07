package org.drooms.api;

import java.util.List;

import edu.uci.ics.jung.graph.Graph;

public interface Playground {

    public Graph<Node, Edge> getGraph();

    public int getHeight();

    public Node getNode(int x, int y);

    public List<Node> getStartingPositions();

    public int getWidth();

    public boolean isAvailable(int x, int y);

}

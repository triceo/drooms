package org.drooms.impl.logic;

import edu.uci.ics.jung.graph.Graph;
import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.drooms.api.Playground;
import org.drooms.impl.DefaultGame;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class PathTrackerTest {

    private static final Playground PLAYGROUND = new DefaultGame().buildPlayground("test", PathTrackerTest.class
            .getResourceAsStream("testing.playground"));
    private static final Graph<Node, Edge> GRAPH = PathTrackerTest.PLAYGROUND.getGraph();

    @Test
    public void testPathToItself() {
        final Node start = PLAYGROUND.getNodeAt(10, 10);
        Assert.assertEquals(Collections.EMPTY_LIST, PathTracker.getPath(GRAPH, start, Collections.singleton(start)));
    }

    @Test
    public void testPathToNext() {
        final Node start = PLAYGROUND.getNodeAt(10, 10);
        final Node end = PLAYGROUND.getNodeAt(10, start.getY() + 1);
        final List<Edge> path = PathTracker.getPath(GRAPH, start, Collections.singleton(end));
        Assert.assertEquals(1, path.size());
        final Edge edge = path.get(0);
        Assert.assertEquals(start, edge.getNodes().getLeft());
        Assert.assertEquals(end, edge.getNodes().getRight());
    }

    @Test
    public void testPathToNextWithGarbage() {
        final Node start = PLAYGROUND.getNodeAt(10, 10);
        final Node end = PLAYGROUND.getNodeAt(10, start.getY() + 1);
        // start and null in the set will be ignored as garbage
        final List<Edge> path = PathTracker.getPath(GRAPH, start, PathTrackerTest.toSet(start, end, null));
        Assert.assertEquals(1, path.size());
        final Edge edge = path.get(0);
        Assert.assertEquals(start, edge.getNodes().getLeft());
        Assert.assertEquals(end, edge.getNodes().getRight());
    }

    private static int getManhattanDistance(final Node start, final Node end) {
        return Math.abs(end.getX() - start.getX()) + Math.abs(end.getY() - start.getY());
    }

    private static Set<Node> toSet(final Node... nodes) {
        return new LinkedHashSet<>(Arrays.asList(nodes));
    }

    @Test
    public void testShortestPath() {
        final Node start = PLAYGROUND.getNodeAt(10, 10);
        final Node end = PLAYGROUND.getNodeAt(15, 15);
        // the length of shortest path between two objects in a 2D grid is their Manhattan distance
        final int manhattanDistance = PathTrackerTest.getManhattanDistance(start, end);
        final List<Edge> path = PathTracker.getPath(GRAPH, start, Collections.singleton(end));
        Assert.assertEquals(manhattanDistance, path.size());
    }

    private void testShortestPathThroughNode(final boolean endFirst) {
        // pass through a corner node
        final Node start = PLAYGROUND.getNodeAt(3, 0);
        final Node middle = PLAYGROUND.getNodeAt(0, 0);
        final Node end = PLAYGROUND.getNodeAt(0, 3);
        // the length of shortest path between two objects in a 2D grid is their Manhattan distance
        final int manhattanDistance = PathTrackerTest.getManhattanDistance(start, end);
        final List<Edge> path = PathTracker.getPath(GRAPH, start, endFirst ? PathTrackerTest.toSet(end, middle) :
                PathTrackerTest.toSet(middle, end));
        Assert.assertEquals(manhattanDistance, path.size());
        // make sure that in the path there is the middle node
        final Collection<Edge> edges = GRAPH.getIncidentEdges(middle);
        Assert.assertEquals(2, edges.size()); // otherwise the following assertion makes no sense
        edges.forEach(edge -> Assert.assertTrue(path.contains(edge)));
    }

    @Test
    public void testShortestPathThroughNodeMiddleFirst() {
        this.testShortestPathThroughNode(false);
    }

    @Test
    public void testShortestPathThroughNodeEndFirst() {
        this.testShortestPathThroughNode(true);
    }

    private void testWrongNode(final boolean unreachableFirst, final boolean doesNodeExist) {
        final Node start = PLAYGROUND.getNodeAt(3, 0);
        final Node end = PLAYGROUND.getNodeAt(0, 3);
        // first node is surrounded by walls, the other is a wall itself and therefore not present in the graph
        final Node unreachable = doesNodeExist ? PLAYGROUND.getNodeAt(68, 12) : PLAYGROUND.getNodeAt(10, 15);
        final List<Edge> path = PathTracker.getPath(GRAPH, start, unreachableFirst ? PathTrackerTest.toSet(unreachable,
                end) : PathTrackerTest.toSet(end, unreachable));
        Assert.assertEquals(0, path.size());
    }


    @Test
    public void testUnreachableNodeFirst() {
        this.testWrongNode(true, true);
    }

    @Test
    public void testUnreachableNodeLast() {
        this.testWrongNode(false, true);
    }

    @Test
    public void testNonexistentNodeFirst() {
        this.testWrongNode(true, false);
    }

    @Test
    public void testNonexistentNodeLast() {
        this.testWrongNode(false, false);
    }

    @Test
    public void testShortestPathThroughTwoNodes() {
        final int DISTANCE = 3;
        // pass through a corner node
        final Node start = PLAYGROUND.getNodeAt(DISTANCE, 0);
        final Node middle1 = PLAYGROUND.getNodeAt(0, 0);
        final Node middle2 = PLAYGROUND.getNodeAt(DISTANCE, DISTANCE);
        final Node end = PLAYGROUND.getNodeAt(0, DISTANCE);
        // minimal possible path length
        final List<Edge> path = PathTracker.getPath(GRAPH, start, PathTrackerTest.toSet(middle1, end, middle2));
        Assert.assertEquals(DISTANCE + DISTANCE + DISTANCE, path.size());
        // make sure that all nodes are in path
        Set<Node> nodesToFind = PathTrackerTest.toSet(start, middle1, middle2, end);
        path.forEach(edge -> nodesToFind.removeAll(GRAPH.getIncidentVertices(edge)));
        Assert.assertEquals(0, nodesToFind.size());
    }
}

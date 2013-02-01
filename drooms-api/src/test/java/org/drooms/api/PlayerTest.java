package org.drooms.api;

import java.net.URL;
import java.net.URLClassLoader;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;

public class PlayerTest {

    private static ClassLoader obtainClassLoader() {
        return new URLClassLoader(new URL[0]);
    }

    private static CustomPathBasedStrategy obtainStrategy() {
        return new CustomPathBasedStrategy() {

            @Override
            public boolean enableAudit() {
                return false;
            }

            @Override
            public KnowledgeBuilder getKnowledgeBuilder(final ClassLoader cls) {
                return KnowledgeBuilderFactory.newKnowledgeBuilder();
            }

            @Override
            public String getName() {
                return "Testing";
            }

            @Override
            public ShortestPath<Node, Edge> getShortestPathAlgorithm(final Graph<Node, Edge> graph) {
                return new UnweightedShortestPath<Node, Edge>(graph);
            }
        };
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullClassLoader() {
        new Player("test", PlayerTest.obtainStrategy(), null);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new Player(null, PlayerTest.obtainStrategy(), PlayerTest.obtainClassLoader());
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStrategy() {
        new Player("test", null, PlayerTest.obtainClassLoader());
    }

    public void testEquals() {
        final CustomPathBasedStrategy strategy = PlayerTest.obtainStrategy();
        final ClassLoader loader = PlayerTest.obtainClassLoader();
        final Player p1 = new Player("test", strategy, loader);
        final Player p2 = new Player("test", strategy, loader);
        final Player p3 = new Player("test2", strategy, loader);
        Assert.assertEquals(p1, p1);
        Assert.assertEquals(p1, p2);
        Assert.assertNotEquals(p1, p3);
        Assert.assertNotEquals(p2, p3);
    }

}

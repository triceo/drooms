package org.drooms.api;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilderFactory;
import org.junit.Assert;
import org.junit.Test;

public class PlayerTest {

    private static KnowledgeBase obtainKnowledgeBase() {
        return KnowledgeBuilderFactory.newKnowledgeBuilder().newKnowledgeBase();
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKBase() {
        new Player("test", null);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new Player(null, PlayerTest.obtainKnowledgeBase());
    }

    public void testEquals() {
        final KnowledgeBase kbase = PlayerTest.obtainKnowledgeBase();
        final Player p1 = new Player("test", kbase);
        final Player p2 = new Player("test", kbase);
        final Player p3 = new Player("test", PlayerTest.obtainKnowledgeBase());
        Assert.assertEquals(p1, p1);
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p2, p3);
        Assert.assertEquals(p1, p3);
    }

    public void testNotEqualsName() {
        final KnowledgeBase kbase = PlayerTest.obtainKnowledgeBase();
        final Player p1 = new Player("test", kbase);
        final Player p2 = new Player("test2", kbase);
        final Player p3 = new Player("test2", PlayerTest.obtainKnowledgeBase());
        Assert.assertNotEquals(p1, p2);
        Assert.assertNotEquals(p2, p3);
        Assert.assertNotEquals(p1, p3);
    }
}

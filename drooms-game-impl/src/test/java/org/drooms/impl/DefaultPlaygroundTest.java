package org.drooms.impl;

import java.util.List;

import org.drooms.api.Node;
import org.drooms.api.Playground;
import org.junit.Assert;
import org.junit.Test;

public class DefaultPlaygroundTest {

    @Test
    public void testGoodPlayground() {
        final Playground p = new DefaultGame().buildPlayground("test", this.getClass().getResourceAsStream("good_playground.txt"));
        Assert.assertEquals(4, p.getWidth());
        Assert.assertEquals(5, p.getHeight());
        List<Node> startingPositions = p.getStartingPositions();
        Assert.assertEquals(2, startingPositions.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testBadPortal1() {
        new DefaultGame().buildPlayground("test", this.getClass().getResourceAsStream("playground_multiportal.txt"));
    }

    @Test(expected = IllegalStateException.class)
    public void testBadPortal2() {
        new DefaultGame().buildPlayground("test", this.getClass().getResourceAsStream("playground_unfinished_portal.txt"));
    }
}

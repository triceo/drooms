package org.drooms.api;

import org.junit.Assert;
import org.junit.Test;

public class EdgeTest {

    private final Node leastNode = Node.getNode(10, 20);
    private final Node largerNode = Node.getNode(11, 20);
    private final Node largestNode = Node.getNode(9, 21);

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeSame() {
        new Edge(this.leastNode, this.leastNode);
    }

    @Test
    public void testEqualsObject() {
        final Edge e1 = new Edge(this.leastNode, this.largerNode);
        final Edge e2 = new Edge(this.largerNode, this.leastNode);
        Assert.assertEquals(e1, e2);
    }

    @Test
    public void testNotEqualsObject() {
        final Edge e1 = new Edge(this.leastNode, this.largerNode);
        final Edge e2 = new Edge(this.largerNode, this.largestNode);
        Assert.assertNotEquals(e1, e2);
    }

    @Test
    public void testOrdering1() {
        final Edge e1 = new Edge(this.leastNode, this.largerNode);
        Assert.assertSame(this.leastNode, e1.getNodes().getLeft());
        Assert.assertSame(this.largerNode, e1.getNodes().getRight());
    }

    @Test
    public void testOrdering2() {
        final Edge e1 = new Edge(this.largerNode, this.leastNode);
        Assert.assertSame(this.leastNode, e1.getNodes().getLeft());
        Assert.assertSame(this.largerNode, e1.getNodes().getRight());
    }

    @Test
    public void testOrdering3() {
        final Edge e1 = new Edge(this.leastNode, this.largestNode);
        Assert.assertSame(this.leastNode, e1.getNodes().getLeft());
        Assert.assertSame(this.largestNode, e1.getNodes().getRight());
    }

    @Test
    public void testOrdering4() {
        final Edge e1 = new Edge(this.largestNode, this.leastNode);
        Assert.assertSame(this.leastNode, e1.getNodes().getLeft());
        Assert.assertSame(this.largestNode, e1.getNodes().getRight());
    }

    @Test
    public void testOrdering5() {
        final Edge e1 = new Edge(this.largerNode, this.largestNode);
        Assert.assertSame(this.largerNode, e1.getNodes().getLeft());
        Assert.assertSame(this.largestNode, e1.getNodes().getRight());
    }

    @Test
    public void testOrdering6() {
        final Edge e1 = new Edge(this.largestNode, this.largerNode);
        Assert.assertSame(this.largerNode, e1.getNodes().getLeft());
        Assert.assertSame(this.largestNode, e1.getNodes().getRight());
    }
}

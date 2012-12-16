package org.drooms.api;

import org.junit.Assert;
import org.junit.Test;

public class NodeTest {

    @Test
    public void testCompareToDifferentY() {
        final Node n1 = Node.getNode(1, 9);
        final Node n2 = Node.getNode(1, 10);
        Assert.assertTrue(n2.compareTo(n1) > 0);
        Assert.assertTrue(n1.compareTo(n2) < 0);
    }

    @Test
    public void testCompareToSame() {
        final Node n1 = Node.getNode(1, 10);
        Assert.assertTrue(n1.compareTo(n1) == 0);
    }

    @Test
    public void testCompareToSameY() {
        final Node n1 = Node.getNode(1, 10);
        final Node n2 = Node.getNode(2, 10);
        Assert.assertTrue(n2.compareTo(n1) > 0);
        Assert.assertTrue(n1.compareTo(n2) < 0);
    }

    @Test
    public void testGetNode() {
        final Node n1 = Node.getNode(10, 20);
        final Node n2 = Node.getNode(10, 20);
        Assert.assertSame(n1, n2);
        Assert.assertEquals(n1, n2);
    }

}

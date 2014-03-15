package org.drooms.api;

import org.junit.Assert;
import org.junit.Test;

public class NodeTest {

    @Test
    public void testGetNode() {
        final Node n1 = new Node(10, 20);
        final Node n2 = new Node(10, 20);
        Assert.assertEquals(n1, n2);
    }

}

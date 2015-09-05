package org.drooms.impl;

import org.assertj.core.api.Assertions;
import org.drooms.api.Edge;
import org.drooms.api.Node;
import org.junit.Test;

public class DefaultEdgeTest {

    private final Node leastNode = new Node(10, 20);
    private final Node largerNode = new Node(11, 20);
    private final Node largestNode = new Node(9, 21);

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeSame() {
        new DefaultEdge(this.leastNode, this.leastNode);
    }

    @Test
    public void testEqualsObject() {
        final Edge e1 = new DefaultEdge(this.leastNode, this.largerNode);
        final Edge e2 = new DefaultEdge(this.largerNode, this.leastNode);
        Assertions.assertThat(e2).isEqualTo(e1);
    }

    @Test
    public void testNotEqualsObject() {
        final Edge e1 = new DefaultEdge(this.leastNode, this.largerNode);
        final Edge e2 = new DefaultEdge(this.largerNode, this.largestNode);
        Assertions.assertThat(e2).isNotEqualTo(e1);
    }

    @Test
    public void testOrdering1() {
        final Edge e1 = new DefaultEdge(this.leastNode, this.largerNode);
        Assertions.assertThat(e1.getFirstNode()).isSameAs(this.leastNode);
        Assertions.assertThat(e1.getSecondNode()).isSameAs(this.largerNode);
    }

    @Test
    public void testOrdering2() {
        final Edge e1 = new DefaultEdge(this.largerNode, this.leastNode);
        Assertions.assertThat(e1.getFirstNode()).isSameAs(this.leastNode);
        Assertions.assertThat(e1.getSecondNode()).isSameAs(this.largerNode);
    }

    @Test
    public void testOrdering3() {
        final Edge e1 = new DefaultEdge(this.leastNode, this.largestNode);
        Assertions.assertThat(e1.getFirstNode()).isSameAs(this.leastNode);
        Assertions.assertThat(e1.getSecondNode()).isSameAs(this.largestNode);
    }

    @Test
    public void testOrdering4() {
        final Edge e1 = new DefaultEdge(this.largestNode, this.leastNode);
        Assertions.assertThat(e1.getFirstNode()).isSameAs(this.leastNode);
        Assertions.assertThat(e1.getSecondNode()).isSameAs(this.largestNode);
    }

    @Test
    public void testOrdering5() {
        final Edge e1 = new DefaultEdge(this.largerNode, this.largestNode);
        Assertions.assertThat(e1.getFirstNode()).isSameAs(this.largerNode);
        Assertions.assertThat(e1.getSecondNode()).isSameAs(this.largestNode);
    }

    @Test
    public void testOrdering6() {
        final Edge e1 = new DefaultEdge(this.largestNode, this.largerNode);
        Assertions.assertThat(e1.getFirstNode()).isSameAs(this.largerNode);
        Assertions.assertThat(e1.getSecondNode()).isSameAs(this.largestNode);
    }
}

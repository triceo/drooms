package org.drooms.impl;

import org.assertj.core.api.Assertions;
import org.drooms.api.Node;
import org.junit.Test;

public class DefaultNodeTest {

    @Test
    public void testGetNode() {
        final Node n1 = new DefaultNode(10, 20);
        final Node n2 = new DefaultNode(10, 20);
        Assertions.assertThat(n2).isEqualTo(n1);
    }

}

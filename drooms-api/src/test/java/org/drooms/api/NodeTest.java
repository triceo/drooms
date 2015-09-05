package org.drooms.api;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class NodeTest {

    @Test
    public void testGetNode() {
        final Node n1 = new Node(10, 20);
        final Node n2 = new Node(10, 20);
        Assertions.assertThat(n2).isEqualTo(n1);
    }

}

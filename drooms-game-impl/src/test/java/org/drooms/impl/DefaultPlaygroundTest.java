package org.drooms.impl;

import org.assertj.core.api.Assertions;
import org.drooms.api.Node;
import org.drooms.api.Playground;
import org.junit.Test;

import java.util.List;

public class DefaultPlaygroundTest {

    @Test
    public void testGoodPlayground() {
        final Playground p = new DefaultGame().buildPlayground("test",
                this.getClass().getResourceAsStream("good_playground.txt"));
        Assertions.assertThat(p.getWidth()).isEqualTo(4);
        Assertions.assertThat(p.getHeight()).isEqualTo(5);
        final List<Node> startingPositions = p.getStartingPositions();
        Assertions.assertThat(startingPositions).hasSize(2);
    }

    @Test(expected = IllegalStateException.class)
    public void testBadPortal1() {
        new DefaultGame().buildPlayground("test", this.getClass().getResourceAsStream("playground_multiportal.txt"));
    }

    @Test(expected = IllegalStateException.class)
    public void testBadPortal2() {
        new DefaultGame().buildPlayground("test",
                this.getClass().getResourceAsStream("playground_unfinished_portal.txt"));
    }
}

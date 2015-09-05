package org.drooms.impl;

import org.assertj.core.api.Assertions;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.logic.PathTrackerTest;
import org.junit.Test;

public class PlayerPositionTest {

    private static final Playground PLAYGROUND = new DefaultGame().buildPlayground("test", PathTrackerTest.class
            .getResourceAsStream("testing.playground"));

    private static final Player PLAYER = new Player("a", "b", "c", "d");

    @Test
    public void testReverse() {
        final Node node1 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 1);
        final Node node2 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 2);
        final Node node3 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 3);
        final PlayerPosition pos = PlayerPosition.build(PlayerPositionTest.PLAYGROUND, PlayerPositionTest.PLAYER, node1, node2, node3);
        Assertions.assertThat(pos.getHeadNode()).isEqualTo(node1);
        final PlayerPosition pos2 = pos.reverse();
        Assertions.assertThat(pos2.getNodes()).containsOnlyElementsOf(pos.getNodes());
        Assertions.assertThat(pos2.getHeadNode()).isEqualTo(node3);
        Assertions.assertThat(pos2).isNotSameAs(pos);
    }

    @Test
    public void testNewHead() {
        final Node node1 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 1);
        final Node node2 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 2);
        final PlayerPosition pos = PlayerPosition.build(PlayerPositionTest.PLAYGROUND, PlayerPositionTest.PLAYER, node1);
        Assertions.assertThat(pos.getHeadNode()).isEqualTo(node1);
        final PlayerPosition pos2 = pos.newHead(node2);
        Assertions.assertThat(pos2.getNodes()).containsOnly(node1, node2);
        Assertions.assertThat(pos2.getHeadNode()).isEqualTo(node2);
        Assertions.assertThat(pos2).isNotSameAs(pos);
    }

    @Test
    public void testCutDownToSize() {
        final Node node1 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 1);
        final Node node2 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 2);
        final Node node3 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 3);
        final PlayerPosition pos = PlayerPosition.build(PlayerPositionTest.PLAYGROUND, PlayerPositionTest.PLAYER, node1, node2, node3);
        Assertions.assertThat(pos.getNodes()).containsExactly(node1, node2, node3);
        Assertions.assertThat(pos.getHeadNode()).isEqualTo(node1);
        // cut down
        final int newLength = 2;
        final PlayerPosition pos2 = pos.ensureMaxLength(newLength);
        Assertions.assertThat(pos2).isNotSameAs(pos);
        Assertions.assertThat(pos2.getNodes()).containsExactly(node1, node2);
        // stay same
        final PlayerPosition pos3 = pos2.ensureMaxLength(newLength);
        Assertions.assertThat(pos3).isSameAs(pos2);
        Assertions.assertThat(pos3.getNodes()).containsExactly(node1, node2);
    }

}

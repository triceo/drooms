package org.drooms.impl;

import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.logic.PathTrackerTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lpetrovi on 5.9.15.
 */
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
        Assert.assertEquals(node1, pos.getHeadNode());
        final PlayerPosition pos2 = pos.reverse();
        Assert.assertEquals(pos.getNodes().size(), pos2.getNodes().size());
        Assert.assertEquals(node3, pos2.getHeadNode());
        Assert.assertNotSame(pos, pos2);
    }

    @Test
    public void testNewHead() {
        final Node node1 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 1);
        final Node node2 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 2);
        final PlayerPosition pos = PlayerPosition.build(PlayerPositionTest.PLAYGROUND, PlayerPositionTest.PLAYER, node1);
        Assert.assertEquals(node1, pos.getHeadNode());
        final PlayerPosition pos2 = pos.newHead(node2);
        Assert.assertEquals(pos.getNodes().size() + 1, pos2.getNodes().size());
        Assert.assertEquals(node2, pos2.getHeadNode());
        Assert.assertNotSame(pos, pos2);
    }

    @Test
    public void testCutDownToSize() {
        final Node node1 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 1);
        final Node node2 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 2);
        final Node node3 = PlayerPositionTest.PLAYGROUND.getNodeAt(1, 3);
        final PlayerPosition pos = PlayerPosition.build(PlayerPositionTest.PLAYGROUND, PlayerPositionTest.PLAYER, node1, node2, node3);
        Assert.assertEquals(3, pos.getNodes().size());
        Assert.assertEquals(node1, pos.getHeadNode());
        // cut down
        final int newLength = 2;
        final PlayerPosition pos2 = pos.ensureMaxLength(newLength);
        Assert.assertNotSame(pos, pos2);
        Assert.assertEquals(newLength, pos2.getNodes().size());
        Assert.assertEquals(node1, pos2.getHeadNode());
        // stay same
        final PlayerPosition pos3 = pos2.ensureMaxLength(newLength);
        Assert.assertSame(pos2, pos3);
        Assert.assertEquals(newLength, pos3.getNodes().size());
        Assert.assertEquals(node1, pos3.getHeadNode());
    }

}

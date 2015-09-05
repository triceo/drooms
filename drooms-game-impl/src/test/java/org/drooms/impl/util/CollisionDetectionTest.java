package org.drooms.impl.util;

import org.assertj.core.api.Assertions;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.DefaultGame;
import org.drooms.impl.PlayerPosition;
import org.drooms.impl.logic.PathTrackerTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CollisionDetectionTest {

    private static final Playground PLAYGROUND = new DefaultGame().buildPlayground("test", PathTrackerTest.class
            .getResourceAsStream("testing.playground"));

    private static Deque<Node> toPlayer(Node... positions) {
        return new LinkedList<>(Arrays.asList(positions));
    }

    private static Node SHARED_NODE = PLAYGROUND.getNodeAt(1, 1);
    private static Node COLLISION_NODE = PLAYGROUND.getNodeAt(5, 6);
    private static Node HEAD_COLLISION_NODE = PLAYGROUND.getNodeAt(11, 11);
    private static Node WALL_NODE = PLAYGROUND.getNodeAt(10, 15);

    private static Deque<Node> PLAYER_COLLIDED_WITH_ITSELF = CollisionDetectionTest.toPlayer(
            CollisionDetectionTest.SHARED_NODE,
            CollisionDetectionTest.PLAYGROUND.getNodeAt(1, 2),
            CollisionDetectionTest.PLAYGROUND.getNodeAt(2, 2),
            CollisionDetectionTest.PLAYGROUND.getNodeAt(2, 1),
            CollisionDetectionTest.SHARED_NODE);

    private static Deque<Node> PLAYER_COLLIDED_WITH_WALL = CollisionDetectionTest.toPlayer(
            CollisionDetectionTest.WALL_NODE,
            CollisionDetectionTest.PLAYGROUND.getNodeAt(10, 14));

    private static Deque<Node> PLAYER_UNCOLLIDED = CollisionDetectionTest.toPlayer(
            CollisionDetectionTest.PLAYGROUND.getNodeAt(5, 5),
            CollisionDetectionTest.COLLISION_NODE,
            CollisionDetectionTest.PLAYGROUND.getNodeAt(6, 6));

    private static Deque<Node> PLAYER_HITTING_THE_UNCOLLIDED = CollisionDetectionTest.toPlayer(
            CollisionDetectionTest.COLLISION_NODE,
            CollisionDetectionTest.PLAYGROUND.getNodeAt(4, 6),
            CollisionDetectionTest.PLAYGROUND.getNodeAt(3, 6));

    private static Deque<Node> PLAYER_HEAD_ON_HEAD_1 = CollisionDetectionTest.toPlayer(
            CollisionDetectionTest.HEAD_COLLISION_NODE,
            CollisionDetectionTest.PLAYGROUND.getNodeAt(11, 12)
    );

    private static Deque<Node> PLAYER_HEAD_ON_HEAD_2 = CollisionDetectionTest.toPlayer(
            CollisionDetectionTest.HEAD_COLLISION_NODE,
            CollisionDetectionTest.PLAYGROUND.getNodeAt(10, 11)
    );

    @Test
    public void testCollidedWithItself() {
        Assert.assertTrue(Detectors.didPlayerHitItself(CollisionDetectionTest.PLAYER_COLLIDED_WITH_ITSELF));
    }

    @Test
    public void testCollidedWithWall() {
        Assert.assertTrue(Detectors.didPlayerHitWall(CollisionDetectionTest.PLAYER_COLLIDED_WITH_WALL.getFirst(),
                CollisionDetectionTest.PLAYGROUND));
    }

    @Test
    public void testMutuallyUncollided() {
        Assert.assertFalse(Detectors.didPlayerCollideWithOther(
                CollisionDetectionTest.PLAYER_COLLIDED_WITH_ITSELF.getFirst(),
                CollisionDetectionTest.PLAYER_COLLIDED_WITH_WALL));
        Assert.assertFalse(Detectors.didPlayerCollideWithOther(
                CollisionDetectionTest.PLAYER_COLLIDED_WITH_WALL.getFirst(),
                CollisionDetectionTest.PLAYER_COLLIDED_WITH_ITSELF));
    }

    @Test
    public void testHeadOnCollision() {
        Assert.assertFalse(Detectors.didPlayerCollideWithOther(CollisionDetectionTest.PLAYER_UNCOLLIDED.getFirst(),
                CollisionDetectionTest.PLAYER_HITTING_THE_UNCOLLIDED));
        Assert.assertTrue(Detectors.didPlayerCollideWithOther(
                CollisionDetectionTest.PLAYER_HITTING_THE_UNCOLLIDED.getFirst(),
                CollisionDetectionTest.PLAYER_UNCOLLIDED));
    }

    @Test
    public void testIntegration() {
        // prepare the scenario
        final PlayerPosition expected = PlayerPosition.build(CollisionDetectionTest.PLAYGROUND,
                new Player("a", "b", "c", "1.0"), CollisionDetectionTest.PLAYER_UNCOLLIDED);
        final Set<PlayerPosition> players = new HashSet<>();
        players.add(expected);
        players.add(PlayerPosition.build(CollisionDetectionTest.PLAYGROUND, new Player("d", "e", "f", "1.0"),
                CollisionDetectionTest.PLAYER_HITTING_THE_UNCOLLIDED));
        players.add(PlayerPosition.build(CollisionDetectionTest.PLAYGROUND, new Player("g", "h", "i", "1.0"),
                CollisionDetectionTest.PLAYER_HEAD_ON_HEAD_1));
        players.add(PlayerPosition.build(CollisionDetectionTest.PLAYGROUND, new Player("j", "k", "l", "1.0"),
                CollisionDetectionTest.PLAYER_HEAD_ON_HEAD_2));
        players.add(PlayerPosition.build(CollisionDetectionTest.PLAYGROUND, new Player("m", "n", "o", "1.0"),
                CollisionDetectionTest.PLAYER_COLLIDED_WITH_WALL));
        players.add(PlayerPosition.build(CollisionDetectionTest.PLAYGROUND, new Player("p", "q", "r", "1.0"),
                CollisionDetectionTest.PLAYER_COLLIDED_WITH_ITSELF));
        // run the scenario
        final Set<PlayerPosition> collided = Detectors.detectCollision(players);
        Assertions.assertThat(collided).doesNotContain(expected);
    }

}

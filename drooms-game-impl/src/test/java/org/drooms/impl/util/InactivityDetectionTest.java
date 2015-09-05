package org.drooms.impl.util;

import org.assertj.core.api.Assertions;
import org.drooms.api.Action;
import org.drooms.api.Player;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class InactivityDetectionTest {

    private static  final <T> List<T> toList(T... objects) {
        return Arrays.asList(objects);
    }

    @Test
    public void testActivity1() {
        Assert.assertTrue(Detectors.isActive(InactivityDetectionTest.toList(Action.MOVE_UP), 1));
    }

    @Test
    public void testActivity2() {
        Assert.assertTrue(Detectors.isActive(InactivityDetectionTest.toList(Action.MOVE_UP, Action.NOTHING, Action.NOTHING), 3));
    }

    @Test
    public void testInactivityDetectionWorks() {
        Assert.assertFalse(Detectors.isActive(InactivityDetectionTest.toList(Action.MOVE_UP, Action.NOTHING, Action.NOTHING, Action.NOTHING), 2));
    }

    @Test
    public void testInactivityEvaluatesFromTheBack() {
        Assert.assertTrue(Detectors.isActive(InactivityDetectionTest.toList(Action.NOTHING, Action.NOTHING, Action.MOVE_UP, Action.NOTHING), 2));
    }

    @Test
    public void testIntegration() {
        final Map<Player, List<Action>> players = new HashMap<>();
        final Player active = new Player("a", "b", "c", "1.0");
        players.put(active, InactivityDetectionTest.toList(Action.MOVE_LEFT, Action.MOVE_UP, Action.NOTHING));
        final Player inactive = new Player("b", "c", "d", "1.0");
        players.put(inactive, InactivityDetectionTest.toList(Action.MOVE_RIGHT, Action.NOTHING, Action.NOTHING));
        Assertions.assertThat(Detectors.detectInactivity(1, players)).containsOnly(inactive);
    }

    @Test
    public void testNegativeTurnCount() {
        Assertions.assertThat(Detectors.detectInactivity(-1, Collections.emptyMap())).isEmpty();
    }

    @Test
    public void testEmptyPlayers() {
        Assertions.assertThat(Detectors.detectInactivity(1, Collections.emptyMap())).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAttribute() {
        Detectors.detectInactivity(0, null);
    }

}

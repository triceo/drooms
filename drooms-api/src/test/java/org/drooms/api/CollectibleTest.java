package org.drooms.api;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CollectibleTest {

    private static final Node LOCATION = new Node(0, 0);

    @Test
    public void testCollectibleConstructorExpiring() {
        final int points = 5;
        final int expiration = 2;
        final Collectible c = new Collectible(CollectibleTest.LOCATION, points, expiration);
        Assertions.assertThat(c.getPoints()).isEqualTo(points);
        Assertions.assertThat(c.expires()).isTrue();
        Assertions.assertThat(c.expiresInTurn()).isEqualTo(expiration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorNegativeExpiration() {
        new Collectible(CollectibleTest.LOCATION, 1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorNegativePoints() {
        new Collectible(CollectibleTest.LOCATION, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorProperExpirationNegativePoints() {
        new Collectible(CollectibleTest.LOCATION, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorProperExpirationZeroPoints() {
        new Collectible(CollectibleTest.LOCATION, 0, 1);
    }

    @Test
    public void testCollectibleConstructorUnexpiring() {
        final int points = 10;
        final Collectible c = new Collectible(CollectibleTest.LOCATION, points);
        Assertions.assertThat(c.getPoints()).isEqualTo(points);
        Assertions.assertThat(c.expires()).isFalse();
        Assertions.assertThat(c.expiresInTurn()).isEqualTo(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorZeroExpiration() {
        new Collectible(CollectibleTest.LOCATION, 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorZeroPoints() {
        new Collectible(CollectibleTest.LOCATION, 0);
    }

    @Test
    public void testEqualsSame() {
        final Collectible c = new Collectible(CollectibleTest.LOCATION, 5, 2);
        Assertions.assertThat(c).isEqualTo(c);
    }

    @Test
    public void testEqualsSameParameters() {
        final Collectible c1 = new Collectible(CollectibleTest.LOCATION, 5, 2);
        final Collectible c2 = new Collectible(CollectibleTest.LOCATION, 5, 2);
        Assertions.assertThat(c2).isNotEqualTo(c1);
    }
}

package org.drooms.api;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class CollectibleTest {

    private static final Node LOCATION = new Node(0, 0);

    @Test
    public void testCollectibleConstructorExpiring() {
        final int points = 5;
        final int expiration = 2;
        final Collectible c = new Collectible(CollectibleTest.LOCATION, points, expiration);
        Assume.assumeThat(c.getPoints(), CoreMatchers.is(points));
        Assert.assertTrue(c.expires());
        Assert.assertThat(c.expiresInTurn(), CoreMatchers.is(expiration));
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
        Assume.assumeThat(c.getPoints(), CoreMatchers.is(points));
        Assert.assertFalse(c.expires());
        Assert.assertThat(c.expiresInTurn(), CoreMatchers.is(-1));
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
        Assert.assertEquals(c, c);
    }

    @Test
    public void testEqualsSameParameters() {
        final Collectible c1 = new Collectible(CollectibleTest.LOCATION, 5, 2);
        final Collectible c2 = new Collectible(CollectibleTest.LOCATION, 5, 2);
        Assert.assertNotEquals(c1, c2);
    }
}

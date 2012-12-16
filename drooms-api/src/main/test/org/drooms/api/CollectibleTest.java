package org.drooms.api;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class CollectibleTest {

    @Test
    public void testCollectibleConstructorExpiring() {
        final int points = 5;
        final int expiration = 2;
        final Collectible c = new Collectible(points, expiration);
        Assume.assumeThat(c.getPoints(), CoreMatchers.is(points));
        Assert.assertTrue(c.expires());
        Assert.assertThat(c.expiresInTurn(), CoreMatchers.is(expiration));
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorNegativeExpiration() {
        new Collectible(1, -1);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorNegativePoints() {
        new Collectible(-1);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorProperExpirationNegativePoints() {
        new Collectible(-1, 1);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorProperExpirationZeroPoints() {
        new Collectible(0, 1);
    }

    @Test
    public void testCollectibleConstructorUnexpiring() {
        final int points = 10;
        final Collectible c = new Collectible(points);
        Assume.assumeThat(c.getPoints(), CoreMatchers.is(points));
        Assert.assertFalse(c.expires());
        Assert.assertThat(c.expiresInTurn(), CoreMatchers.is(-1));
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorZeroExpiration() {
        new Collectible(1, 0);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testCollectibleConstructorZeroPoints() {
        new Collectible(0);
    }

    @Test
    public void testEqualsSame() {
        final Collectible c = new Collectible(5, 2);
        Assert.assertEquals(c, c);
    }

    @Test
    public void testEqualsSameParameters() {
        final Collectible c1 = new Collectible(5, 2);
        final Collectible c2 = new Collectible(5, 2);
        Assert.assertNotEquals(c1, c2);
    }
}

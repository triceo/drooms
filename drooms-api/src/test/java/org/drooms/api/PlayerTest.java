package org.drooms.api;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lpetrovi on 29.8.15.
 */
public class PlayerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNoName() {
        new Player(null, "b", "c", "2.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoStrategy() {
        new Player("a", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGAVMissingArtifactId() {
        new Player("a", "b", null, "3.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGAVMissingGroupId() {
        new Player("a", null, "b", "3.0");
    }

    @Test
    public void testEquality() {
        final Player p = new Player("a", "b", "c", "1.0");
        Assert.assertEquals(p, p);
        final Player sameNameSameStrategy = new Player(p.getName(), p.getStrategyReleaseId());
        Assert.assertEquals(p, sameNameSameStrategy);
        final Player sameNameDifferentStrategy = new Player(p.getName(), p.getStrategyReleaseId().getGroupId(), p
                .getStrategyReleaseId().getArtifactId(), "2.0");
        Assert.assertEquals(p, sameNameDifferentStrategy);
        final Player sameNameDifferentStrategyVersion = new Player(p.getName(), "d", "e", "3.0");
        Assert.assertEquals(p, sameNameDifferentStrategyVersion);
        final Player differentNameSameStrategy = new Player("b", p.getStrategyReleaseId());
        Assert.assertNotEquals(p, differentNameSameStrategy);
    }

}

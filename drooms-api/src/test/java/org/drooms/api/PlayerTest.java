package org.drooms.api;

import org.assertj.core.api.Assertions;
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
        Assertions.assertThat(p).isEqualTo(p);
        final Player sameNameSameStrategy = new Player(p.getName(), p.getStrategyReleaseId());
        Assertions.assertThat(sameNameSameStrategy).isEqualTo(p);
        final Player sameNameDifferentStrategy = new Player(p.getName(), p.getStrategyReleaseId().getGroupId(),
                p.getStrategyReleaseId().getArtifactId(), "2.0");
        Assertions.assertThat(sameNameDifferentStrategy).isEqualTo(p);
        final Player sameNameDifferentStrategyVersion = new Player(p.getName(), "d", "e", "3.0");
        Assertions.assertThat(sameNameDifferentStrategyVersion).isEqualTo(p);
        final Player differentNameSameStrategy = new Player("b", p.getStrategyReleaseId());
        Assertions.assertThat(differentNameSameStrategy).isNotEqualTo(p);
    }

}

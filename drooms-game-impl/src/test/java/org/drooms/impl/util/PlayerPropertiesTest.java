package org.drooms.impl.util;

import org.drooms.api.Player;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerPropertiesTest {

    private static final Player PLAYER1 = new Player("name1", "some.strategies", "strategy-a", "1.0-SNAPSHOT");
    private static final Player PLAYER2_SAME_GAV = new Player("name2", PLAYER1.getStrategyReleaseId());
    private static final Player PLAYER3_DIFFERENT_VERSION = new Player("name3", PLAYER1.getStrategyReleaseId()
            .getGroupId(), PLAYER1.getStrategyReleaseId().getArtifactId(), "1.0");
    private static final Player PLAYER4_SAME_GROUP_ID = new Player("name4", PLAYER1.getStrategyReleaseId().getGroupId(),
            "strategy-b", "1.0-SNAPSHOT");
    private static final Player PLAYER5_ALL_DIFFERENT = new Player("name5", "other.strategies", "strategy-c",
            "1.0-SNAPSHOT");

    @Test
    public void testRoundTrip() throws Exception {
        final Map<String, Player> players = Arrays.asList(PLAYER1, PLAYER2_SAME_GAV, PLAYER3_DIFFERENT_VERSION,
                PLAYER4_SAME_GROUP_ID, PLAYER5_ALL_DIFFERENT).stream().collect(Collectors.toMap(Player::getName,
                Function.identity()));
        final PlayerProperties props = new PlayerProperties(File.createTempFile("drooms-", ".test"));
        props.write(players.values());
        final List<Player> retrievedPlayers = props.read();
        // and now figure out the assertions
        Assert.assertEquals("Number of players doesn't match", players.size(), retrievedPlayers.size());
        retrievedPlayers.forEach(player -> {
            Assert.assertTrue("No such player in the original collection", players.containsKey(player.getName()));
            final Player originalPlayer = players.get(player.getName());
            Assert.assertEquals("Strategy GAVs do not match", originalPlayer.getStrategyReleaseId(), player
                    .getStrategyReleaseId());
        });
    }

}

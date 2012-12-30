package org.drooms.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.drooms.api.Collectible;
import org.drooms.api.GameProgressListener;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;
import org.drooms.impl.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlProgressListener implements GameProgressListener {
    private static Logger logger = LoggerFactory.getLogger(XmlProgressListener.class);
    
    private static String collectibleXml(final Collectible c) {
        return "<collectible points='" + c.getPoints() + "' expiresInTurn='"
                + c.expiresInTurn() + "' />";
    }

    private static String nodeXml(final Node c) {
        return "<node x='" + c.getX() + "' y='" + c.getY() + "' />";
    }

    private static String playerXml(final Player p) {
        return "<player name='" + p.getName() + "' />";
    }

    private final StringBuilder report = new StringBuilder();

    private int turnNumber = 0;

    private final Map<Player, Integer> playerPoints = new HashMap<>();

    private boolean prettyPrint = false;
    
    public XmlProgressListener(final Playground p, final Collection<Player> players, Properties gameConfig) {
        // property has to be set exactly to "true" otherwise no pretty printing
        if (gameConfig.getProperty("report.pretty.print", "false").equals("true")) {
            prettyPrint = true;
        }
        this.report.append("<game>");
        // report game config
        this.report.append("<config>");
        for (final Map.Entry<Object, Object> pair : gameConfig.entrySet()) {
            final String key = (String) pair.getKey();
            final String value = (String) pair.getValue();
            this.report.append("<property name='" + key + "' value='" + value
                    + "' />");
        }
        this.report.append("</config>");
        // report players
        this.report.append("<players>");
        for (Player player : players) {
            this.report.append(XmlProgressListener.playerXml(player));
        }
        this.report.append("</players>");
        // report playground
        this.report.append("<playground>");
        for (int x = -1; x <= p.getWidth(); x++) {
            for (int y = -1; y <= p.getHeight(); y++) {
                if (p.isAvailable(x, y)) {
                    this.report.append(XmlProgressListener.nodeXml(Node
                            .getNode(x, y)));
                }
            }
        }
        this.report.append("</playground>");
        this.report.append("<turns>");
    }

    private void addPoints(final Player p, final int points) {
        if (this.playerPoints.containsKey(p)) {
            this.playerPoints.put(p, this.playerPoints.get(p) + points);
        } else {
            this.playerPoints.put(p, points);
        }
    }

    @Override
    public void collectibleAdded(final Collectible c, final Node where) {
        this.report.append("<newCollectible>");
        this.report.append(XmlProgressListener.collectibleXml(c));
        this.report.append(XmlProgressListener.nodeXml(where));
        this.report.append("</newCollectible>");
    }

    @Override
    public void collectibleCollected(final Collectible c, final Player p,
            final Node where, final int points) {
        this.addPoints(p, points);
        this.report.append("<collectedCollectible points='" + points + "'>");
        this.report.append(XmlProgressListener.collectibleXml(c));
        this.report.append(XmlProgressListener.playerXml(p));
        this.report.append(XmlProgressListener.nodeXml(where));
        this.report.append("</collectedCollectible>");
    }

    @Override
    public void collectibleRemoved(final Collectible c, final Node where) {
        this.report.append("<removedCollectible>");
        this.report.append(XmlProgressListener.collectibleXml(c));
        this.report.append(XmlProgressListener.nodeXml(where));
        this.report.append("</removedCollectible>");
    }

    @Override
    public void nextTurn() {
        if (this.turnNumber > 0) {
            this.report.append("</turn>");
        }
        this.report.append("<turn number='" + this.turnNumber + "'>");
        this.turnNumber += 1;
    }

    @Override
    public void playerCrashed(final Player p) {
        this.report.append("<crashedPlayer>");
        this.report.append(XmlProgressListener.playerXml(p));
        this.report.append("</crashedPlayer>");
    }

    @Override
    public void playerDeactivated(final Player p) {
        this.report.append("<deactivatedPlayer>");
        this.report.append(XmlProgressListener.playerXml(p));
        this.report.append("</deactivatedPlayer>");
    }

    @Override
    public void playerMoved(final Player p, final Move m, final Node... nodes) {
        this.report.append("<playerPosition>");
        this.report.append(XmlProgressListener.playerXml(p));
        for (final Node n : nodes) {
            this.report.append(XmlProgressListener.nodeXml(n));
        }
        this.report.append("</playerPosition>");
    }

    @Override
    public void playerSurvived(final Player p, final int points) {
        this.addPoints(p, points);
        this.report.append("<survivedPlayer points='" + points + "'>");
        this.report.append(XmlProgressListener.playerXml(p));
        this.report.append("</survivedPlayer>");
    }

    @Override
    public void write(final Writer w) throws IOException {
        final StringBuilder result = new StringBuilder(this.report);
        if (this.turnNumber > 0) {
            result.append("</turn>");
        }
        result.append("</turns>");
        result.append("<results>");
        for (final Map.Entry<Player, Integer> entry : this.playerPoints
                .entrySet()) {
            result.append("<score points='" + entry.getValue() + "'>");
            result.append(XmlProgressListener.playerXml(entry.getKey()));
            result.append("</score>");
        }
        result.append("</results>");
        result.append("</game>");
        String resultingXml = result.toString();
        // make the xml pretty if specified by property
        // if it fails, error is logged and original string will be returned
        if (prettyPrint) {
            try {
                resultingXml = XmlUtil.prettyPrint(result.toString());
            } catch (RuntimeException re) {
                logger.error("Error while pretty printing XML report, !\n", re);
            }
        }
        w.write(resultingXml);
    }
}

package org.drooms.impl;

import java.io.IOException;
import java.io.Writer;

import org.drooms.api.Collectible;
import org.drooms.api.GameReport;
import org.drooms.api.Move;
import org.drooms.api.Node;
import org.drooms.api.Player;

public class XmlReport implements GameReport<DefaultPlayground> {

    private static String collectibleXml(final Collectible c) {
        return "<collectible type='" + c.getClass().getName() + "' sign='"
                + c.getSign() + "' points='" + c.getPoints() + "' />";
    }

    private static String nodeXml(final Node c) {
        return "<node x='" + c.getX() + "' y='" + c.getY() + "' />";
    }

    private static String playerXml(final Player p) {
        return "<player name='" + p.getName() + "' sign='" + p.getSign()
                + "' />";
    }

    private final StringBuilder report = new StringBuilder();

    private int turnNumber = 0;

    public XmlReport() {
        this.report.append("<game date=''>");
    }

    @Override
    public void collectibleAdded(final Collectible c, final Node where) {
        this.report.append("<newCollectible>");
        this.report.append(XmlReport.collectibleXml(c));
        this.report.append(XmlReport.nodeXml(where));
        this.report.append("</newCollectible>");
    }

    @Override
    public void collectibleCollected(final Collectible c, final Player p,
            final int points) {
        this.report.append("<collectedCollectible points='" + points + "'>");
        this.report.append(XmlReport.collectibleXml(c));
        this.report.append(XmlReport.playerXml(p));
        this.report.append("</collectedCollectible>");
    }

    @Override
    public void collectibleRemoved(final Collectible c) {
        this.report.append("<removedCollectible>");
        this.report.append(XmlReport.collectibleXml(c));
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
        this.report.append(XmlReport.playerXml(p));
        this.report.append("</crashedPlayer>");
    }

    @Override
    public void playerDeactivated(final Player p) {
        this.report.append("<deactivatedPlayer>");
        this.report.append(XmlReport.playerXml(p));
        this.report.append("</deactivatedPlayer>");
    }

    @Override
    public void playerMoved(final Player p, final Move m, final Node... nodes) {
        this.report.append("<playerPosition>");
        this.report.append(XmlReport.playerXml(p));
        for (final Node n : nodes) {
            this.report.append(XmlReport.nodeXml(n));
        }
        this.report.append("</playerPosition>");
    }

    @Override
    public void playerSurvived(final Player p, final int points) {
        this.report.append("<survivedPlayer points='" + points + "'>");
        this.report.append(XmlReport.playerXml(p));
        this.report.append("</survivedPlayer>");
    }

    @Override
    public void write(final Writer w) throws IOException {
        final StringBuilder result = new StringBuilder(this.report);
        if (this.turnNumber > 0) {
            result.append("</turn>");
        }
        result.append("</game>");
        w.write(result.toString());
    }

}

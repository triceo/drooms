package org.drooms.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.drooms.api.Collectible;
import org.drooms.api.GameReport;
import org.drooms.api.Move;
import org.drooms.api.Player;

public class DefaultReport implements
        GameReport<DefaultPlayground, DefaultNode, DefaultEdge> {

    @Override
    public void addCollectible(final Collectible c, final DefaultNode where) {
        // TODO Auto-generated method stub

    }

    @Override
    public void collectCollectible(final Collectible c, final Player p,
            final int points) {
        // TODO Auto-generated method stub

    }

    @Override
    public void crashPlayer(final Player p) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deactivatePlayer(final Player p) {
        // TODO Auto-generated method stub

    }

    @Override
    public void movePlayer(final Player p, final Move m,
            final DefaultNode... nodes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void nextTurn() {
        // TODO Auto-generated method stub

    }

    @Override
    public void playerSurvived(final Player p, final int points) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeCollectible(final Collectible c) {
        // TODO Auto-generated method stub

    }

    @Override
    public void write(final OutputStream s) throws IOException {
        // TODO Auto-generated method stub

    }

}

package org.drooms.impl;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.drooms.api.Collectible;
import org.drooms.api.GameReport;
import org.drooms.api.Player;

public class Reporter implements GameReport<DefaultSituation, DefaultPlayground, DefaultNode, DefaultEdge> {
    
    private static final char WALL = '#';

    private char[][] situationToDrawing(DefaultSituation situation, Set<Player> players) {
        int width = situation.getPlayground().getWidth();
        int height = situation.getPlayground().getHeight();
        char[][] result = new char[height + 2][width + 2];
        // fill the place with white space
        for (int y = 0; y <= height + 1; y++) {
            for (int x = 0; x <= width + 1; x++) {
                result[y][x] = ' ';
            }
        }
        // first draw walls around the edges of the playground
        for (int y = 0; y <= height + 1; y++) {
            result[y][0] = WALL;
            result[y][width + 1] = WALL;
        }
        for (int x = 0; x <= width + 1; x++) {
            result[0][x] = WALL;
            result[height + 1][x] = WALL;
        }
        // then draw walls from the playground
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!situation.getPlayground().isAvailable(x, y)) {
                    result[y + 1][x + 1] = WALL;
                }
            }
        }
        // now draw collectibles
        for (Map.Entry<Collectible, DefaultNode> entry: situation.getCollectibles().entrySet()) {
            DefaultNode node = entry.getValue();
            Collectible collectible = entry.getKey();
            result[node.getY() + 1][node.getX() + 1] = collectible.getSign();
        }
        // and finally, draw players
        for (Player p: players) {
            if (!situation.hasPlayer(p)) {
                continue;
            }
            for (DefaultNode node: situation.getPositions(p)) {
                result[node.getY() + 1][node.getX() + 1] = p.getSign();
            }
        }
        return result;
    }

    @Override
    public void addTurn(DefaultSituation situation, Map<Player, Integer> points) {
        char[][] drawing = situationToDrawing(situation, points.keySet());
        for (char[] line: drawing) {
            System.out.println(line);
        }
        System.out.println();
    }

    @Override
    public void write(OutputStream s) {
        // TODO Auto-generated method stub
        
    }

}

package org.drooms.impl;

import org.drooms.api.Node;
import org.drooms.api.Player;
import org.drooms.api.Playground;

import java.util.*;

public class PlayerPosition {

    /**
     * Represent a player's position in a playground.
     *
     * @param playground The playground in question.
     * @param player The player in question.
     * @param nodes Nodes that the player occupies, head-first.
     * @return Representation of a player's position in a given playground.
     */
    public static PlayerPosition build(Playground playground, Player player, Node... nodes) {
        if (playground == null) {
            throw new IllegalArgumentException("Playground must be provided.");
        } else if (player == null) {
            throw new IllegalArgumentException("Player must be provided.");
        } else if (nodes.length == 0) {
            throw new IllegalArgumentException("At least one node must be provided.");
        }
        // TODO ensure all nodes come from same playground
        // ensure continuous worm
        for (int i = 0; i < nodes.length - 1; i++) {
            final Node node1 = nodes[i];
            final Node node2 = nodes[i + 1];
            final int xDifference = Math.abs(node1.getX() - node2.getX());
            final int yDifference = Math.abs(node1.getY() - node2.getY());
            if (xDifference + yDifference > 1) {
                throw new IllegalArgumentException("Worm not continuous: " + node1 + ", " + node2 + ".");
            }
        }
        return new PlayerPosition(playground, player, nodes);
    }

    public static PlayerPosition build(Playground playground, Player player, Collection<Node> nodes) {
        return PlayerPosition.build(playground, player, nodes.toArray(new Node[nodes.size()]));
    }

    private final Playground playground;
    private final Player player;
    private final List<Node> nodes;

    private PlayerPosition(final Playground playground, final Player player, final Node... nodes) {
        this.playground = playground;
        this.player = player;
        this.nodes = Collections.unmodifiableList(Arrays.asList(nodes));
    }

    public Player getPlayer() {
        return this.player;
    }

    public Playground getPlayground() {
        return this.playground;
    }

    public Collection<Node> getNodes() {
        return this.nodes;
    }

    public Node getHeadNode() {
        return this.nodes.get(0);
    }

    public PlayerPosition reverse() {
        final List<Node> reverseNodes = new ArrayList<>(this.getNodes());
        Collections.reverse(reverseNodes);
        return PlayerPosition.build(this.getPlayground(), this.getPlayer(), new LinkedList<>(reverseNodes));
    }

    public PlayerPosition newHead(final Node newHead) {
        final Deque<Node> newNodes = new LinkedList<>(this.getNodes());
        newNodes.addFirst(newHead);
        return PlayerPosition.build(this.getPlayground(), this.getPlayer(), newNodes);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayerPosition [");
        sb.append("playground=").append(playground);
        sb.append(", player=").append(player);
        sb.append(", nodes=").append(nodes);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerPosition that = (PlayerPosition) o;
        return Objects.equals(playground, that.playground) &&
                Objects.equals(player, that.player) &&
                Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playground, player, nodes);
    }

    public PlayerPosition ensureMaxLength(final int maxLength) {
        if (this.getNodes().size() > maxLength) {
            final Deque<Node> newNodes = new LinkedList<>(this.getNodes());
            while (newNodes.size() != maxLength) {
                newNodes.removeLast();
            }
            return PlayerPosition.build(this.getPlayground(), this.getPlayer(), newNodes);
        }
        return this;
    }

}

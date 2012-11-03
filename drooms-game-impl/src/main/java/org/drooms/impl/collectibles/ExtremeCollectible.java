package org.drooms.impl.collectibles;

import org.drooms.api.Collectible;

public class ExtremeCollectible implements Collectible {

    private final int expiresInTurn, points;

    public ExtremeCollectible(final int points, final int expiresInTurn) {
        if (expiresInTurn < 0) {
            throw new IllegalArgumentException(
                    "Expiration must be a positive number.");
        }
        this.expiresInTurn = expiresInTurn;
        this.points = points;
    }

    @Override
    public boolean expires() {
        return (this.expiresInTurn != Integer.MAX_VALUE);
    }

    @Override
    public int expiresInTurn() {
        return this.expiresInTurn;
    }

    @Override
    public int getPoints() {
        return this.points;
    }

    @Override
    public char getSign() {
        return '*';
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExtremeCollectible [expiresInTurn=")
				.append(expiresInTurn).append(", points=").append(points)
				.append("]");
		return builder.toString();
	}

}

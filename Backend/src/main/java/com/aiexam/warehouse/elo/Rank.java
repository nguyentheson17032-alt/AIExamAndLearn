package com.aiexam.warehouse.elo;

public enum Rank {
    BRONZE(0, 999),
    SILVER(1000, 1299),
    GOLD(1300, 1599),
    PLATINUM(1600, 1899),
    DIAMOND(1900, Integer.MAX_VALUE);

    private final int minElo;
    private final int maxElo;

    Rank(int minElo, int maxElo) {
        this.minElo = minElo;
        this.maxElo = maxElo;
    }

    public int getMinElo() {
        return minElo;
    }

    public int getMaxElo() {
        return maxElo;
    }

    public static Rank fromElo(int elo) {
        for (Rank rank : values()) {
            if (elo >= rank.minElo && elo <= rank.maxElo) {
                return rank;
            }
        }
        return BRONZE;
    }
}

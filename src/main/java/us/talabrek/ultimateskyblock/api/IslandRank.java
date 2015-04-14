package us.talabrek.ultimateskyblock.api;

import java.util.List;

/**
 * Rank for an island.
 */
public class IslandRank extends IslandLevel {
    private final int rank;

    public IslandRank(String islandName, String leaderName, List<String> members, double score, int rank) {
        super(islandName, leaderName, members, score);
        this.rank = rank;
    }

    public IslandRank(IslandLevel level, int rank) {
        this(level.getIslandName(), level.getLeaderName(), level.getMembers(), level.getScore(), rank);
    }

    public int getRank() {
        return rank;
    }
}

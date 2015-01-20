package us.talabrek.ultimateskyblock.island;

/**
 * Simple Immutable POJO for holding the island level.
 */
public class IslandLevel implements Comparable<IslandLevel> {
    private final String islandName;
    private final String leaderName;
    private final String members;
    private final double score;

    public IslandLevel(String islandName, String leaderName, String members, double score) {
        this.islandName = islandName;
        this.leaderName = leaderName;
        this.members = members;
        this.score = score;
    }

    public String getIslandName() {
        return islandName;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public String getMembers() {
        return members;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(IslandLevel o) {
        int cmp = (int) Math.round((o.getScore() - score) * 100);
        if (cmp == 0) {
            cmp = getLeaderName().compareTo(o.getLeaderName());
        }
        return cmp;
    }

    public boolean hasMember(String name) {
        return false;
    }
}

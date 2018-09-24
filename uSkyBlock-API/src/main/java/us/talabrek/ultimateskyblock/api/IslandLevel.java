package us.talabrek.ultimateskyblock.api;

import java.util.List;

/**
 * Simple Immutable POJO for holding the island level.
 * @since v2.1.0
 */
public class IslandLevel implements Comparable<IslandLevel> {
    private final String islandName;
    private final String leaderName;
    private final List<String> members;
    private final double score;

    public IslandLevel(String islandName, String leaderName, List<String> members, double score) {
        this.islandName = islandName;
        this.leaderName = leaderName != null ? leaderName : "\u00a7eAbandoned";
        this.members = members;
        this.score = score;
    }

    /**
     * Returns a logical name of the island on the form <code>x,y</code>
     * @return the name of the island.
     */
    public String getIslandName() {
        return islandName;
    }

    /**
     * Returns the display-name of the island-leader.
     *
     * I.e. <code>\u00a79[Own]\u00a7f R4zorax</code>
     * @return the display-name of the island-leader.
     */
    public String getLeaderName() {
        return leaderName;
    }

    /**
     * Returns a comma-separated list of members, with parenthesis around, or the empty string.
     *
     * I.e. <code>(R4zorax, dutchy1001)</code>
     * @return a comma-separated list of members, with parenthesis around, or the empty string.
     * @since v2.1.1
     */
    public List<String> getMembers() {
        return members;
    }

    /**
     * Returns the score of the island.
     * Each block within the island contributes to the score.
     * But with varying weights (i.e. cobble has a cut-off at 10000 blocks).
     * @return the score of the island.
     */
    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(IslandLevel o) {
        int cmp = (int) Math.round((o.getScore() - score) * 100);
        if (cmp == 0) {
            cmp = getLeaderName().compareTo(o.getLeaderName());
        }
        if (cmp == 0) {
            cmp = getIslandName().compareTo(o.getIslandName());
        }
        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IslandLevel that = (IslandLevel) o;

        if (!islandName.equals(that.islandName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return islandName.hashCode();
    }
}

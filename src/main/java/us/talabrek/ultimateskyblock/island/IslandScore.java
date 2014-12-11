package us.talabrek.ultimateskyblock.island;

import java.util.Collections;
import java.util.List;

/**
 * The summary of island calculation.
 */
public class IslandScore {
    private final double score;
    private final List<BlockScore> top;
    private boolean isSorted = false;

    public IslandScore(double score, List<BlockScore> top) {
        this.score = score;
        this.top = top;
    }

    public double getScore() {
        return score;
    }

    public List<BlockScore> getTop(int num) {
        if (!isSorted) {
            Collections.sort(top, new BlockScoreComparator());
            isSorted = true;
        }
        return top.subList(0, Math.min(num, top.size()));
    }
}

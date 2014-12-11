package us.talabrek.ultimateskyblock.island;

import java.util.Comparator;

/**
 * Comparator that sorts after score.
 */
public class BlockScoreComparator implements Comparator<BlockScore> {
    @Override
    public int compare(BlockScore o1, BlockScore o2) {
        int cmp = (int) (o2.getScore() - o1.getScore());
        if (cmp == 0) {
            cmp = o2.getCount() - o1.getCount();
        }
        return cmp;
    }
}

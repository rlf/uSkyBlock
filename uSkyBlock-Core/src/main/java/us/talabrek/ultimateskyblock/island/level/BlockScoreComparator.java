package us.talabrek.ultimateskyblock.island.level;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import us.talabrek.ultimateskyblock.api.model.BlockScore;

import java.util.Comparator;

/**
 * Comparator that sorts after score.
 */
public class BlockScoreComparator implements Comparator<BlockScore> {
    @Override
    public int compare(BlockScore o1, BlockScore o2) {
        int cmp = (int) Math.round(100*(o2.getScore() - o1.getScore()));
        if (cmp == 0) {
            cmp = o2.getCount() - o1.getCount();
        }
        if (cmp == 0) {
            cmp = ItemStackUtil.getItemName(o2.getBlock()).compareTo(ItemStackUtil.getItemName(o1.getBlock()));
        }
        return cmp;
    }
}

package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.math.BlockVector2;

import java.util.Comparator;

public class ChunkComparator implements Comparator<BlockVector2> {
    private BlockVector2 origin;

    public ChunkComparator(BlockVector2 origin) {
        this.origin = origin;
    }

    @Override
    public int compare(BlockVector2 o1, BlockVector2 o2) {
        int cmp = (int) Math.round(origin.distanceSq(o1) - origin.distanceSq(o2));
        if (cmp == 0) {
            cmp = o1.getBlockX() - o2.getBlockX();
        }
        if (cmp == 0) {
            cmp = o1.getBlockZ() - o2.getBlockZ();
        }
        return cmp;
    }
}

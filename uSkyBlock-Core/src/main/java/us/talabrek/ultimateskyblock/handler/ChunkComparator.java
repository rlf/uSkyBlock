package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.Vector2D;

import java.util.Comparator;

public class ChunkComparator implements Comparator<Vector2D> {
    private Vector2D origin;

    public ChunkComparator(Vector2D origin) {
        this.origin = origin;
    }

    @Override
    public int compare(Vector2D o1, Vector2D o2) {
        int cmp = (int) Math.round(origin.distanceSq(o1) -  origin.distanceSq(o2));
        if (cmp == 0) {
            cmp = o1.getBlockX() - o2.getBlockX();
        }
        if (cmp == 0) {
            cmp = o1.getBlockZ() - o2.getBlockZ();
        }
        return cmp;
    }
}

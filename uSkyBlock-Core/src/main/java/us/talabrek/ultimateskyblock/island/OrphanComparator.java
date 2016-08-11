package us.talabrek.ultimateskyblock.island;

/**
 * A comparator that sorts according to distance from spawn (0,0)
 */
public class OrphanComparator implements java.util.Comparator<OrphanLogic.Orphan> {
    @Override
    public int compare(OrphanLogic.Orphan o1, OrphanLogic.Orphan o2) {
        int cmp = o1.distanceSquared() - o2.distanceSquared();
        if (cmp == 0) {
            cmp = o1.getX() - o2.getX();
        }
        if (cmp == 0) {
            cmp = o1.getZ() - o2.getZ();
        }
        return cmp;
    }
}

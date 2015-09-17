package us.talabrek.ultimateskyblock.island;

/**
 * A comparator that sorts according to distance from spawn (0,0)
 */
public class OrphanComparator implements java.util.Comparator<OrphanLogic.Orphan> {
    @Override
    public int compare(OrphanLogic.Orphan o1, OrphanLogic.Orphan o2) {
        return o1.distanceSquared() - o2.distanceSquared();
    }
}

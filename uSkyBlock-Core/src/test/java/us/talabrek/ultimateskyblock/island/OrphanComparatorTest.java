package us.talabrek.ultimateskyblock.island;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Tests the OrphanComparator
 */
public class OrphanComparatorTest {
    @Test
    public void testSort() throws Exception {
        OrphanLogic.Orphan o1 = new OrphanLogic.Orphan(10, 10);
        OrphanLogic.Orphan o2 = new OrphanLogic.Orphan(-10, 10);
        OrphanLogic.Orphan o3 = new OrphanLogic.Orphan(0, 10);
        List<OrphanLogic.Orphan> list = Arrays.asList(o1,o2,o3);
        List<OrphanLogic.Orphan> expected = Arrays.asList(o3,o2,o1);

        Collections.sort(list, new OrphanComparator());
        assertThat(list, is(expected));
    }

    @Test
    public void testSet() throws Exception {
        OrphanLogic.Orphan o1 = new OrphanLogic.Orphan(10, 10);
        OrphanLogic.Orphan o2 = new OrphanLogic.Orphan(-10, 10);
        OrphanLogic.Orphan o3 = new OrphanLogic.Orphan(0, 10);
        Set<OrphanLogic.Orphan> set = new TreeSet<>(new OrphanComparator());
        set.addAll(Arrays.asList(o1,o2,o3));

        assertThat(set.size(), is(3));
    }
}
package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WorldEditHandlerTest {

    /**
     * Tests that chunk-aligned regions gives out empty borders.
     *
     * <pre>
     *      ^
     *      |
     *   15 |   +------+ +------+
     *      |   |  D   | |  A   |
     *      |   |      | |      |
     *    0 |   +------+ +------+
     *   -1 |   +------+ +------+
     * Z    |   |  C   | |  B   |
     *      |   |      | |      |
     *  -16 |   +------+ +------+
     *      |
     *      +-------------------------------->
     *        -16     -1 0     15
     *                     X
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testGetBorderRegionsAligned() throws Exception {
        // A
        Region region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(15, 15, 15));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("A", borderRegions, is(Collections.<Region>emptySet()));

        // B
        region = new CuboidRegion(BlockVector3.at(0, 0, -16), BlockVector3.at(15, 15, -1));
        borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("B", borderRegions, is(Collections.<Region>emptySet()));

        // C
        region = new CuboidRegion(BlockVector3.at(-16, 0, -16), BlockVector3.at(-1, 15, -1));
        borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("C", borderRegions, is(Collections.<Region>emptySet()));

        // D
        region = new CuboidRegion(BlockVector3.at(-16, 0, 0), BlockVector3.at(-1, 15, 15));
        borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("D", borderRegions, is(Collections.<Region>emptySet()));
    }

    @Test
    public void testBorderXPosMax() {
        Region region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(16, 15, 15));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(16, 0, 0), BlockVector3.at(16, 15, 15))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderXPosMin() {
        Region region = new CuboidRegion(BlockVector3.at(15, 0, 0), BlockVector3.at(31, 15, 15));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(15, 0, 0), BlockVector3.at(15, 15, 15))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderXNegMax() {
        Region region = new CuboidRegion(BlockVector3.at(-16, 0, -16), BlockVector3.at(0, 15, -1));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(0, 0, -16), BlockVector3.at(0, 15, -1))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderXNegMin() {
        Region region = new CuboidRegion(BlockVector3.at(-17, 0, -16), BlockVector3.at(-1, 15, -1));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(-17, 0, -16), BlockVector3.at(-17, 15, -1))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderZPos() {
        Region region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(15, 15, 16));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(0, 0, 16), BlockVector3.at(15, 15, 16))
        ));
        verifySame(borderRegions, expected);
    }

    private void verifySame(Set<Region> borderRegions, Set<Region> expected) {
        Set<String> actual = new HashSet<>();
        Set<String> expString = new HashSet<>();
        for (Region r : borderRegions) {
            actual.add("" + r);
        }
        for (Region r : expected) {
            expString.add("" + r);
        }
        assertThat(actual, is(expString));
    }

    /**
     * Tests that single blocks overlapping regions gives out 1-wide borders.
     * <pre>
     *     ^
     *     |           +---+---+---+---+---+---+
     *     |           |   |   |   |   |   |   |
     *     |           |   |   |   |   |   |   |
     *     |           +---+---+---+---+---+---+
     *     |         l |   | D=======C |   |   |
     *     |           |   | I |   | I |   |   |
     *     |         k +---+-I-R---Q-I-+---+---+
     *     |           |   | I |   | I |   |   |
     *     |           |   | I |   | I |   |   |
     *     |         j +---+-I-O---P-I-+---+---+
     *     |           |   | I |   | I |   |   |
     *     |         i |   | A=======B |   |   |
     *     |           +---+---+---+---+---+---+
     *     |                 a b   c d
     *     +----------------------------------------->
     *
     * Points:
     *     A = (a,i)
     *     B = (d,i)
     *     C = (d,l)
     *     D = (a,l)
     *
     *     M(x) = X mod 16, i.e. Mc = C mod 16.
     *
     * Borders:
     *     O = A + 16 - Ma   | A > 0
     *       = A - Ma        | A <= 0
     *
     *     Q = C - Mc - 1    | C > 0 && Mc != 15
     *       = C + Mc - 16   | C < 0 && Mc != -1
     * </pre>
     *
     * <pre>
     *     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |     |     |     |     |     |     |     |     |
     *     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |x===========x B  |     |     |     |     |     |
     *     +-----+-----+|----+-----+|----+-----+-----+-----+-----+-----+
     *     |     |     ||    |     ||    |     |     |     |     |     |
     *     |     |     ||    |     ||    |     |     |     |     |     |
     *     |     |     ||    |     ||    |     |     |     |     |     |
     *     +-----+-----+|----+-----||----+-----+-----+-----+-----+-----+
     *     |     |     ||    |     ||    |     |     |     |     |     |
     *     |     |     || A  |     ||    |     |     |     |     |     |
     *     |     |     |x===========x    |     |     |     |     |     |
     *     +-----+-----0-----+-----+-----+-----+-----+-----+-----+-----+
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |     |     |     |     |     |     |     |     |
     *     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     *     A =   1,   1
     *     B =  32,  32
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testGetBorderRegionsUnalignedPos() throws Exception {
        Region region = new CuboidRegion(BlockVector3.at(1, 0, 1), BlockVector3.at(32, 15, 32));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expectedBorder = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(1, 0, 1), BlockVector3.at(15, 15, 32)),
                new CuboidRegion(BlockVector3.at(32, 0, 1), BlockVector3.at(32, 15, 32)),
                new CuboidRegion(BlockVector3.at(16, 0, 1), BlockVector3.at(31, 15, 15)),
                new CuboidRegion(BlockVector3.at(16, 0, 32), BlockVector3.at(31, 15, 32))
        ));
        Set<BlockVector2> expectedInner = new HashSet<>(Arrays.asList(
                BlockVector2.at(1, 1)
        ));
        verifySame(borderRegions, expectedBorder);
        Set<BlockVector2> innerChunks = WorldEditHandler.getInnerChunks(region);
        assertThat(innerChunks, is(expectedInner));
    }

    @Test
    public void testGetBorderRegionsAligned4Quadrants() throws Exception {
        Region region = new CuboidRegion(BlockVector3.at(-64, 0, -64), BlockVector3.at(63, 15, 63));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expectedBorder = new HashSet<>(Arrays.<Region>asList());
        Set<BlockVector2> expectedInner = new HashSet<>();
        for (int x = -4; x <= 3; x++) {
            for (int z = -4; z <= 3; z++) {
                expectedInner.add(BlockVector2.at(x, z));
            }
        }
        verifySame(borderRegions, expectedBorder);
        assertThat(WorldEditHandler.getInnerChunks(region), is(expectedInner));
        assertThat(WorldEditHandler.getOuterChunks(region), is(expectedInner));
    }

    /**
     * <pre>
     *     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |     |     |     |     |     |     |     |     |
     *     |     |     |x=======================x B  |     |     |     |
     *     +-----+-----+|----+-----+-----+-----+|----+-----+-----+-----+
     *     |     |     ||    |     |     |     ||    |     |     |     |
     *     |     |     ||    |     |     |     ||    |     |     |     |
     *     +-----+-----+|----+-----+-----+-----+|----+-----+-----+-----+
     *     |     |     ||    |     |     |     ||    |     |     |     |
     *     |     |     ||    |     | 0,0 |     ||    |     |     |     |
     *     +-----+-----+|----+-----0-----+-----+|----+-----+-----+-----+
     *     |     |     ||    |     |     |     ||    |     |     |     |
     *     |     |     ||    |     |     |     ||    |     |     |     |
     *     +-----+-----+|----+-----+-----+-----+|----+-----+-----+-----+
     *     |     |     || A  |     |     |     ||    |     |     |     |
     *     |     |     |x=======================x    |     |     |     |
     *     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     *     A = -31, -31
     *     B =  32,  32
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testGetBorderRegionsUnaligned4Quadrants() throws Exception {
        Region region = new CuboidRegion(BlockVector3.at(-31, 0, -31), BlockVector3.at(32, 15, 32));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expectedBorder = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(BlockVector3.at(-16, 0, 32), BlockVector3.at(31, 15, 32)),
                new CuboidRegion(BlockVector3.at(-16, 0, -31), BlockVector3.at(31, 15, -17)),
                new CuboidRegion(BlockVector3.at(-31, 0, -31), BlockVector3.at(-17, 15, 32)),
                new CuboidRegion(BlockVector3.at(32, 0, -31), BlockVector3.at(32, 15, 32))
        ));
        Set<BlockVector2> expectedInner = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                expectedInner.add(BlockVector2.at(x, z));
            }
        }
        Set<BlockVector2> expectedOuter = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                expectedOuter.add(BlockVector2.at(x, z));
            }
        }

        verifySame(borderRegions, expectedBorder);
        assertThat(WorldEditHandler.getInnerChunks(region), is(expectedInner));
        assertThat(WorldEditHandler.getOuterChunks(region), is(expectedOuter));
    }
}
package us.talabrek.ultimateskyblock.handler;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
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
     * @throws Exception
     */
    @Test
    public void testGetBorderRegionsAligned() throws Exception {
        // A
        Region region = new CuboidRegion(new Vector(0,0,0), new Vector(15, 15, 15));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("A", borderRegions, is(Collections.<Region>emptySet()));

        // B
        region = new CuboidRegion(new Vector(0,0,-16), new Vector(15, 15, -1));
        borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("B", borderRegions, is(Collections.<Region>emptySet()));

        // C
        region = new CuboidRegion(new Vector(-16,0,-16), new Vector(-1, 15, -1));
        borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("C", borderRegions, is(Collections.<Region>emptySet()));

        // D
        region = new CuboidRegion(new Vector(-16,0,0), new Vector(-1, 15, 15));
        borderRegions = WorldEditHandler.getBorderRegions(region);
        assertThat("D", borderRegions, is(Collections.<Region>emptySet()));
    }

    @Test
    public void testBorderXPosMax() {
        Region region = new CuboidRegion(new Vector(0,0,0), new Vector(16, 15, 15));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(new Vector(16,0,0), new Vector(16,15,15))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderXPosMin() {
        Region region = new CuboidRegion(new Vector(15,0,0), new Vector(31, 15, 15));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(new Vector(15,0,0), new Vector(15,15,15))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderXNegMax() {
        Region region = new CuboidRegion(new Vector(-16,0,-16), new Vector(0, 15, -1));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(new Vector(0,0,-16), new Vector(0,15,-1))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderXNegMin() {
        Region region = new CuboidRegion(new Vector(-17,0,-16), new Vector(-1, 15, -1));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(new Vector(-17,0,-16), new Vector(-17,15,-1))
        ));
        verifySame(borderRegions, expected);
    }

    @Test
    public void testBorderZPos() {
        Region region = new CuboidRegion(new Vector(0,0,0), new Vector(15, 15, 16));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expected = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(new Vector(0,0,16), new Vector(15,15,16))
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
     * @throws Exception
     */
    @Test
    public void testGetBorderRegionsUnalignedPos() throws Exception {
        Region region = new CuboidRegion(new Vector(1,0,1), new Vector(32, 15, 32));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expectedBorder = new HashSet<>(Arrays.<Region>asList(
                new CuboidRegion(new Vector(1,0,1), new Vector(15,15,32)),
                new CuboidRegion(new Vector(32,0,1), new Vector(32,15,32)),
                new CuboidRegion(new Vector(16,0,1), new Vector(31,15,15)),
                new CuboidRegion(new Vector(16,0,32), new Vector(31,15,32))
        ));
        Set<Vector2D> expectedInner = new HashSet<>(Arrays.asList(
                new Vector2D(1, 1),
                new Vector2D(1, 2),
                new Vector2D(2, 1),
                new Vector2D(2, 2)
        ));
        verifySame(borderRegions, expectedBorder);
        Set<Vector2D> innerChunks = WorldEditHandler.getInnerChunks(region);
        assertThat(innerChunks, is(expectedInner));
    }

    @Test
    public void testGetBorderRegionsAligned4Quadrants() throws Exception {
        Region region = new CuboidRegion(new Vector(-64,0,-64), new Vector(63, 15, 63));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expectedBorder = new HashSet<>(Arrays.<Region>asList());
        Set<Vector2D> expectedInner = new HashSet<>();
        for (int x = -4; x <= 3; x++) {
            for (int z = -4; z <= 3; z++) {
                expectedInner.add(new Vector2D(x, z));
            }
        }
        verifySame(borderRegions, expectedBorder);
        assertThat(WorldEditHandler.getInnerChunks(region), is(expectedInner));
        assertThat(WorldEditHandler.getOuterChunks(region), is(expectedInner));
    }

    @Test
    public void testGetBorderRegionsUnaligned4Quadrants() throws Exception {
        Region region = new CuboidRegion(new Vector(-64,0,-64), new Vector(63, 15, 63));
        Set<Region> borderRegions = WorldEditHandler.getBorderRegions(region);
        Set<Region> expectedBorder = new HashSet<>(Arrays.<Region>asList());
        Set<Vector2D> expectedInner = new HashSet<>();
        for (int x = -4; x <= 3; x++) {
            for (int z = -4; z <= 3; z++) {
                expectedInner.add(new Vector2D(x, z));
            }
        }
        verifySame(borderRegions, expectedBorder);
        Set<Vector2D> innerChunks = WorldEditHandler.getInnerChunks(region);
        assertThat(innerChunks, is(expectedInner));
    }
}
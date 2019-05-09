package us.talabrek.ultimateskyblock.util;

import org.bukkit.Location;
import org.junit.Test;
import us.talabrek.ultimateskyblock.Settings;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LocationUtilTest {

    @Test
    public void testAlignToDistance0x0() throws Exception {
        Location loc = new Location(null, 0, 0, 0);
        assertThat(LocationUtil.alignToDistance(loc, 1024), is(new Location(null, 0, Settings.island_height, 0)));
    }

    @Test
    public void testAlignToDistance1024x1024() throws Exception {
        Location loc = new Location(null, 1024, 0, 1024);
        assertThat(LocationUtil.alignToDistance(loc, 1024), is(new Location(null, 1024, Settings.island_height, 1024)));

        loc.setX(511.9);
        loc.setZ(512.5);
        assertThat(LocationUtil.alignToDistance(loc, 1024), is(new Location(null, 0, Settings.island_height, 1024)));

        loc.setX(1535.9);
        loc.setZ(512.5);
        assertThat(LocationUtil.alignToDistance(loc, 1024), is(new Location(null, 1024, Settings.island_height, 1024)));

        loc.setX(-511.99);
        loc.setZ(-511.99);
        assertThat(LocationUtil.alignToDistance(loc, 1024), is(new Location(null, 0, Settings.island_height, 0)));

        loc.setX(-512.01);
        loc.setZ(-512.01);
        assertThat(LocationUtil.alignToDistance(loc, 1024), is(new Location(null, -1024, Settings.island_height, -1024)));
    }
}
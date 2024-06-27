package dk.lockfuglsang.minecraft.util;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationUtilTest {
    @BeforeClass
    public static void SetupOnce() throws Exception {
        Server server = BukkitServerMock.setupServerMock();
        when(server.getWorld(anyString())).thenAnswer((a) -> {
            World mock = mock(World.class);
            when(mock.getName()).thenReturn(a.getArguments()[0].toString());
            return mock;
        });
    }

    @Test
    public void asString() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location loc = new Location(world, 1.12, -34.12, 2.0);

        assertThat(LocationUtil.asString(loc), is("world:1.12,-34.12,2.00"));
    }

    @Test
    public void asKey() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location loc = new Location(world, 1.12, -34.12, 2.0);

        assertThat(LocationUtil.asKey(loc), is("world/1_12,-34_12,2_00"));
    }

    @Test
    public void fromString_asString() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location loc = new Location(world, 1.12, -34.12, 2.0);

        String str = "world:1.12,-34.12,2.00";
        assertThat(LocationUtil.asString(LocationUtil.fromString(str)), is(LocationUtil.asString(loc)));
    }

    @Test
    public void fromString_asKey() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        Location loc = new Location(world, 1.12, -34.12, 2.0);

        String key = "world/1_12,-34_12,2_00";
        assertThat(LocationUtil.asString(LocationUtil.fromString(key)), is(LocationUtil.asString(loc)));
    }

}
package us.talabrek.ultimateskyblock;

import org.bukkit.generator.*;
import org.bukkit.*;

import java.util.*;

public class SkyBlockChunkGenerator extends ChunkGenerator {
    private static final BlockPopulator populator = new SkyBlockPopulator();
    private static final byte[] generate = new byte[32768];

    @Override
    public byte[] generate(World world, Random random, int x, int z) {
        return generate;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.singletonList(populator);
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, Settings.island_height, 0);
    }

    private static class SkyBlockPopulator extends BlockPopulator {
        @Override
        public void populate(World world, Random random, Chunk chunk) {
            return;
        }
    }
}

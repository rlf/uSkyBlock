package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyBlockChunkGenerator extends ChunkGenerator {
    private static final List<BlockPopulator> emptyBlockPopulatorList = new ArrayList<>();

    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                biome.setBiome(x, z, Biome.OCEAN);
            }
        }
        return chunkData;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return emptyBlockPopulatorList;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0.5d, Settings.island_height, 0.5d);
    }
}

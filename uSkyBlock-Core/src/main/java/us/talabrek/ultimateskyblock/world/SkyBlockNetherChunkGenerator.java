package us.talabrek.ultimateskyblock.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import us.talabrek.ultimateskyblock.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyBlockNetherChunkGenerator extends ChunkGenerator {
    private static final List<BlockPopulator> emptyBlockPopulatorList = new ArrayList<BlockPopulator>();

    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = 0; y < world.getMaxHeight(); y++) {
                    biome.setBiome(x, y, z, Biome.NETHER_WASTES);
                }
            }
        }
        int y = 0;
        // Solid floor
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, y, z, Material.BEDROCK);
            }
        }
        // Bedrock with holes in it
        for (y = 1; y <= 5; y++) {
            double yThreshold = 0.10 * y;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (random.nextDouble() >= yThreshold) { // 10%-50% air
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        chunkData.setBlock(x, y, z, Material.LAVA);
                    }
                }
            }
        }
        for (y = 6; y <= Settings.nether_lava_level; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    chunkData.setBlock(x, y, z, Material.LAVA);
                }
            }
        }
        y = 120;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (random.nextDouble() >= 0.20) { // 20% air
                    chunkData.setBlock(x, y, z, Material.NETHERRACK);
                }
            }
        }
        y = 121;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, y, z, Material.NETHERRACK);
            }
        }
        for (y = 122; y <= 126; y++) {
            double yThreashold = 0.20 * (127 - y);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (random.nextDouble() >= yThreashold) { // 20%-100% bedrock
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else {
                        chunkData.setBlock(x, y, z, Material.NETHERRACK);
                    }
                }
            }
        }
        y = 127;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, y, z, Material.BEDROCK);
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
        return  new Location(world, 0,  Settings.nether_height, 0);
    }
}

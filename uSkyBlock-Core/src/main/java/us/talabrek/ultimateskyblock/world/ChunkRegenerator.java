package us.talabrek.ultimateskyblock.world;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Arrays;
import java.util.Random;

import static org.bukkit.World.Environment;

/**
 * Class responsible for regenerating chunks.
 */
public class ChunkRegenerator {
    private final ChunkGenerator chunkGen;
    private final World world;

    ChunkRegenerator(@NotNull World world) {
        Validate.notNull(world, "World cannot be null");

        this.world = world;
        this.chunkGen = uSkyBlock.getInstance().getDefaultWorldGenerator(world.getName(), "");
    }

    /**
     * Regenerates the given {@link Chunk}, removing all it's entities except players and setting the default biome.
     * @param chunk Chunk to regenerate.
     */
    public void regenerateChunk(@NotNull Chunk chunk) {
        Validate.notNull(chunk, "Chunk cannot be null");

        Random random = new Random();
        BiomeGrid biomeGrid = new DefaultBiomeGrid(world.getEnvironment());
        ChunkData chunkData = chunkGen.generateChunkData(world, random, chunk.getX(), chunk.getZ(), biomeGrid);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.getBlock(x, 0, z).setBiome(biomeGrid.getBiome(x, z));

                for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                    chunk.getBlock(x, y, z).setBlockData(chunkData.getBlockData(x, y, z));
                }
            }
        }

        removeEntities(chunk);
    }

    /**
     * Removes all the entities within the given {@link Chunk}, except for {@link Player}s.
     * @param chunk Chunk to remove entities in.
     */
    private void removeEntities(@NotNull Chunk chunk) {
        Arrays.stream(chunk.getEntities())
                .filter(entity -> !(entity instanceof Player))
                .forEach(Entity::remove);
    }

    /**
     * Default BiomeGrid used by uSkyBlock when regenerating chunks.
     */
    class DefaultBiomeGrid implements BiomeGrid {
        private Biome defaultBiome;

        DefaultBiomeGrid(Environment env) {
            switch (env) {
                case THE_END:
                    defaultBiome = Biome.THE_END;
                    break;
                case NETHER:
                    defaultBiome = Biome.NETHER;
                    break;
                default:
                    defaultBiome = Biome.OCEAN;
                    break;
            }
        }

        @NotNull
        @Override
        public Biome getBiome(int x, int z) {
            return defaultBiome;
        }

        @Override
        public void setBiome(int x, int z, @NotNull Biome bio) {
            defaultBiome = bio;
        }
    }
}

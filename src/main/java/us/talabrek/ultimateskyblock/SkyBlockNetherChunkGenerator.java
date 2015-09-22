package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyBlockNetherChunkGenerator extends ChunkGenerator {
    private static final byte[] generate = new byte[32768*2];
    static {
        // base is lava, bedrock is randomized
        byte blockId = (byte) Material.LAVA.getId();
        // All the way to y = 32 is LAVA
        for (int y = 0; y <= 32; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    generate[xyzToByte(x, y, z)] = blockId;
                }
            }
        }
        // Trap for players trying to glitch through the roof.
        for (int y = 127; y <= 130; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    generate[xyzToByte(x, y, z)] = blockId;
                }
            }
        }
        blockId = (byte) Material.BEDROCK.getId();
        int y = 126;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                generate[xyzToByte(x, y, z)] = blockId;
            }
        }
        for (y = 131; y <= 140; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    generate[xyzToByte(x, y, z)] = blockId;
                }
            }
        }
    }
    private static final List<BlockPopulator> emptyBlockPopulatorList = new ArrayList<BlockPopulator>();
    public static final byte NETHERRACK = (byte) Material.NETHERRACK.getId();
    public static final byte BEDROCK = (byte) Material.BEDROCK.getId();

    private static int xyzToByte(int x, int y, int z) {
        return (x * 16 + z) * 256 + y;
    }

    private static void generateRandomBlocks(byte[] blocks, Random random) {
        int y = 0;
        // Solid floor
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                blocks[xyzToByte(x, y, z)] = BEDROCK;
            }
        }
        // Bedrock with holes in it
        for (y = 1; y <= 5; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (random.nextDouble() >= (0.10*y)) { // 10%-50% air
                        blocks[xyzToByte(x, y, z)] = BEDROCK;
                    }
                }
            }
        }
        y = 120;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (random.nextDouble() >= 0.20) { // 20% air
                    blocks[xyzToByte(x, y, z)] = NETHERRACK;
                }
            }
        }
        y = 121;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                blocks[xyzToByte(x, y, z)] = NETHERRACK; // solid
            }
        }
        for (y = 122; y < 126; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (random.nextDouble() >= (0.20*(126-y))) { // 20%-100% bedrock
                        blocks[xyzToByte(x, y, z)] = BEDROCK;
                    } else {
                        blocks[xyzToByte(x, y, z)] = NETHERRACK;
                    }
                }
            }
        }
    }

    @Override
    public byte[] generate(World world, Random random, int x, int z) {
        byte[] blocks = generate.clone();
        generateRandomBlocks(blocks, random);
        return blocks;
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return null;
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return null;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return emptyBlockPopulatorList;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, Settings.island_height/2, 0);
    }
}

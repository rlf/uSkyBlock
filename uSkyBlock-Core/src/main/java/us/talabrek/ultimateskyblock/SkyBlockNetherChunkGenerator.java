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

    public static final byte BEDROCK = (byte) Material.BEDROCK.getId();
    public static final byte LAVA = (byte) Material.LAVA.getId();
    public static final byte NETHERRACK = (byte) Material.NETHERRACK.getId();
    public static final int LAVA_LEVEL = 7;

    static {
        // base is lava, bedrock is randomized
        // All the way to y = 32 is LAVA
        for (int y = 0; y <= LAVA_LEVEL; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    generate[xyzToByte(x, y, z)] = LAVA;
                }
            }
        }
        int y = 127;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                generate[xyzToByte(x, y, z)] = BEDROCK;
            }
        }
        for (y = 131; y <= 140; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    generate[xyzToByte(x, y, z)] = BEDROCK;
                }
            }
        }
    }
    private static final List<BlockPopulator> emptyBlockPopulatorList = new ArrayList<BlockPopulator>();


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
            double yThreshold = 0.10 * y;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (random.nextDouble() >= yThreshold) { // 10%-50% air
                        blocks[xyzToByte(x, y, z)] = BEDROCK;
                    } else {
                        blocks[xyzToByte(x, y, z)] = LAVA;
                    }
                }
            }
        }
        for (y = 6; y <= Settings.nether_lava_level; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    blocks[xyzToByte(x, y, z)] = LAVA;
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
        for (y = 122; y <= 126; y++) {
            double yThreashold = 0.20 * (127 - y);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (random.nextDouble() >= yThreashold) { // 20%-100% bedrock
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
        byte[] blocks = new byte[generate.length];
        System.arraycopy(generate, 0, blocks, 0, generate.length);
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
        return  new Location(world, 0,  Settings.nether_height, 0);
    }
}

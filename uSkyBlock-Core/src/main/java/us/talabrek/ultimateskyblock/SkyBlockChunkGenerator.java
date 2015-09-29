package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyBlockChunkGenerator extends ChunkGenerator {
    private static final byte[] generate = new byte[32768];
    private static final byte[][] blockSections = new byte[16][];
    private static final short[][] extBlockSections = new short[16][];
    private static final List<BlockPopulator> emptyBlockPopulatorList = new ArrayList<BlockPopulator>();

    @Override
    public byte[] generate(World world, Random random, int x, int z) {
        return generate;
    }

    @Override
    public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return extBlockSections;
    }

    @Override
    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
        return blockSections;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return emptyBlockPopulatorList;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, Settings.island_height, 0);
    }
}

package us.talabrek.ultimateskyblock;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class SkyBlockChunkGenerator extends ChunkGenerator {

	@Override
	public byte[] generate(final World world, final Random rand, final int chunkx, final int chunkz) {
		final byte[] result = new byte[32768];
		return result;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(new BlockPopulator[0]);
	}
}
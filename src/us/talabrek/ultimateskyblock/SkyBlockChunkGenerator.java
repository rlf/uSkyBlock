package us.talabrek.ultimateskyblock;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class SkyBlockChunkGenerator extends ChunkGenerator {
	@Override
	public byte[] generate(World world, Random random, int cx, int cz) {
		final byte[] result = new byte[32768];

		return result;
	}
}
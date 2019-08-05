package us.talabrek.ultimateskyblock.util;

import org.bukkit.block.Biome;
import org.junit.Assert;
import org.junit.Test;

public class BiomeUtilTest {
    @Test
    public void getBiomeTest() throws Exception {
        Assert.assertEquals(Biome.DARK_FOREST, BiomeUtil.getBiome("DARK_FOREST"));
        Assert.assertEquals(Biome.JUNGLE, BiomeUtil.getBiome("JUNGLE"));

        Assert.assertNull(BiomeUtil.getBiome("INVALID_BIOME"));
    }
}

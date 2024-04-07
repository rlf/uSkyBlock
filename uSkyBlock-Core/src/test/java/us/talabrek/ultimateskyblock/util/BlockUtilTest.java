package us.talabrek.ultimateskyblock.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class BlockUtilTest {
    @Test
    public void testIsBreathable() throws Exception {
        Block fakeBlock = Mockito.mock(Block.class);

        Mockito.when(fakeBlock.getType()).thenReturn(Material.DIRT);
        Assert.assertFalse(BlockUtil.isBreathable(fakeBlock));

        Mockito.when(fakeBlock.getType()).thenReturn(Material.SHORT_GRASS);
        Assert.assertTrue(BlockUtil.isBreathable(fakeBlock));

        Mockito.when(fakeBlock.getType()).thenReturn(Material.WATER);
        Assert.assertFalse(BlockUtil.isBreathable(fakeBlock));
    }

    @Test
    public void testIsFluidMaterial() throws Exception {
        Assert.assertFalse(BlockUtil.isFluid(Material.DIAMOND_BLOCK));
        Assert.assertFalse(BlockUtil.isFluid(Material.LAVA_BUCKET));

        Assert.assertTrue(BlockUtil.isFluid(Material.WATER));
        Assert.assertTrue(BlockUtil.isFluid(Material.LAVA));
    }

    @Test
    public void testIsFluidBlock() throws Exception {
        Block fakeBlock = Mockito.mock(Block.class);

        Mockito.when(fakeBlock.getType()).thenReturn(Material.WATER);
        Assert.assertTrue(BlockUtil.isFluid(fakeBlock));

        Mockito.when(fakeBlock.getType()).thenReturn(Material.AIR);
        Assert.assertFalse(BlockUtil.isFluid(fakeBlock));
    }
}

package us.talabrek.ultimateskyblock.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MaterialUtilTest {
    @Test
    public void testGuava() {
        Preconditions.checkArgument(true,"description", "something to test against");
    }
    @Test
    public void testIsTool() {
        assertThat(MaterialUtil.isTool(Material.OAK_WOOD), is(false));
        assertThat(MaterialUtil.isTool(Material.STONE_BUTTON), is(false));
        assertThat(MaterialUtil.isTool(Material.GOLDEN_CARROT), is(false));
        assertThat(MaterialUtil.isTool(Material.WOODEN_SWORD), is(true));
        assertThat(MaterialUtil.isTool(Material.DIAMOND_AXE), is(true));
        assertThat(MaterialUtil.isTool(Material.STONE_PICKAXE), is(true));
    }

    @Test
    public void testGetToolType() {
        assertThat(MaterialUtil.getToolType(Material.AIR), nullValue());
        assertThat(MaterialUtil.getToolType(Material.GOLDEN_HELMET), nullValue());
        assertThat(MaterialUtil.getToolType(Material.STONE_SLAB), nullValue());
        assertThat(MaterialUtil.getToolType(Material.GOLD_ORE), nullValue());

        assertThat(MaterialUtil.getToolType(Material.GOLDEN_SWORD), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLDEN_AXE), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLDEN_HOE), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLDEN_PICKAXE), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLDEN_SHOVEL), is("GOLD"));

        assertThat(MaterialUtil.getToolType(Material.STONE_SWORD), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_AXE), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_HOE), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_PICKAXE), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_SHOVEL), is("STONE"));

        assertThat(MaterialUtil.getToolType(Material.IRON_SWORD), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_AXE), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_HOE), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_PICKAXE), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_SHOVEL), is("IRON"));

        assertThat(MaterialUtil.getToolType(Material.WOODEN_SWORD), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOODEN_AXE), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOODEN_HOE), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOODEN_PICKAXE), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOODEN_SHOVEL), is("WOOD"));

        assertThat(MaterialUtil.getToolType(Material.DIAMOND_SWORD), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_AXE), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_HOE), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_PICKAXE), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_SHOVEL), is("DIAMOND"));
    }
}
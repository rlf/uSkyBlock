package us.talabrek.ultimateskyblock.util;

import org.bukkit.Material;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.text.MessageFormat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class MaterialUtilTest {
    @Test
    public void testIsTool() {
        assertThat(MaterialUtil.isTool(Material.WOOD), is(false));
        assertThat(MaterialUtil.isTool(Material.STONE_BUTTON), is(false));
        assertThat(MaterialUtil.isTool(Material.DIAMOND_BARDING), is(false));
        assertThat(MaterialUtil.isTool(Material.GOLDEN_CARROT), is(false));
        assertThat(MaterialUtil.isTool(Material.WOOD_SWORD), is(true));
        assertThat(MaterialUtil.isTool(Material.DIAMOND_AXE), is(true));
        assertThat(MaterialUtil.isTool(Material.STONE_PICKAXE), is(true));
    }

    @Test
    public void testGetToolType() {
        assertThat(MaterialUtil.getToolType(Material.AIR), nullValue());
        assertThat(MaterialUtil.getToolType(Material.IRON_BARDING), nullValue());
        assertThat(MaterialUtil.getToolType(Material.GOLD_HELMET), nullValue());
        assertThat(MaterialUtil.getToolType(Material.STONE_SLAB2), nullValue());
        assertThat(MaterialUtil.getToolType(Material.GOLD_ORE), nullValue());

        assertThat(MaterialUtil.getToolType(Material.GOLD_SWORD), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLD_AXE), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLD_HOE), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLD_PICKAXE), is("GOLD"));
        assertThat(MaterialUtil.getToolType(Material.GOLD_SPADE), is("GOLD"));

        assertThat(MaterialUtil.getToolType(Material.STONE_SWORD), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_AXE), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_HOE), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_PICKAXE), is("STONE"));
        assertThat(MaterialUtil.getToolType(Material.STONE_SPADE), is("STONE"));

        assertThat(MaterialUtil.getToolType(Material.IRON_SWORD), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_AXE), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_HOE), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_PICKAXE), is("IRON"));
        assertThat(MaterialUtil.getToolType(Material.IRON_SPADE), is("IRON"));

        assertThat(MaterialUtil.getToolType(Material.WOOD_SWORD), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOOD_AXE), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOOD_HOE), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOOD_PICKAXE), is("WOOD"));
        assertThat(MaterialUtil.getToolType(Material.WOOD_SPADE), is("WOOD"));

        assertThat(MaterialUtil.getToolType(Material.DIAMOND_SWORD), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_AXE), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_HOE), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_PICKAXE), is("DIAMOND"));
        assertThat(MaterialUtil.getToolType(Material.DIAMOND_SPADE), is("DIAMOND"));
    }
}
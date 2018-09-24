package us.talabrek.ultimateskyblock.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Collection;

/**
 * Utility for common block related functions.
 */
public enum BlockUtil {;
    private static final Collection<Material> FLUIDS = Arrays.asList(Material.WATER, Material.LAVA);

    public static boolean isBreathable(Block block) {
        return !block.getType().isSolid() && !isFluid(block);
    }

    public static boolean isFluid(Block block) {
        return isFluid(block.getType());
    }

    public static boolean isFluid(Material material) {
        return FLUIDS.contains(material);
    }

}

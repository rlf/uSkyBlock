package us.talabrek.ultimateskyblock.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Collection;

/**
 * Utility for common block related functions.
 */
public enum BlockUtil {;
    private static final Collection<Material> FLUIDS = Arrays.asList(Material.STATIONARY_WATER, Material.WATER, Material.LAVA, Material.STATIONARY_LAVA);

    public static boolean isBreathable(Block block) {
        return !block.getType().isSolid() && !isFluid(block);
    }

    public static boolean isFluid(Block block) {
        return FLUIDS.contains(block.getType());
    }

    public static boolean isFluid(int type) {
        return FLUIDS.contains(Material.getMaterial(type));
    }
}

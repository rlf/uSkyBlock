package us.talabrek.ultimateskyblock.util;

import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to support the idiotic compatibility issues reg. biomes
 */
public enum BiomeUtil {;
    private static final Map<String,String> biomeAlias = new HashMap<>();
    static {
        biomeAlias.put("ICE_PLAINS", "ICE_FLATS"); // Bukkit 1.9 -> 1.10
        biomeAlias.put("FLOWER_FOREST", "MUTATED_FOREST"); // Bukkit 1.8 -> 1.9
    }

    public static Biome getBiome(String name) {
        try {
            return Biome.valueOf(name);
        } catch (IllegalArgumentException e) {
            if (biomeAlias.containsKey(name)) {
                return getBiome(biomeAlias.get(name));
            }
        }
        return null;
    }
}

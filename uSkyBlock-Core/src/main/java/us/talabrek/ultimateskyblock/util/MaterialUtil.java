package us.talabrek.ultimateskyblock.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Functions for working with Materials
 */
public enum MaterialUtil {
    ;
    private static final Pattern MATERIAL_PROBABILITY = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?\\s*(?<id>[A-Z_0-9]+)");
    private static final Collection<Material> SANDS = Arrays.asList(Material.SAND, Material.GRAVEL);
    private static final Collection<Material> WOOD_TOOLS = Arrays.asList(Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL, Material.WOODEN_SWORD);
    private static final Collection<Material> STONE_TOOLS = Arrays.asList(Material.STONE_AXE, Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SHOVEL, Material.STONE_SWORD);
    private static final Collection<Material> IRON_TOOLS = Arrays.asList(Material.IRON_AXE, Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_SWORD);
    private static final Collection<Material> GOLD_TOOLS = Arrays.asList(Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_SWORD);
    private static final Collection<Material> DIAMOND_TOOLS = Arrays.asList(Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_SWORD);
    private static final Collection<Material> TOOLS = new ArrayList<>();
    static {
        TOOLS.addAll(WOOD_TOOLS);
        TOOLS.addAll(STONE_TOOLS);
        TOOLS.addAll(IRON_TOOLS);
        TOOLS.addAll(GOLD_TOOLS);
        TOOLS.addAll(DIAMOND_TOOLS);
    }

    public static boolean isTool(Material type) {
        return TOOLS.contains(type);
    }

    public static String getToolType(Material tool) {
        if (isTool(tool)) {
            String enumName = tool.name();
            String typeName = enumName.substring(0, enumName.indexOf('_')).toUpperCase();
            // GOLDEN and WOODEN -> GOLD and WOOD
            if (typeName.endsWith("EN")) {
                return typeName.substring(0, typeName.length()-2);
            }
            return typeName;
        }
        return null;
    }

    public static Material getMaterial(String name, Material fallback) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public static boolean isFallingMaterial(Material mat) {
        return SANDS.contains(mat);
    }

    public static List<MaterialProbability> createProbabilityList(List<String> matList) {
        List<MaterialProbability> list = new ArrayList<>();
        for (String line : matList) {
            Matcher m = MATERIAL_PROBABILITY.matcher(line);
            if (m.matches()) {
                Material mat = Material.getMaterial(m.group("id"));
                if (mat == null) {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown material: " + line);
                    continue;
                }
                double p = m.group("prob") != null ? Double.parseDouble(m.group("prob")) : 1;
                list.add(new MaterialProbability(mat, p));
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Unknown material: " + line);
            }
        }
        return list;
    }

    public static class MaterialProbability {
        private final Material material;
        private final double probability;

        public MaterialProbability(Material material, double probability) {
            this.material = material;
            this.probability = probability;
        }

        public Material getMaterial() {
            return material;
        }

        public double getProbability() {
            return probability;
        }
    }
}

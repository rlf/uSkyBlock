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
public enum MaterialUtil {;
    private static final Pattern MATERIAL_PROBABILITY = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?\\s*(?<id>[A-Z_0-9]+)");
    private static final Collection<Material> SANDS = Arrays.asList(Material.SAND, Material.GRAVEL);

    public static boolean isFallingMaterial(Material mat) {
        return SANDS.contains(mat);
    }

    public static List<MaterialProbability> createProbabilityList(List<String> matList) {
        List<MaterialProbability> list = new ArrayList<>();
        for (String line : matList) {
            Matcher m = MATERIAL_PROBABILITY.matcher(line);
            if (m.matches()) {
                Material mat = Material.getMaterial(m.group("id"));
                double p = m.group("prob") != null ? Double.parseDouble(m.group("prob")) : 1;
                list.add(new MaterialProbability(mat, p));
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Misconfigured list of materials: "+ line);
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

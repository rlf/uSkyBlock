package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertNotNull;

public class LevelConfigFileTest {
    @Test
    public void testForInvalidMaterials() {
        InputStream levelResource = getClass().getClassLoader().getResourceAsStream("imported/levelConfig.yml");
        YamlConfiguration levelConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(levelResource));

        for (String key : levelConfig.getConfigurationSection("blocks").getKeys(false)) {
            assertNotNull(Material.getMaterial(key));

            if (levelConfig.getConfigurationSection("blocks").getConfigurationSection(key).contains("additionalBlocks")) {
                for (String additionalKey : levelConfig.getConfigurationSection("blocks").getConfigurationSection(key).getStringList("additionalBlocks")) {
                    assertNotNull(Material.getMaterial(additionalKey));
                }
            }
        }
    }
}

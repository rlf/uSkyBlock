package dk.lockfuglsang.minecraft.yml;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertThat;

public class YmlConfigurationTest {
    @Test
    public void testGetStringList_List() throws Exception {
        File simpleYml = new File(getClass().getClassLoader().getResource("yml/simple.yml").toURI());
        YamlConfiguration config = new YamlConfiguration();
        config.load(simpleYml);

        List<String> actual = config.getStringList("root.child node.some-section.another-list");
        assertThat(actual, Matchers.contains("what now", "do you know?"));
    }

    @Test
    public void testGetStringList_OneLineList() throws Exception {
        File simpleYml = new File(getClass().getClassLoader().getResource("yml/simple.yml").toURI());
        YamlConfiguration config = new YamlConfiguration();
        config.load(simpleYml);

        List<String> actual = config.getStringList("root.child node.some-section.section-list");
        assertThat(actual, Matchers.contains("Hej", "Mor"));
    }
}

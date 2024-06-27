package dk.lockfuglsang.minecraft.yml;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by R4zorax on 15/07/2016.
 */
public class YmlConfigurationTest {
    @Test
    public void saveToString() throws Exception {
        File simpleYml = new File(getClass().getClassLoader().getResource("yml/simple.yml").toURI());
        YamlConfiguration config = new YamlConfiguration();
        config.load(simpleYml);

        config.set("root.child node.abe", "lincoln\nwas a wonderful\npresident");
        String expected = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("yml/simple_expected.yml"), StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining(System.lineSeparator()));
        config.save(new File(simpleYml.getParent(), "new_actual.yml"));
        assertThat(config.saveToString(), is(expected));
    }

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

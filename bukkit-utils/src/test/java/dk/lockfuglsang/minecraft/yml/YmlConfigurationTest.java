package dk.lockfuglsang.minecraft.yml;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by R4zorax on 15/07/2016.
 */
public class YmlConfigurationTest {
    @Test
    public void saveToString() throws Exception {
        File simpleYml = new File(getClass().getClassLoader().getResource("yml/simple.yml").toURI());
        YmlConfiguration config = new YmlConfiguration();
        config.load(simpleYml);

        config.set("root.child node.abe", "lincoln\nwas a wonderful\npresident");
        String expected = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("yml/simple_expected.yml").toURI())), "UTF-8");
        assertThat(config.saveToString(), is(expected));
    }

}
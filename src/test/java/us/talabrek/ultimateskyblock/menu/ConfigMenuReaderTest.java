package us.talabrek.ultimateskyblock.menu;

import com.sk89q.worldedit.util.YAMLConfiguration;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ConfigMenuReaderTest {

    @Test
    public void testReadMenusSimple() throws Exception {
        Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("menus.yml"), "UTF-8");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
        List<Menu> menus = ConfigMenuReader.readMenus(config.getConfigurationSection("menus"));

        assertNotNull(menus);
        assertThat(menus.size(), is(1));
        Menu menu = menus.get(0);
        MenuItem menuItem2 = menu.getItems().get(1);
        assertThat(menuItem2.getCommands(), is(Arrays.asList("c")));
        assertThat(menuItem2.getIndex(), is(3));
        assertThat(menus.get(0).getTitle(), is("\u00a79Island Menu"));
    }
}
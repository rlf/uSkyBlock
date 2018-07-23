package us.talabrek.ultimateskyblock.challenge;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.nbt.NBTItemStackTagger;
import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import dk.lockfuglsang.minecraft.util.FormatUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.Ignore;
import org.junit.Test;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChallengeLogicTest {

    @Test
    public void testWordWrap() {
        String test = "a little version that doesn't mean anything, but should be broken on multiple lines";
        List<String> expected = Arrays.asList(
                "a little",
                "version that doesn't",
                "mean anything,",
                "but should be broken",
                "on multiple lines");

        assertThat(FormatUtil.wordWrap(test, 8, 15), is(expected));
    }

    @Test
    @Ignore("Bukkit 1.13 uses server.Unsafe(), which is NULL in test-runner")
    public void testDefaultChallengesYml() throws Exception {
        setupServerMock();
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, new File("src/main/resources/challenges.yml"));
        uSkyBlock plugin = mock(uSkyBlock.class);
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getDataFolder()).thenReturn(new File("target/test-classes"));
        ChallengeLogic sut = new ChallengeLogic(config, plugin);
        // Adjust these values accordingly
        assertThat(sut.getRanks().size(), is(6));
        assertThat(sut.getAllChallengeNames().size(), is(54));
    }

    private static Server setupServerMock() throws NoSuchFieldException, IllegalAccessException {
        NBTUtil.setNBTItemStackTagger(new NBTItemStackTagger() {
            @Override
            public String getNBTTag(ItemStack itemStack) {
                return null;
            }

            @Override
            public ItemStack setNBTTag(ItemStack itemStack, String tag) {
                return null;
            }

            @Override
            public ItemStack addNBTTag(ItemStack itemStack, String tag) {
                return null;
            }
        });
        Field server = Bukkit.class.getDeclaredField("server");
        server.setAccessible(true);
        Server serverMock = createServerMock();
        server.set(null, serverMock);
        server.setAccessible(false);
        return serverMock;
    }

    private static Server createServerMock() {
        Server serverMock = mock(Server.class);
        ItemFactory itemFactoryMock = mock(ItemFactory.class);
        when(serverMock.getItemFactory()).thenReturn(itemFactoryMock);
        PluginManager pluginManagerMock = mock(PluginManager.class);
        when(serverMock.getPluginManager()).thenReturn(pluginManagerMock);
        return serverMock;
    }
}
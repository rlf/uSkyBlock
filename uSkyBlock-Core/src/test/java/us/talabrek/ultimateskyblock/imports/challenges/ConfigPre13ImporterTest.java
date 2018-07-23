package us.talabrek.ultimateskyblock.imports.challenges;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.junit.BeforeClass;
import org.junit.Test;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.InputStream;

import static dk.lockfuglsang.minecraft.util.BukkitServerMock.createServerMock;
import static dk.lockfuglsang.minecraft.util.BukkitServerMock.setupServerMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigPre13ImporterTest {

    @BeforeClass
    public static void SetupAll() throws Exception {
        Server server = setupServerMock();
        UnsafeValues unsafeMock = mock(UnsafeValues.class);
        when(unsafeMock.fromLegacy((Material) any())).thenAnswer(a -> a.getArguments()[0]);
        when(server.getUnsafe()).thenReturn(unsafeMock);

    }

    @Test
    public void importFile_Default_ConfigYml() throws Exception {
        File configFile = File.createTempFile("config", ".dir");
        configFile.delete();
        configFile.deleteOnExit();
        configFile = new File(configFile, "config.yml");
        configFile.deleteOnExit();
        configFile.getParentFile().mkdirs();
        InputStream resourceAsStream = ConfigPre13Importer.class.getClassLoader().getResourceAsStream("config.yml");
        FileUtil.copy(resourceAsStream, configFile);
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, configFile);

        uSkyBlock plugin = mock(uSkyBlock.class);
        when(plugin.getConfig()).thenReturn(config);
        ConfigPre13Importer sut = new ConfigPre13Importer();
        sut.init(plugin);
        assertThat(sut.importFile(configFile), is(true));
    }
}
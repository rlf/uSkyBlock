package us.talabrek.ultimateskyblock.island.level.yml;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfig;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigMap;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LevelConfigYmlReaderTest {

    @BeforeClass
    public static void SetUpStuff() throws NoSuchFieldException, IllegalAccessException {
        uSkyBlock plugin = mock(uSkyBlock.class);
        Field field = uSkyBlock.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, plugin);
        field.setAccessible(false);
        when(plugin.getLogger()).thenReturn(Logger.getGlobal());
    }
    @Test
    public void readLevelConfig_LegacyVsNew() {
        LevelConfigYmlReader newReader = new LevelConfigYmlReader();
        YmlConfiguration newConfig = new YmlConfiguration();
        FileUtil.readConfig(newConfig, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig_v100.yml"));
        BlockLevelConfigMap newMap = newReader.readLevelConfig(newConfig);

        LegacyLevelConfigYmlReader oldReader = new LegacyLevelConfigYmlReader();
        YmlConfiguration oldConfig = new YmlConfiguration();
        FileUtil.readConfig(oldConfig, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig.yml"));
        BlockLevelConfigMap oldMap = oldReader.readLevelConfig(oldConfig);

        List<BlockLevelConfig> newValues = newMap.values();
        List<BlockLevelConfig> oldValues = oldMap.values();
        List<BlockLevelConfig> diff = new ArrayList<>(oldValues);
        diff.removeAll(newValues);
        List<BlockLevelConfig> diff2 = new ArrayList<>(newValues);
        diff2.removeAll(oldValues);
        assertThat("new is not equal to old", diff, equalTo(diff2));
        assertThat(newMap.getDefault(), equalTo(oldMap.getDefault()));
    }
}
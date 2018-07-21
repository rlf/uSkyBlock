package us.talabrek.ultimateskyblock.island.level.yml;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Material;
import org.junit.Test;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfig;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigBuilder;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigMap;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LegacyLevelConfigYmlReaderTest {
    @Test
    public void testDistinctValuesOfMap() {
        Map<String, BlockLevelConfigBuilder> builderMap = new HashMap<>();
        BlockLevelConfigBuilder builderA = new BlockLevelConfigBuilder();
        BlockLevelConfigBuilder builderB = builderA.limit(100).copy().scorePerBlock(20);
        builderMap.put("a", builderA);
        builderMap.put("b", builderB);
        builderMap.put("ace", builderA);

        assertThat(builderMap.values().stream().distinct().count(), is(2L));
    }

    @Test
    public void testReadingOfLevelConfig_blockValues() {
        LegacyLevelConfigYmlReader reader = new LegacyLevelConfigYmlReader();
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig_blockValuesOnly.yml"));
        BlockLevelConfigMap map = reader.readLevelConfig(config);
        assertThat(map.values().size(), is(greaterThan(0)));

        // additional blocks (including direct adressing).
        BlockLevelConfig logConfig = map.get(Material.LOG);
        assertThat(logConfig.calculateScore(1).getScore(), is(10d));
        assertThat(logConfig.matches(Material.LOG, (byte)5), is(true));
        assertThat(logConfig.matches(Material.LOG_2, (byte)1), is(true)); // DARK_OAK_LOG

        // separate values for each dataValue
        //  '42': 300 # IRON_BLOCK
        //  '43/0': 10 # DOUBLE STONE SLAB (DOUBLE_STEP in 1.12)
        //  '43/1': 20
        //  '43/2-3': 10
        //  '43/4': 50
        //  '43/5': 15
        //  '43/6': 20
        //  '43/7': 100
        logConfig = map.get(Material.IRON_BLOCK);
        assertThat(logConfig.calculateScore(1).getScore(), is(300d));
        logConfig = map.get(Material.DOUBLE_STEP);
        assertThat(logConfig.calculateScore(1).getScore(), is(10d));
        assertScore(map, Material.DOUBLE_STEP, 0, 10);
        assertScore(map, Material.DOUBLE_STEP, 1, 20);
        assertScore(map, Material.DOUBLE_STEP, 2, 10);
        assertScore(map, Material.DOUBLE_STEP, 3, 10);
        assertScore(map, Material.DOUBLE_STEP, 4, 50);
        assertScore(map, Material.DOUBLE_STEP, 5, 15);
        assertScore(map, Material.DOUBLE_STEP, 6, 20);
        assertScore(map, Material.DOUBLE_STEP, 7, 100);
        assertScore(map, Material.DOUBLE_STEP, 8, 10);
    }

    private void assertScore(BlockLevelConfigMap map, Material type, int dataValue, double score) {
        BlockLevelConfig levelConfig = map.get(type, (byte) (dataValue & 0xff));
        assertThat("wrong score for " + levelConfig, levelConfig.calculateScore(1).getScore(), is(score));
    }

    @Test
    public void testReadingOfLevelConfig_defaults() {
        LegacyLevelConfigYmlReader reader = new LegacyLevelConfigYmlReader();
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig.yml"));
        BlockLevelConfigMap map = reader.readLevelConfig(config);
        assertThat(map.values().size(), is(greaterThan(0)));

        // additional blocks (including direct adressing).
        double score = 0d;
        BlockLevelConfig logConfig = map.get(Material.AIR);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));

        logConfig = map.get(Material.WATER);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));

        logConfig = map.get(Material.LAVA);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));
    }

    @Test
    public void testReadingOfLevelConfig_limits() {
        LegacyLevelConfigYmlReader reader = new LegacyLevelConfigYmlReader();
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig.yml"));
        BlockLevelConfigMap map = reader.readLevelConfig(config);
        assertThat(map.values().size(), is(greaterThan(0)));

        // additional blocks (including direct adressing).
        double score = 1000d;
        BlockLevelConfig logConfig = map.get(Material.BED_BLOCK);
        System.out.println(logConfig);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));
        assertThat(logConfig.calculateScore(2).getScore(), is(score));

        score = 50d;
        logConfig = map.get(Material.SPONGE);
        System.out.println(logConfig);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));
        assertThat(logConfig.calculateScore(20).getScore(), is(20*score));
        assertThat(logConfig.calculateScore(21).getScore(), is(20*score));
        assertThat(logConfig, not(map.get(Material.SPONGE, (byte)1))); // Separate scores means separate counts

        score = 150d;
        logConfig = map.get(Material.ANVIL);
        System.out.println(logConfig);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));
        assertThat(logConfig.calculateScore(5).getScore(), is(5*score));
        assertThat(logConfig.calculateScore(6).getScore(), is(5*score));
        assertThat(logConfig, is(map.get(Material.ANVIL, (byte)1)));
        assertThat(logConfig, is(map.get(Material.ANVIL, (byte)2)));
        assertThat(logConfig, not(map.get(Material.ANVIL, (byte)3)));
    }

    @Test
    public void testReadingOfLevelConfig_diminishingReturns() {
        LegacyLevelConfigYmlReader reader = new LegacyLevelConfigYmlReader();
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig.yml"));
        BlockLevelConfigMap map = reader.readLevelConfig(config);
        assertThat(map.values().size(), is(greaterThan(0)));

        // additional blocks (including direct adressing).
        double score = 20d;
        BlockLevelConfig logConfig = map.get(Material.DIRT);
        System.out.println(logConfig);
        assertThat(logConfig.calculateScore(10000).getScore(), is(10000*score));
        assertThat(logConfig.calculateScore(10001).getScore(), is(lessThan(10001*score)));

        score = 25d;
        BlockLevelConfig logConfig2 = map.get(Material.DIRT, (byte) 1);
        assertThat(logConfig, not(logConfig2));
        assertThat(logConfig2.calculateScore(10000).getScore(), is(10000*score));
        assertThat(logConfig2.calculateScore(10001).getScore(), is(lessThan(10001*score)));
    }

    @Test
    public void testReadingOfLevelConfig_negativeReturns() {
        LegacyLevelConfigYmlReader reader = new LegacyLevelConfigYmlReader();
        YmlConfiguration config = new YmlConfiguration();
        FileUtil.readConfig(config, getClass().getClassLoader().getResourceAsStream("levelConfig/levelConfig.yml"));
        BlockLevelConfigMap map = reader.readLevelConfig(config);
        assertThat(map.values().size(), is(greaterThan(0)));

        // negativeReturns < blockLimit means negativeReturns wins
        double score = 125d;
        BlockLevelConfig logConfig = map.get(Material.HOPPER);
        System.out.println(logConfig);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));
        assertThat(logConfig.calculateScore(5).getScore(), is(5*score));
        assertThat(logConfig.calculateScore(6).getScore(), is(4*score));
        assertThat(logConfig.calculateScore(10).getScore(), is(0d));
        assertThat(logConfig.calculateScore(11).getScore(), is(-score));

        // negativeReturns > blockLimit gives:
        //    score
        //      ^       .
        //      |     _____
        //      |   /       \
        //      |  /         \
        //      +-------------\-------> blockCount
        //      |              \
        //           ^  ^
        //       limit   negativeReturns
        score = 2000d;
        // limit = 10
        // negativeReturns = 15
        logConfig = map.get(Material.BEACON);
        System.out.println(logConfig);
        assertThat(logConfig.calculateScore(1).getScore(), is(score));
        assertThat(logConfig.calculateScore(10).getScore(), is(10*score));
        assertThat(logConfig.calculateScore(11).getScore(), is(10*score));
        assertThat(logConfig.calculateScore(15).getScore(), is(10*score));
        assertThat(logConfig.calculateScore(16).getScore(), is(10*score));
        assertThat(logConfig.calculateScore(20).getScore(), is(10*score));
        assertThat(logConfig.calculateScore(21).getScore(), is(9*score));
    }

}
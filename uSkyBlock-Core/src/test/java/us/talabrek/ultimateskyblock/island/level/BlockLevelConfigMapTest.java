package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BlockLevelConfigMapTest {

    @Test
    public void get() {
        BlockLevelConfigBuilder defaultBuilder = new BlockLevelConfigBuilder().limit(17).scorePerBlock(10);
        List<BlockLevelConfig> collection = new ArrayList<>();
        collection.add(defaultBuilder.copy().base(Material.AIR).scorePerBlock(0).build());
        collection.add(defaultBuilder.copy().base(Material.STONE).scorePerBlock(12).build());
        collection.add(defaultBuilder.copy().base(Material.OAK_WOOD).scorePerBlock(9)
                .additionalBlocks(new BlockMatch(Material.OAK_LOG, new byte[]{0, 1, 2, 3, 4}))
                .build());
        BlockLevelConfigMap map = new BlockLevelConfigMap(collection, defaultBuilder);

        assertThat(map.get(Material.AIR).getScorePerBlock(), is(0d));
        assertThat(map.get(Material.AIR, (byte) 1).getScorePerBlock(), is(0d));
    }
}
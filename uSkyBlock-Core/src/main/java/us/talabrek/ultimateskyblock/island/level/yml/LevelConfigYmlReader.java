package us.talabrek.ultimateskyblock.island.level.yml;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfig;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigBuilder;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigMap;
import us.talabrek.ultimateskyblock.island.level.BlockMatch;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelConfigYmlReader {
    private static final Pattern BLOCK_KEY_PATTERN = Pattern.compile("(?<type>[A-Z0-9_]+)?");

    public BlockLevelConfigMap readLevelConfig(FileConfiguration config) {
        double defaultScore = config.getDouble("general.default", 10d);
        int defaultLimit = config.getInt("general.limit", Integer.MAX_VALUE);
        int defaultDiminishingReturns = config.getInt("general.diminishingReturns", 0);
        BlockLevelConfigBuilder defaultBuilder = new BlockLevelConfigBuilder()
                .scorePerBlock(defaultScore)
                .limit(defaultLimit)
                .diminishingReturns(defaultDiminishingReturns);
        List<BlockLevelConfig> blocks = new ArrayList<>();
        addDefaults(blocks, defaultBuilder);
        ConfigurationSection section = config.getConfigurationSection("blocks");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (section.isConfigurationSection(key)) {
                    BlockLevelConfig blockConfig = readBlockSection(section.getConfigurationSection(key), getBlockMatch(key), defaultBuilder);
                    blocks.add(blockConfig);
                }
            }
        }
        return new BlockLevelConfigMap(blocks, defaultBuilder);
    }

    private BlockLevelConfig readBlockSection(ConfigurationSection section, BlockMatch blockMatch, BlockLevelConfigBuilder defaultBuilder) {
        BlockLevelConfigBuilder builder = defaultBuilder.copy()
                .base(blockMatch);
        double score = section.getDouble("score", -1);
        if (score >= 0) {
            builder.scorePerBlock(score);
        }
        int limit = section.getInt("limit", -1);
        if (limit >= 0) {
            builder.limit(limit);
        }
        int diminishingReturns = section.getInt("diminishingReturns", -1);
        if (diminishingReturns >= 0) {
            builder.diminishingReturns(diminishingReturns);
        }
        int negativeReturns = section.getInt("negativeReturns", -1);
        if (negativeReturns >= 0) {
            builder.negativeReturns(negativeReturns);
        }
        List<String> additionBlocks = section.getStringList("additionalBlocks");
        if (!additionBlocks.isEmpty()) {
            builder.additionalBlocks(additionBlocks.stream().map(this::getBlockMatch).toArray(BlockMatch[]::new));
        }
        return builder.build();
    }

    private BlockMatch getBlockMatch(String blockKey) {
        Matcher m = BLOCK_KEY_PATTERN.matcher(blockKey);
        if (m.matches()) {
            Material material;
            String materialName = m.group("type");
            material = Material.matchMaterial(materialName);
            if (material == null) {
                material = Material.matchMaterial(materialName, true);
            }
            if (material == null) {
                LogUtil.log(Level.WARNING, "Invalid key '" + blockKey + "' in levelConfig, could not lookup Material");
                return null;
            }
            return new BlockMatch(material);
        } else {
            LogUtil.log(Level.WARNING, "Invalid key '" + blockKey + "' in levelConfig");
        }
        return null;
    }

    private void addDefaults(List<BlockLevelConfig> blocks, BlockLevelConfigBuilder defaultBuilder) {
        BlockLevelConfigBuilder nullScore = defaultBuilder.copy().scorePerBlock(0).limit(0);
        blocks.add(nullScore.base(Material.AIR).build());
    }
}

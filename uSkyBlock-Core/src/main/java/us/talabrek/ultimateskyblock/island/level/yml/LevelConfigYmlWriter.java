package us.talabrek.ultimateskyblock.island.level.yml;

import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfig;
import us.talabrek.ultimateskyblock.island.level.BlockLevelConfigMap;

import java.util.Comparator;
import java.util.stream.Collectors;

public class LevelConfigYmlWriter {

    public YmlConfiguration writeToConfig(YmlConfiguration config, BlockLevelConfigMap map) {
        Configuration root = config.getRoot();
        ConfigurationSection blocks = root.createSection("blocks");
        BlockLevelConfig mapDefault = map.getDefault();
        map.values().stream()
                .distinct()
                .filter(f -> !isDefaultValues(f, mapDefault))
                .sorted(Comparator.comparing(BlockLevelConfig::getKey))
                .forEach(e -> writeToSection(blocks.createSection(createSectionKey(e)), e, mapDefault));
        return config;
    }

    private String createSectionKey(BlockLevelConfig e) {
        return e.getKey().toString();
    }

    private static boolean isDefaultValues(BlockLevelConfig c1, BlockLevelConfig c2) {
        return c1.getLimit() == c2.getLimit()
                && c1.getScorePerBlock() == c2.getScorePerBlock()
                && c1.getDiminishingReturns() == c2.getDiminishingReturns()
                && c1.getNegativeReturns() == c2.getNegativeReturns();
    }

    private void writeToSection(ConfigurationSection section, BlockLevelConfig config, BlockLevelConfig mapDefault) {
        if (!config.getAdditionalBlocks().isEmpty()) {
            section.set("additionalBlocks", config.getAdditionalBlocks().stream().distinct().map(m -> m.toString()).collect(Collectors.toList()));
        }
        if (config.getScorePerBlock() >= 0 && config.getScorePerBlock() != mapDefault.getScorePerBlock()) {
            section.set("score", config.getScorePerBlock());
        }
        if (config.getLimit() >= 0 && config.getLimit() != mapDefault.getLimit()) {
            section.set("limit", config.getLimit());
        }
        if (config.getDiminishingReturns() > 0 && config.getLimit() != mapDefault.getDiminishingReturns()) {
            section.set("diminishingReturns", config.getDiminishingReturns());
        }
        if (config.getNegativeReturns() > 0 && config.getLimit() != mapDefault.getNegativeReturns()) {
            section.set("negativeReturns", config.getNegativeReturns());
        }
    }
}

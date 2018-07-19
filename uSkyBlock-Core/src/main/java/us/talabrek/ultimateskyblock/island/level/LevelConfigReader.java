package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LevelConfigReader {
    private static final Pattern KEY_PATTERN = Pattern.compile("(?<id>[A-Z_0-9]+)([/:](?<sub>(\\*|[0-9]+|[0-9]+-[0-9]+)))?");

    private BlockLevelConfigBuilder defaultBuilder;
    private final Map<String, BlockLevelConfigBuilder> map = new HashMap<>();

    public LevelConfigReader() {
    }

    public BlockLevelConfigMap readLevelConfig(FileConfiguration config) {
        double defaultScore = config.getDouble("general.default", 10d);
        int defaultLimit = config.getInt("general.limit", Integer.MAX_VALUE);
        int defaultDR = config.getInt("general.defaultScale", Integer.MAX_VALUE);
        defaultBuilder = new BlockLevelConfigBuilder()
                .scorePerBlock(defaultScore)
                .limit(defaultLimit);
        addDefaults();
        readBlockValues(config.getConfigurationSection("blockValues"));
        readBlockLimits(config.getConfigurationSection("blockLimits"), defaultLimit);
        readBlockDiminishingReturns(config.getConfigurationSection("diminishingReturns"), defaultDR);
        readNegativeReturns(config.getConfigurationSection("negativeReturns"));
        return new BlockLevelConfigMap(map.values().stream().distinct().map(BlockLevelConfigBuilder::build).collect(Collectors.toList()), defaultBuilder);
    }

    private void addDefaults() {
        BlockLevelConfigBuilder nullScore = defaultBuilder.copy().base(new BlockMatch(Material.AIR)).scorePerBlock(0).limit(0);
        map.put(createKey(Material.AIR), nullScore);
    }

    private void readBlockValues(ConfigurationSection blockValues) {
        for (String key : blockValues.getKeys(false)) {
            BlockMatch blockMatch = getBlockMatch(key);
            double value = blockValues.getDouble(key);
            BlockLevelConfigBuilder builder = null;
            if (value == -1) {
                // share with "base-block" (i.e. the one with data-value 0)
                String baseKey = createKey(blockMatch.getType());
                if (map.containsKey(baseKey)) {
                    builder = map.get(baseKey).additionalBlocks(blockMatch);
                } else {
                    builder = defaultBuilder.copy().additionalBlocks(blockMatch);
                }
            } else if (value >= 0) {
                // Normal level config
                String baseKey = createKey(blockMatch);
                if (map.containsKey(baseKey)) {
                    builder = map.get(baseKey);
                } else {
                    builder = defaultBuilder.copy();
                }
                builder.base(blockMatch).scorePerBlock(value);
            } else if (value < -1) {
                // Direct adressing (the negative value is the material-id to map to).
                Material baseType = Material.getMaterial((int) -Math.round(value));
                String baseKey = createKey(baseType);
                if (map.containsKey(baseKey)) {
                    map.get(baseKey).additionalBlocks(blockMatch);
                } else {
                    builder = map.put(baseKey, defaultBuilder.copy().base(new BlockMatch(baseType, (byte) 0)).additionalBlocks(blockMatch));
                }
            }
            if (builder != null) {
                final BlockLevelConfigBuilder finalBuilder = builder;
                createKeys(blockMatch).forEach(k -> map.put(k, finalBuilder));
            }
        }
    }

    private Stream<String> createKeys(BlockMatch blockMatch) {
        List<String> keyList = new ArrayList<>();
        if (blockMatch.getDataValues().isEmpty()) {
            keyList.add(createKey(blockMatch));
        } else {
            blockMatch.getDataValues().stream().forEach(data -> keyList.add(createKey(blockMatch.getType(), data)));
        }
        return keyList.stream();
    }

    private void readBlockLimits(ConfigurationSection blockLimits, int defaultLimit) {
        if (blockLimits == null) {
            return;
        }
        for (String blockKey : blockLimits.getKeys(false)) {
            BlockMatch blockMatch = getBlockMatch(blockKey);
            int value = blockLimits.getInt(blockKey, defaultLimit);
            createKeys(blockMatch).forEach(k -> {
                if (map.containsKey(k)) {
                    map.get(k).limit(value);
                } else {
                    map.put(k, defaultBuilder.copy().base(blockMatch).limit(value));
                }
            });
        }
    }

    private void readBlockDiminishingReturns(ConfigurationSection diminishingReturns, int defaultLimit) {
        if (diminishingReturns == null) {
            return;
        }
        for (String blockKey : diminishingReturns.getKeys(false)) {
            BlockMatch blockMatch = getBlockMatch(blockKey);
            int value = diminishingReturns.getInt(blockKey, defaultLimit);
            createKeys(blockMatch).forEach(k -> {
                if (map.containsKey(k)) {
                    map.get(k).diminishingReturns(value);
                } else {
                    map.put(k, defaultBuilder.copy().base(blockMatch).diminishingReturns(value));
                }
            });
        }
    }

    private void readNegativeReturns(ConfigurationSection negativeReturns) {
        if (negativeReturns == null) {
            return;
        }
        for (String blockKey : negativeReturns.getKeys(false)) {
            BlockMatch blockMatch = getBlockMatch(blockKey);
            int value = negativeReturns.getInt(blockKey, -1);
            createKeys(blockMatch).forEach(k -> {
                if (map.containsKey(k)) {
                    map.get(k).negativeReturns(value);
                } else {
                    map.put(k, defaultBuilder.copy().base(blockMatch).negativeReturns(value));
                }
            });
        }
    }

    private String createKey(BlockMatch block) {
        return createKey(block.getType(), block.getDataValues().isEmpty() ? (byte) 0 : block.getDataValues().get(0));
    }

    private String createKey(Material material) {
        return createKey(material, (byte) 0);
    }

    private String createKey(Material material, byte dataValue) {
        return material.name() + ":" + (dataValue & 0xff);
    }

    private BlockMatch getBlockMatch(String blockKey) {
        Matcher m = KEY_PATTERN.matcher(blockKey);
        if (m.matches()) {
            Material material = null;
            String materialName = m.group("id");
            if (materialName.matches("[0-9]+")) {
                material = Material.getMaterial(Integer.parseInt(materialName, 10));
            } else {
                material = Material.getMaterial(materialName);
            }
            if (material == null) {
                LogUtil.log(Level.WARNING, "Invalid key '" + blockKey + "' in levelConfig, could not lookup Material");
                return null;
            }
            byte[] dataValues = getDataValues(m.group("sub"));
            return new BlockMatch(material, dataValues);
        } else {
            LogUtil.log(Level.WARNING, "Invalid key '" + blockKey + "' in levelConfig");
        }
        return null;
    }

    private byte[] getDataValues(String sub) {
        if (sub == null) {
            return new byte[0];
        }
        if (sub.equalsIgnoreCase("*") || sub.equalsIgnoreCase("0-15")) {
            byte[] data = new byte[16];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) ((i) & 0xff);
            }
            return data;
        } else if (!sub.isEmpty()) {
            String[] split = sub.split("-");
            if (split.length == 1) {
                return new byte[]{(byte) (Integer.parseInt(split[0]) & 0xff)};
            } else {
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                byte[] data = new byte[max - min + 1];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) ((min + i) & 0xff);
                }
                return data;
            }
        }
        return new byte[0];
    }
}

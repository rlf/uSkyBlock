package us.talabrek.ultimateskyblock.imports;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ItemComponentConverter {

    private final Logger logger;

    public ItemComponentConverter(Logger logger) {
        this.logger = logger;
    }

    public void checkAndDoImport(File directory) {
        var configFile = new File(directory, "config.yml");
        if (configFile.exists() && YamlConfiguration.loadConfiguration(configFile).getInt("version") <= 108) {
            importFile(configFile);
        }
        var challengesFile = new File(directory, "challenges.yml");
        if (challengesFile.exists() && YamlConfiguration.loadConfiguration(challengesFile).getInt("version") <= 106) {
            importFile(challengesFile);
        }
    }

    public void importFile(File file) {
        Path configFile = file.toPath();
        try {
            Files.copy(configFile, configFile.getParent().resolve(configFile.getFileName() + ".old"));

            FileConfiguration config = new YamlConfiguration();
            config.load(file);

            if (file.getName().equals("challenges.yml")) {
                convertChallenges(config);
            } else if (file.getName().equals("config.yml")) {
                convertConfig(config);
            }

            config.save(file);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while attempting to convert file " + file, e);
        }
    }

    private void convertConfig(FileConfiguration config) {
        var oldVersion = config.getInt("version");
        if (oldVersion > 108) {
            logger.warning("Expecting config.yml version 108, but found " + oldVersion + " instead. Skipping conversion.");
            return;
        }

        logger.info("Converting config.yml to new item component format for Minecraft 1.20.5 and later.");

        for (var path : config.getKeys(true)) {
            if (path.endsWith("chestItems")
                || (path.contains(".extraPermissions.") && config.isList(path))
                || path.endsWith("extraItems")
            ) {
                var oldSpecifications = config.getStringList(path);
                var results = oldSpecifications.stream().map(spec -> convertItemReward(spec, path)).toList();
                var newSpecifications = results.stream().map(pair -> pair.item).toList();
                var newComments = results.stream().map(pair -> pair.comment).filter(Objects::nonNull).toList();
                if (!newSpecifications.isEmpty()) {
                    config.set(path, newSpecifications);
                }
                if (!newComments.isEmpty()) {
                    List<String> comments = new ArrayList<>(config.getComments(path));
                    comments.addAll(newComments);
                    config.setComments(path, comments);
                }
            } else if (path.endsWith("displayItem") || path.endsWith("tool")) {
                var oldSpecification = config.getString(path);
                var pair = convertDisplayItem(oldSpecification, path);
                config.set(path, pair.item);
                if (pair.comment != null) {
                    List<String> comments = new ArrayList<>(config.getComments(path));
                    comments.add(pair.comment);
                    config.setComments(path, comments);
                }
            }
        }

        // fix old grass block in default config. We don't fix all of them, as GRASS has referred to both grass_block
        // and short_grass in the past, and we cannot determine what the user meant. They should fix it manually.
        List<String> giantBonus = config.getStringList("options.island.extraPermissions.giantbonus");
        if (giantBonus.contains("grass:1")) {
            var index = giantBonus.indexOf("grass:1");
            giantBonus.set(index, "grass_block:1");
            config.set("options.island.extraPermissions.giantbonus", giantBonus);
        }

        config.set("version", 109);
    }

    private void convertChallenges(FileConfiguration config) throws Exception {
        var oldVersion = config.getInt("version");
        if (oldVersion > 106) {
            logger.warning("Expecting challanges.yml version 106, but found " + oldVersion + " instead. Skipping conversion.");
            return;
        }
        logger.info("Converting challenges.yml to new item component format for Minecraft 1.20.5 and later.");

        convertChallengeItems(config);
        updateHeaderAndVersion(config);
    }


    private void convertChallengeItems(FileConfiguration config) {
        for (var path : config.getKeys(true)) {
            if (path.endsWith("displayItem") || path.endsWith("lockedDisplayItem")) {
                var oldSpecification = config.getString(path);
                var pair = convertDisplayItem(oldSpecification, path);
                config.set(path, pair.item);
                if (pair.comment != null) {
                    List<String> comments = new ArrayList<>(config.getComments(path));
                    comments.add(pair.comment);
                    config.setComments(path, comments);
                }
            } else if (path.endsWith(".items")) {
                var oldSpecifications = config.getStringList(path);
                var results = oldSpecifications.stream().map(spec -> convertItemReward(spec, path)).toList();
                var newSpecifications = results.stream().map(pair -> pair.item).toList();
                var newComments = results.stream().map(pair -> pair.comment).filter(Objects::nonNull).toList();
                config.set(path, newSpecifications);
                if (!newComments.isEmpty()) {
                    List<String> comments = new ArrayList<>(config.getComments(path));
                    comments.addAll(newComments);
                    config.setComments(path, comments);
                }
            } else if (path.endsWith("requiredItems")) {
                var oldSpecifications = config.getStringList(path);
                var results = oldSpecifications.stream().map(spec -> convertItemRequirement(spec, path)).toList();
                var newSpecifications = results.stream().map(pair -> pair.item).toList();
                var newComments = results.stream().map(pair -> pair.comment).filter(Objects::nonNull).toList();
                config.set(path, newSpecifications);
                if (!newComments.isEmpty()) {
                    List<String> comments = new ArrayList<>(config.getComments(path));
                    comments.addAll(newComments);
                    config.setComments(path, comments);
                }
            }
        }
    }

    private void updateHeaderAndVersion(FileConfiguration config) throws Exception {
        config.set("version", 107);
        // Add a comment to the version number with the latest changes. This replaces the old explanation,
        // which is moved to the header where it is more convenient and stable.
        config.setComments("version", Arrays.asList(
            null,
            "This file has been updated to version 107. Please check the changes made in this version.",
            "Changes in this version:",
            " - Items are now specified in the new component format.",
            " - Refer to the config header for the new format.",
            " - NBT tags are not automatically converted. They have been moved to the comments, please check them manually.",
            "   You can use the following converter to convert the old item specifications to the new format:",
            "   https://docs.papermc.io/misc/tools/item-command-converter",
            "DO NOT CHANGE THE VERSION! You will break the conversion and unexpected things will happen!"
        ));

        // Add the new header with explanations and usage instructions
        var defaultConfig = new YamlConfiguration();
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream("challenges.yml")), StandardCharsets.UTF_8))) {
            defaultConfig.load(reader);
        }
        config.options().setHeader(defaultConfig.options().getHeader());
    }

    private static final Pattern DISPLAY_PATTERN = Pattern.compile("(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?\\s*(?<meta>\\{.*})?");
    private static final Pattern REWARD_PATTERN = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)})?(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?:(?<amount>[0-9]+)\\s*(?<meta>\\{.*})?");
    public static final Pattern REQUIREMENT_PATTERN = Pattern.compile("(?<itemstack>(?<type>[0-9A-Z_]+)(:(?<subtype>[0-9]+))?(?<meta>\\{.*})?):(?<amount>[0-9]+)(;(?<op>[-+*^])(?<inc>[0-9]+))?");

    private SpecificationCommentPair convertDisplayItem(String oldSpecification, String path) {
        var matcher = DISPLAY_PATTERN.matcher(oldSpecification);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid display item specification at path " + path);
        }
        var itemType = fixMaterial(matcher.group("id"));
        var type = Material.matchMaterial(itemType);
        if (type != null) {
            itemType = type.getKey().getKey();
        } else {
            itemType = itemType.toLowerCase(Locale.ROOT);
            logger.warning("Unknown material " + itemType + " in display item specification at " + path);
        }
        var sub = matcher.group("sub");
        if (sub != null) {
            logger.warning("Old pre-1.13 item specification at " + path + " uses subtypes, which are no longer supported.");
        }
        var meta = matcher.group("meta");
        if (meta != null && meta.trim().isEmpty()) {
            meta = null;
        }
        String comment = null;
        if (meta != null) {
            logger.warning("Some items contain NBT tags, which are not automatically converted. Please check the entry at " + path + " manually.");
            comment = itemType + meta;
        }

        var newSpecification = new StringBuilder();
        newSpecification.append(itemType);
        if (meta != null) {
            newSpecification.append("[]");
        }

        return new SpecificationCommentPair(newSpecification.toString(), comment);
    }

    private SpecificationCommentPair convertItemReward(String oldSpecification, String path) {
        var matcher = REWARD_PATTERN.matcher(oldSpecification);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid reward item specification at path " + path);
        }
        var probability = matcher.group("prob");

        var itemType = fixMaterial(matcher.group("id"));
        var type = Material.matchMaterial(itemType);
        if (type != null) {
            itemType = type.getKey().getKey();
        } else {
            itemType = itemType.toLowerCase(Locale.ROOT);
            logger.warning("Unknown material " + itemType + " in reward item specification at " + path);
        }
        var sub = matcher.group("sub");
        if (sub != null) {
            logger.warning("Old pre-1.13 item specification at " + path + " uses subtypes, which are no longer supported.");
        }
        var amount = matcher.group("amount");
        var meta = matcher.group("meta");
        if (meta != null && meta.trim().isEmpty()) {
            meta = null;
        }
        String comment = null;
        String convertedMeta = convertDefaultItemMeta(meta);

        if (meta != null && convertedMeta == null) {
            logger.warning("Some items contain NBT tags, which are not automatically converted. Please check the entry at " + path + " manually.");
            comment = itemType + meta;
        }

        var newSpecification = new StringBuilder();
        if (probability != null) {
            newSpecification.append("{p=").append(probability).append("}");
        }
        newSpecification.append(itemType);
        if (convertedMeta != null) {
            newSpecification.append(convertedMeta);
        } else if (meta != null) {
            newSpecification.append("[]");
        }

        newSpecification.append(":").append(amount);

        return new SpecificationCommentPair(newSpecification.toString(), comment);
    }

    private SpecificationCommentPair convertItemRequirement(String oldSpecification, String path) {
        var matcher = REQUIREMENT_PATTERN.matcher(oldSpecification);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid requirement item specification at path " + path + ": " + oldSpecification);
        }

        var itemType = fixMaterial(matcher.group("type"));
        var type = Material.matchMaterial(itemType);
        if (type != null) {
            itemType = type.getKey().getKey();
        } else {
            itemType = itemType.toLowerCase(Locale.ROOT);
            logger.warning("Unknown material " + itemType + " in reward item specification at " + path);
        }
        var sub = matcher.group("subtype");
        if (sub != null) {
            logger.warning("Old pre-1.13 item specification at " + path + " uses subtypes, which are no longer supported.");
        }
        var amount = matcher.group("amount");
        var meta = matcher.group("meta");
        if (meta != null && meta.trim().isEmpty()) {
            meta = null;
        }
        String comment = null;
        if (meta != null) {
            logger.warning("Some items contain NBT tags, which are not automatically converted. Please check the entry at " + path + " manually.");
            comment = itemType + meta;
        }

        var op = matcher.group("op");
        var inc = matcher.group("inc");

        var newSpecification = new StringBuilder();
        newSpecification.append(itemType);
        if (meta != null) {
            newSpecification.append("[]");
        }
        newSpecification.append(":").append(amount);
        if (op != null) {
            newSpecification.append(";").append(op).append(inc);
        }

        return new SpecificationCommentPair(newSpecification.toString(), comment);
    }

    private static final Map<String, String> DEFAULT_ITEM_META_CONVERSION = Map.of(
        "{Enchantments:[{id:\"minecraft:infinity\",lvl:1},{id:\"minecraft:unbreaking\",lvl:3}]}", "[enchantments={levels:{infinity:1,unbreaking:3}}]",
        "{Enchantments:[{id:\"minecraft:infinity\",lvl:1},{id:\"minecraft:unbreaking\",lvl:3},{id:\"minecraft:power\",lvl:5}]}", "[enchantments={levels:{infinity:1,power:5,unbreaking:3}}]",
        "{Enchantments:[{id:\"minecraft:sharpness\",lvl:5},{id:\"minecraft:unbreaking\",lvl:3},{id:\"minecraft:fire\",lvl:1}]}", "[enchantments={levels:{fire_aspect:1,sharpness:5,unbreaking:3}}]",
        "{Enchantments:[{id:\"minecraft:feather_falling\",lvl:4},{id:\"minecraft:protection\",lvl:4},{id:\"minecraft:blast_protection\",lvl:4}]}", "[enchantments={levels:{blast_protection:4,feather_falling:4,protection:4}}]"
    );

    private String convertDefaultItemMeta(String oldMeta) {
        if (oldMeta == null) {
            return null;
        }
        return DEFAULT_ITEM_META_CONVERSION.get(oldMeta);
    }

    private static final Map<String, String> MATERIAL_UPDATES = Map.of(
        "SIGN", "OAK_SIGN",
        "SAPLING", "OAK_SAPLING"
    );

    private String fixMaterial(String material) {
        return MATERIAL_UPDATES.getOrDefault(material, material);
    }

    private record SpecificationCommentPair(@NotNull String item, @Nullable String comment) {
    }
}

package us.talabrek.ultimateskyblock.imports.challenges;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.imports.USBImporter;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dk.lockfuglsang.minecraft.file.FileUtil.readConfig;

public class ConfigPre113Importer implements USBImporter {
    private static final Pattern REQ_PATTERN = Pattern.compile("(?<itemstack>(?<type>[0-9A-Z_]+)(:(?<subtype>[0-9]+))?)(?<meta>\\{.*\\})?:(?<amount>[0-9]+)(;(?<op>[+\\-*\\^])(?<inc>[0-9]+))?");
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?(?<itemstack>(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?):(?<amount>[0-9]+)\\s*(?<meta>\\{.*\\})?");
    private static final Pattern ITEM_PATTERN = Pattern.compile("(?<itemstack>(?<id>[0-9A-Z_]+)(:(?<sub>[0-9]+))?)\\s*(?<meta>\\{.*\\})?");

    private uSkyBlock plugin;

    @Override
    public String getName() {
        return "config-1-13";
    }

    @Override
    public void init(uSkyBlock plugin) {
        this.plugin = plugin;
        plugin.setMaintenanceMode(true);
    }

    @Override
    public Boolean importFile(File file) {
        FileConfiguration config = new YmlConfiguration();
        readConfig(config, file);
        try {
            config.save(new File(file.getParentFile(), file.getName() + ".org"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.getName().equals("challenges.yml")) {
            convertChallenges(config);
        } else if (file.getName().equals("config.yml")) {
            config = plugin.getConfig();
            convertConfig(config);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void convertConfig(FileConfiguration config) {
        Set<String> keys = config.getKeys(true);
        // displayItems (and lockedDisplayItem)
        for (String key : keys.stream().filter(f -> f.toLowerCase().endsWith("displayitem")).collect(Collectors.toList())) {
            try {
                config.set(key, convertItem(config.getString(key)));
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Unable to convert config.yml:" + key + ":" + e.getMessage());
            }
        }
        // chestItems
        for (String key : keys.stream().filter(f -> f.endsWith(".chestItems") || f.endsWith(".extraItems")).collect(Collectors.toList())) {
            try {
                List<String> value = new ArrayList<>();
                value.addAll(convertItemAmountList(config.getStringList(key)));
                config.set(key, value);
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Unable to convert config.yml:" + key + ":" + e.getMessage());
            }
        }
        // extraPermissions
        ConfigurationSection extraPerms = config.getConfigurationSection("options.island.extraPermissions");
        if (extraPerms != null) {
            for (String key : extraPerms.getKeys(false)) {
                try {
                    extraPerms.set(key, convertItemAmountList(extraPerms.getStringList(key)));
                } catch (Exception e) {
                    LogUtil.log(Level.WARNING, "Unable to convert config.yml:" + key + ":" + e.getMessage());
                }
            }
        }
    }

    private void convertChallenges(FileConfiguration config) {
        // Convert all requiredItems
        Set<String> keys = config.getKeys(true);
        for (String key : keys.stream().filter(f -> f.endsWith(".requiredItems")).collect(Collectors.toList())) {
            try {
                List<String> value = new ArrayList<>();
                value.addAll(convertRequiredItemsList(config.getStringList(key)));
                config.set(key, value);
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Unable to convert challenges.yml:" + key + ":" + e.getMessage());
            }
        }
        // Rewards (.items)
        for (String key : keys.stream().filter(f -> f.endsWith(".items")).collect(Collectors.toList())) {
            try {
                List<String> value = new ArrayList<>();
                value.addAll(convertItemAmountList(config.getStringList(key)));
                config.set(key, value);
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Unable to convert challenges.yml:" + key + ":" + e.getMessage());
            }
        }
        // displayItems (and lockedDisplayItem)
        for (String key : keys.stream().filter(f -> f.toLowerCase().endsWith("displayitem")).collect(Collectors.toList())) {
            try {
                config.set(key, convertItem(config.getString(key)));
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Unable to convert challenges.yml:" + key + ":" + e.getMessage());
            }
        }
    }

    private Collection<? extends String> convertItemAmountList(List<String> stringList) {
        List<String> values = new ArrayList<>();
        for (String value : stringList) {
            values.add(convertItemAmount(value));
        }
        return values.stream().filter(f -> f != null).collect(Collectors.toList());
    }

    private Collection<? extends String> convertRequiredItemsList(List<String> stringList) {
        List<String> values = new ArrayList<>();
        for (String value : stringList) {
            values.add(convertRequiredItems(value));
        }
        return values.stream().filter(f -> f != null).collect(Collectors.toList());
    }

    private String convertRequiredItems(String value) {
        Matcher m = REQ_PATTERN.matcher(value);
        if (m.matches()) {
            String itemstack = m.group("itemstack");
            ItemStack itemStack = ItemStackUtil.createItemStack(itemstack);
            String newItemStack = itemStack.getType().name() + (itemStack.getDurability() > 0 ? ":" + itemStack.getDurability() : "");
            return replaceItemStack(m, value, newItemStack);
        }
        return null;
    }

    private String convertItemAmount(String reward) {
        Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unknown item: '" + reward + "'");
        }
        String itemstack = m.group("itemstack");
        ItemStack itemStack = ItemStackUtil.createItemStack(itemstack);
        String newItemStack = itemStack.getType().name() + (itemStack.getDurability() > 0 ? ":" + itemStack.getDurability() : "");
        return replaceItemStack(m, reward, newItemStack);
    }

    private String convertItem(String displayItem) {
        Matcher m = ITEM_PATTERN.matcher(displayItem);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unknown displayItem: '" + displayItem + "'");
        }
        String itemstack = m.group("itemstack");
        ItemStack itemStack = ItemStackUtil.createItemStack(itemstack);
        String newItemStack = itemStack.getType().name() + (itemStack.getDurability() > 0 ? ":" + itemStack.getDurability() : "");
        return replaceItemStack(m, displayItem, newItemStack);
    }

    private String replaceItemStack(Matcher m, String original, String replacement) {
        return original.substring(0, m.start("itemstack")) + replacement + original.substring(m.end("itemstack"));
    }

    @Override
    public File[] getFiles() {
        return new File[]{
                new File(plugin.getDataFolder(), "challenges.yml"),
                new File(plugin.getDataFolder(), "config.yml"),
        };
    }

    @Override
    public void completed(int success, int failed, int skipped) {
        plugin.setMaintenanceMode(false);
    }
}

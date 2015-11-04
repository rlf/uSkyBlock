package us.talabrek.ultimateskyblock;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public class Settings {
    private static final Logger log = Logger.getLogger(Settings.class.getName());
    public static int general_maxPartySize;
    public static String general_worldName;
    public static int island_distance;
    public static int island_height;
    public static int general_spawnSize;
    public static boolean island_removeCreaturesByTeleport;
    public static int island_protectionRange;
    public static int island_radius;
    public static ItemStack[] island_chestItems;
    public static boolean island_addExtraItems;
    public static String[] island_extraPermissions;
    public static boolean island_useOldIslands;
    public static boolean island_allowIslandLock;
    public static boolean island_useIslandLevel;
    public static boolean island_useTopTen;
    public static int general_cooldownInfo;
    public static int general_cooldownRestart;
    public static int general_biomeChange;
    public static boolean extras_sendToSpawn;
    public static boolean extras_obsidianToLava;
    public static String island_schematicName;
    public static long island_topTenTimeout;
    public static boolean island_allowPvP;
    public static Locale locale = Locale.getDefault();
    public static boolean nether_enabled;

    public static boolean loadPluginConfig(FileConfiguration config) {
        boolean changed = false;
        try {
            general_maxPartySize = config.getInt("options.general.maxPartySize");
            if (general_maxPartySize < 0) {
                general_maxPartySize = 0;
            }
        } catch (Exception e) {
            general_maxPartySize = 4;
        }
        try {
            island_distance = config.getInt("options.island.distance");
            if (island_distance < 50) {
                island_distance = 50;
            }
        } catch (Exception e) {
            island_distance = 110;
        }
        try {
            island_protectionRange = config.getInt("options.island.protectionRange");
            if (island_protectionRange > island_distance) {
                island_protectionRange = island_distance;
            }
        } catch (Exception e) {
            island_protectionRange = 100;
        }
        island_radius = island_protectionRange / 2;
        try {
            general_cooldownInfo = config.getInt("options.general.cooldownInfo");
            if (general_cooldownInfo < 0) {
                general_cooldownInfo = 0;
            }
        } catch (Exception e) {
            general_cooldownInfo = 60;
        }
        try {
            general_biomeChange = config.getInt("options.general.biomeChange");
            if (general_biomeChange < 0) {
                general_biomeChange = 0;
            }
        } catch (Exception e) {
            general_biomeChange = 3600;
        }
        try {
            general_cooldownRestart = config.getInt("options.general.cooldownRestart");
            if (general_cooldownRestart < 0) {
                general_cooldownRestart = 0;
            }
        } catch (Exception e) {
            general_cooldownRestart = 60;
        }
        try {
            island_height = config.getInt("options.island.height");
            if (island_height < 20) {
                island_height = 20;
            }
        } catch (Exception e) {
            island_height = 120;
        }
        if (!config.contains("options.extras.obsidianToLava")) {
            config.set("options.extras.obsidianToLava", true);
            changed = true;
        }
        if (!config.contains("options.general.spawnSize")) {
            config.set("options.general.spawnSize", 50);
            changed = true;
        }
        try {
            general_spawnSize = config.getInt("options.general.spawnSize");
            if (general_spawnSize < 50) {
                general_spawnSize = 50;
            }
        } catch (Exception e) {
            general_spawnSize = 50;
        }
        // TODO: 06/12/2014 - R4zorax: Null pointer protection
        String chestItemString = config.getString("options.island.chestItems", "");
        island_chestItems = ItemStackUtil.createItemArray(chestItemString);

        island_schematicName = config.getString("options.island.schematicName");
        final Set<String> permissionList = new HashSet<>();
        if (config.isConfigurationSection("options.island.extraPermissions")) {
            permissionList.addAll(config.getConfigurationSection("options.island.extraPermissions").getKeys(true));
        }
        island_addExtraItems = config.getBoolean("options.island.addExtraItems");
        extras_obsidianToLava = config.getBoolean("options.extras.obsidianToLava");
        island_useIslandLevel = config.getBoolean("options.island.useIslandLevel");
        island_extraPermissions = permissionList.toArray(new String[0]);
        extras_sendToSpawn = config.getBoolean("options.extras.sendToSpawn");
        island_useTopTen = config.getBoolean("options.island.useTopTen");
        general_worldName = config.getString("options.general.worldName", "skyworld");
        island_removeCreaturesByTeleport = config.getBoolean("options.island.removeCreaturesByTeleport");
        island_allowIslandLock = config.getBoolean("options.island.allowIslandLock");
        island_useOldIslands = config.getBoolean("options.island.useOldIslands");
        island_topTenTimeout = config.getInt("options.island.topTenTimeout", 7); // Every 7 minutes
        island_allowPvP = config.getString("options.island.allowPvP", "deny").equalsIgnoreCase("allow") ||
            config.getString("options.island.allowPvP", "false").equalsIgnoreCase("true");
        Locale loc = I18nUtil.getLocale(config.getString("language", null));
        if (loc != null) {
            locale = loc;
            I18nUtil.setLocale(locale);
        }
        nether_enabled = config.getBoolean("nether.enabled", false);
        if (nether_enabled && (island_distance % 32 != 0 || island_protectionRange % 32 != 0)) {
            log.warning("Nether DISABLED, since island distance and protectionRange is not divisible by 32!");
            nether_enabled = false;
            changed = true;
        }
        nether_enabled = config.getBoolean("nether.enabled", false);
        if (nether_enabled && (island_distance % 32 != 0 || island_protectionRange % 32 != 0)) {
            log.warning("Nether DISABLED, since island distance and protectionRange is not divisible by 32!");
            nether_enabled = false;
            changed = true;
        }
        return changed;
    }

}

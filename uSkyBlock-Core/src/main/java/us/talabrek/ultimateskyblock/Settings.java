package us.talabrek.ultimateskyblock;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

public class Settings {
    private static final Logger log = Logger.getLogger(Settings.class.getName());
    public static int general_maxPartySize;
    public static String general_worldName;
    public static int island_plotRadius;
    public static int island_height;
    public static int general_spawnSize;
    public static boolean island_removeCreaturesByTeleport;
    public static int island_protectionRadius;
    public static int island_protection_radius;
    public static ItemStack[] island_chestItems;
    public static boolean island_addExtraItems;
    public static String[] island_extraPermissions;
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
    public static int nether_lava_level;
    public static int nether_height;
    public static boolean extra_nether_ceiling;

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
        	// Catch and convert old naming convention.
            if (!config.contains("options.island.islandRadius")) {
                if (config.contains("options.island.distance")) {
                	int size = config.getInt("options.island.distance");
                    config.set("options.island.islandRadius", size/2);
                } else {
                	config.set("options.island.islandRadius", 64);
                }
                changed = true;
            }
            island_plotRadius = config.getInt("options.island.islandRadius");
            if (island_plotRadius < 64) {
                island_plotRadius = 64;
            }
        } catch (Exception e) {
        	//Protection range cannot be higher than 'radius'
            island_plotRadius = 64; 
        }
        try {
        	// Catch and convert old naming convention. 
            if (!config.contains("options.island.protectionRadius")) {
                if (config.contains("options.island.protectionRange")) {
                	int size = config.getInt("options.island.protectionRange");
                    config.set("options.island.protectionRadius", size/2);
                } else {
                	config.set("options.island.protectionRadius", 64);
                }
                changed = true;
            }
            island_protectionRadius = config.getInt("options.island.protectionRadius");
            if (island_protectionRadius > island_plotRadius) {
                island_protectionRadius = island_plotRadius;
            }
        } catch (Exception e) {
        	//Protection range cannot be higher than 'radius'
            island_protectionRadius = 64;
        }
        island_protection_radius = island_protectionRadius;
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
     // Catch and convert old naming convention.
        if (!config.contains("options.general.spawnRadius")) {
            if (config.contains("options.general.spawnSize")) {
            	int size = config.getInt("options.general.spawnSize", 64);
                config.set("options.general.spawnRadius", size);
            } else {
            	config.set("options.general.spawnRadius", 64);
            }
            changed = true;
        }
        general_spawnSize = config.getInt("options.general.spawnRadius", 64);
        island_chestItems = ItemStackUtil.createItemArray(ItemStackUtil.createItemList(
                config.getStringList("options.island.chestItems")
        ));

        island_schematicName = config.getString("options.island.schematicName");
        if (island_schematicName == null || "yourschematicname".equals(island_schematicName) || "uSkyBlockDefault".equals(island_schematicName)) {
            island_schematicName = "default";
            config.set("options.island.schematicName", island_schematicName);
            changed = true;
        }
        final Set<String> permissionList = new HashSet<>();
        if (config.isConfigurationSection("options.island.extraPermissions")) {
            permissionList.addAll(config.getConfigurationSection("options.island.extraPermissions").getKeys(false));
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
        island_topTenTimeout = config.getInt("options.island.topTenTimeout", 7); // Every 7 minutes
        island_allowPvP = config.getString("options.island.allowPvP", "deny").equalsIgnoreCase("allow") ||
            config.getString("options.island.allowPvP", "false").equalsIgnoreCase("true");
        Locale loc = I18nUtil.getLocale(config.getString("language", null));
        if (loc != null) {
            locale = loc;
            I18nUtil.setLocale(locale);
        }
        nether_enabled = config.getBoolean("nether.enabled", false);
        if (nether_enabled && !WorldEditHandler.isOuterPossible()) {
            log.warning("Nether DISABLED, since islands cannot be chunk-aligned!");
            nether_enabled = false;
            changed = true;
        }
        nether_lava_level = config.getInt("nether.lava_level", config.getInt("nether.lava-level", 32));
        nether_height = config.getInt("nether.height", island_height/2);
        extra_nether_ceiling = config.getBoolean("nether.extra-nether-ceiling", true);
        if (!config.contains("nether.extra-nether-ceiling")){
            config.set("nether.extra-nether-ceiling", true);
            changed = true;
        }
        return changed;
    }

}

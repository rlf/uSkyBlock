package us.talabrek.ultimateskyblock;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;

import java.util.*;

import org.bukkit.*;

public class Settings {
    public static int general_maxPartySize;
    public static String general_worldName;
    public static int island_distance;
    public static int[] blockList;
    public static int[] limitList;
    public static int[] diminishingReturnsList;
    public static int island_height;
    public static int general_spawnSize;
    public static boolean island_removeCreaturesByTeleport;
    public static boolean island_protectWithWorldGuard;
    public static int island_protectionRange;
    public static String island_allowPvP;
    public static ItemStack[] island_chestItems;
    public static boolean island_addExtraItems;
    public static String[] island_extraPermissions;
    public static boolean island_useOldIslands;
    public static boolean island_allowIslandLock;
    public static boolean island_useIslandLevel;
    public static boolean island_useTopTen;
    public static int island_listTime;
    public static int general_cooldownInfo;
    public static int general_cooldownRestart;
    public static int general_biomeChange;
    public static boolean extras_sendToSpawn;
    public static boolean extras_obsidianToLava;
    public static String island_schematicName;
    public static boolean challenges_broadcastCompletion;
    public static String challenges_broadcastText;
    public static String[] challenges_ranks;
    public static boolean challenges_requirePreviousRank;
    public static int challenges_rankLeeway;
    public static String challenges_challengeColor;
    public static String challenges_finishedColor;
    public static String challenges_repeatableColor;
    public static boolean challenges_enableEconomyPlugin;
    public static boolean challenges_allowChallenges;
    public static Set<String> challenges_challengeList;
    public static Material[] itemList;

    static {
        Settings.blockList = new int[256];
        Settings.limitList = new int[256];
        Settings.diminishingReturnsList = new int[256];
        Settings.itemList = new Material[2000];
    }

    public static boolean loadPluginConfig(FileConfiguration config) {
        boolean changed = false;
        try {
            Settings.general_maxPartySize = config.getInt("options.general.maxPartySize");
            if (Settings.general_maxPartySize < 0) {
                Settings.general_maxPartySize = 0;
            }
        } catch (Exception e) {
            Settings.general_maxPartySize = 4;
        }
        try {
            Settings.island_distance = config.getInt("options.island.distance");
            if (Settings.island_distance < 50) {
                Settings.island_distance = 50;
            }
        } catch (Exception e) {
            Settings.island_distance = 110;
        }
        try {
            Settings.island_protectionRange = config.getInt("options.island.protectionRange");
            if (Settings.island_protectionRange > Settings.island_distance) {
                Settings.island_protectionRange = Settings.island_distance;
            }
        } catch (Exception e) {
            Settings.island_protectionRange = 100;
        }
        try {
            Settings.general_cooldownInfo = config.getInt("options.general.cooldownInfo");
            if (Settings.general_cooldownInfo < 0) {
                Settings.general_cooldownInfo = 0;
            }
        } catch (Exception e) {
            Settings.general_cooldownInfo = 60;
        }
        try {
            Settings.general_biomeChange = config.getInt("options.general.biomeChange");
            if (Settings.general_biomeChange < 0) {
                Settings.general_biomeChange = 0;
            }
        } catch (Exception e) {
            Settings.general_biomeChange = 3600;
        }
        try {
            Settings.general_cooldownRestart = config.getInt("options.general.cooldownRestart");
            if (Settings.general_cooldownRestart < 0) {
                Settings.general_cooldownRestart = 0;
            }
        } catch (Exception e) {
            Settings.general_cooldownRestart = 60;
        }
        try {
            Settings.island_height = config.getInt("options.island.height");
            if (Settings.island_height < 20) {
                Settings.island_height = 20;
            }
        } catch (Exception e) {
            Settings.island_height = 120;
        }
        try {
            Settings.challenges_rankLeeway = config.getInt("options.challenges.rankLeeway");
            if (Settings.challenges_rankLeeway < 0) {
                Settings.challenges_rankLeeway = 0;
            }
        } catch (Exception e) {
            Settings.challenges_rankLeeway = 0;
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
            Settings.general_spawnSize = config.getInt("options.general.spawnSize");
            if (Settings.general_spawnSize < 50) {
                Settings.general_spawnSize = 50;
            }
        } catch (Exception e) {
            Settings.general_spawnSize = 50;
        }
        final String[] chestItemString = config.getString("options.island.chestItems").split(" ");
        final ItemStack[] tempChest = new ItemStack[chestItemString.length];
        String[] amountdata = new String[2];
        for (int i = 0; i < tempChest.length; ++i) {
            amountdata = chestItemString[i].split(":");
            tempChest[i] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
        }
        Settings.island_chestItems = tempChest;
        Settings.island_allowPvP = config.getString("options.island.allowPvP");
        Settings.island_schematicName = config.getString("options.island.schematicName");
        if (!Settings.island_allowPvP.equalsIgnoreCase("allow")) {
            Settings.island_allowPvP = "deny";
        }
        final Set<String> permissionList = config.getConfigurationSection("options.island.extraPermissions").getKeys(true);
        Settings.island_addExtraItems = config.getBoolean("options.island.addExtraItems");
        Settings.extras_obsidianToLava = config.getBoolean("options.extras.obsidianToLava");
        Settings.island_useIslandLevel = config.getBoolean("options.island.useIslandLevel");
        Settings.island_extraPermissions = permissionList.toArray(new String[0]);
        Settings.island_protectWithWorldGuard = config.getBoolean("options.island.protectWithWorldGuard");
        Settings.extras_sendToSpawn = config.getBoolean("options.extras.sendToSpawn");
        Settings.island_useTopTen = config.getBoolean("options.island.useTopTen");
        Settings.general_worldName = config.getString("options.general.worldName");
        Settings.island_removeCreaturesByTeleport = config.getBoolean("options.island.removeCreaturesByTeleport");
        Settings.island_allowIslandLock = config.getBoolean("options.island.allowIslandLock");
        Settings.island_useOldIslands = config.getBoolean("options.island.useOldIslands");
        Settings.challenges_challengeList = config.getConfigurationSection("options.challenges.challengeList").getKeys(false);
        Settings.challenges_broadcastCompletion = config.getBoolean("options.challenges.broadcastCompletion");
        Settings.challenges_broadcastText = config.getString("options.challenges.broadcastText");
        Settings.challenges_challengeColor = config.getString("options.challenges.challengeColor");
        Settings.challenges_enableEconomyPlugin = config.getBoolean("options.challenges.enableEconomyPlugin");
        Settings.challenges_finishedColor = config.getString("options.challenges.finishedColor");
        Settings.challenges_repeatableColor = config.getString("options.challenges.repeatableColor");
        Settings.challenges_requirePreviousRank = config.getBoolean("options.challenges.requirePreviousRank");
        Settings.challenges_allowChallenges = config.getBoolean("options.challenges.allowChallenges");
        Settings.challenges_ranks = config.getString("options.challenges.ranks").split(" ");

        // TODO: Fix wrongly formatting in text uSkyBlock.correctFormatting(String)
        return changed;
    }

}

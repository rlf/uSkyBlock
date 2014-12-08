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
        blockList = new int[256];
        limitList = new int[256];
        diminishingReturnsList = new int[256];
        itemList = new Material[2000];
    }

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
        try {
            challenges_rankLeeway = config.getInt("options.challenges.rankLeeway");
            if (challenges_rankLeeway < 0) {
                challenges_rankLeeway = 0;
            }
        } catch (Exception e) {
            challenges_rankLeeway = 0;
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
        final String[] chestItemString = config.getString("options.island.chestItems").split(" ");
        final ItemStack[] tempChest = new ItemStack[chestItemString.length];
        String[] amountdata = new String[2];
        for (int i = 0; i < tempChest.length; ++i) {
            amountdata = chestItemString[i].split(":");
            tempChest[i] = new ItemStack(Integer.parseInt(amountdata[0]), Integer.parseInt(amountdata[1]));
        }
        island_chestItems = tempChest;
        island_allowPvP = config.getString("options.island.allowPvP");
        island_schematicName = config.getString("options.island.schematicName");
        if (!island_allowPvP.equalsIgnoreCase("allow")) {
            island_allowPvP = "deny";
        }
        final Set<String> permissionList = config.getConfigurationSection("options.island.extraPermissions").getKeys(true);
        island_addExtraItems = config.getBoolean("options.island.addExtraItems");
        extras_obsidianToLava = config.getBoolean("options.extras.obsidianToLava");
        island_useIslandLevel = config.getBoolean("options.island.useIslandLevel");
        island_extraPermissions = permissionList.toArray(new String[0]);
        island_protectWithWorldGuard = config.getBoolean("options.island.protectWithWorldGuard");
        extras_sendToSpawn = config.getBoolean("options.extras.sendToSpawn");
        island_useTopTen = config.getBoolean("options.island.useTopTen");
        general_worldName = config.getString("options.general.worldName");
        island_removeCreaturesByTeleport = config.getBoolean("options.island.removeCreaturesByTeleport");
        island_allowIslandLock = config.getBoolean("options.island.allowIslandLock");
        island_useOldIslands = config.getBoolean("options.island.useOldIslands");
        challenges_challengeList = config.getConfigurationSection("options.challenges.challengeList").getKeys(false);
        challenges_broadcastCompletion = config.getBoolean("options.challenges.broadcastCompletion");
        challenges_broadcastText = config.getString("options.challenges.broadcastText");
        challenges_challengeColor = config.getString("options.challenges.challengeColor");
        challenges_enableEconomyPlugin = config.getBoolean("options.challenges.enableEconomyPlugin");
        challenges_finishedColor = config.getString("options.challenges.finishedColor");
        challenges_repeatableColor = config.getString("options.challenges.repeatableColor");
        challenges_requirePreviousRank = config.getBoolean("options.challenges.requirePreviousRank");
        challenges_allowChallenges = config.getBoolean("options.challenges.allowChallenges");
        challenges_ranks = config.getString("options.challenges.ranks").split(" ");
        String icons = config.getString("options.challenges.rank_icons");

        // TODO: Fix wrongly formatting in text uSkyBlock.correctFormatting(String)
        return changed;
    }
}

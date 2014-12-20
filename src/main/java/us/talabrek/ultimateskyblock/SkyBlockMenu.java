package us.talabrek.ultimateskyblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.talabrek.ultimateskyblock.challenge.ChallengeLogic;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Move all the texts to resource-files (translatable).
/**
 * The UI menu of uSkyBlock (using the inventory UI).
 */
public class SkyBlockMenu {
    private static final Pattern PERM_VALUE_PATTERN = Pattern.compile("(\\[(?<perm>(?<not>[!])?[^\\]]+)\\])?(?<value>.*)");
    private uSkyBlock skyBlock;
    private final ChallengeLogic challengeLogic;
    ItemStack pHead;
    ItemStack sign;
    ItemStack biome;
    ItemStack lock;
    ItemStack warpset;
    ItemStack warptoggle;
    ItemStack invite;
    ItemStack kick;

    public SkyBlockMenu(uSkyBlock skyBlock, ChallengeLogic challengeLogic) {
        this.skyBlock = skyBlock;
        this.challengeLogic = challengeLogic;
        pHead = new ItemStack(397, 1, (short) 3);
        sign = new ItemStack(323, 1);
        biome = new ItemStack(6, 1, (short) 3);
        lock = new ItemStack(101, 1);
        warpset = new ItemStack(90, 1);
        warptoggle = new ItemStack(69, 1);
        invite = new ItemStack(398, 1);
        kick = new ItemStack(301, 1);
    }

    public Inventory displayPartyPlayerGUI(final Player player, final String pname) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 9, pname + " <Permissions>");
        final ItemStack pHead = new ItemStack(397, 1, (short) 3);
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName("\u00a7hPlayer Permissions");
        lores.add("\u00a7eClick here to return to");
        lores.add("\u00a7eyour island group's info.");
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        meta3.setDisplayName(pname + "'s Permissions");
        lores.add("\u00a7eHover over an icon to view");
        lores.add("\u00a7ea permission. Change the");
        lores.add("\u00a7epermission by clicking it.");
        meta3.setLore(lores);
        pHead.setItemMeta(meta3);
        menu.addItem(new ItemStack[]{pHead});
        lores.clear();
        meta2 = biome.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canChangeBiome")) {
            meta2.setDisplayName("\u00a7aChange Biome");
            lores.add("\u00a7fThis player \u00a7acan\u00a7f change the");
            lores.add("\u00a7fisland's biome. Click here");
            lores.add("\u00a7fto remove this permission.");
        } else {
            meta2.setDisplayName("\u00a7cChange Biome");
            lores.add("\u00a7fThis player \u00a7ccannot\u00a7f change the");
            lores.add("\u00a7fisland's biome. Click here");
            lores.add("\u00a7fto grant this permission.");
        }
        meta2.setLore(lores);
        biome.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{biome});
        lores.clear();
        meta2 = lock.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canToggleLock")) {
            meta2.setDisplayName("\u00a7aToggle Island Lock");
            lores.add("\u00a7fThis player \u00a7acan\u00a7f toggle the");
            lores.add("\u00a7fisland's lock, which prevents");
            lores.add("\u00a7fnon-group members from entering.");
            lores.add("\u00a7fClick here to remove this permission.");
        } else {
            meta2.setDisplayName("\u00a7cToggle Island Lock");
            lores.add("\u00a7fThis player \u00a7ccannot\u00a7f toggle the");
            lores.add("\u00a7fisland's lock, which prevents");
            lores.add("\u00a7fnon-group members from entering.");
            lores.add("\u00a7fClick here to add this permission");
        }
        meta2.setLore(lores);
        lock.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{lock});
        lores.clear();
        meta2 = warpset.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canChangeWarp")) {
            meta2.setDisplayName("\u00a7aSet Island Warp");
            lores.add("\u00a7fThis player \u00a7acan\u00a7f set the");
            lores.add("\u00a7fisland's warp, which allows");
            lores.add("\u00a7fnon-group members to teleport");
            lores.add("\u00a7fto the island. Click here to");
            lores.add("\u00a7fremove this permission.");
        } else {
            meta2.setDisplayName("\u00a7cSet Island Warp");
            lores.add("\u00a7fThis player \u00a7ccannot\u00a7f set the");
            lores.add("\u00a7fisland's warp, which allows");
            lores.add("\u00a7fnon-group members to teleport");
            lores.add("\u00a7fto the island. Click here to");
            lores.add("\u00a7fadd this permission.");
        }
        meta2.setLore(lores);
        warpset.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{warpset});
        lores.clear();
        meta2 = warptoggle.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canToggleWarp")) {
            meta2.setDisplayName("\u00a7aToggle Island Warp");
            lores.add("\u00a7fThis player \u00a7acan\u00a7f toggle the");
            lores.add("\u00a7fisland's warp, allowing them");
            lores.add("\u00a7fto turn it on or off at anytime.");
            lores.add("\u00a7fbut not set the location. Click");
            lores.add("\u00a7fhere to remove this permission.");
        } else {
            meta2.setDisplayName("\u00a7cToggle Island Warp");
            lores.add("\u00a7fThis player \u00a7ccannot\u00a7f toggle the");
            lores.add("\u00a7fisland's warp, allowing them");
            lores.add("\u00a7fto turn it on or off at anytime,");
            lores.add("\u00a7fbut not set the location. Click");
            lores.add("\u00a7fhere to add this permission.");
        }
        meta2.setLore(lores);
        warptoggle.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{warptoggle});
        lores.clear();
        meta2 = invite.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canInviteOthers")) {
            meta2.setDisplayName("\u00a7aInvite Players");
            lores.add("\u00a7fThis player \u00a7acan\u00a7f invite");
            lores.add("\u00a7fother players to the island if");
            lores.add("\u00a7fthere is enough room for more");
            lores.add("\u00a7fmembers. Click here to remove");
            lores.add("\u00a7fthis permission.");
        } else {
            meta2.setDisplayName("\u00a7cInvite Players");
            lores.add("\u00a7fThis player \u00a7ccannot\u00a7f invite");
            lores.add("\u00a7fother players to the island.");
            lores.add("\u00a7fClick here to add this permission.");
        }
        meta2.setLore(lores);
        invite.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{invite});
        lores.clear();
        meta2 = kick.getItemMeta();
        if (skyBlock.getIslandInfo(player).hasPerm(pname, "canKickOthers")) {
            meta2.setDisplayName("\u00a7aKick Players");
            lores.add("\u00a7fThis player \u00a7acan\u00a7f kick");
            lores.add("\u00a7fother players from the island,");
            lores.add("\u00a7fbut they are unable to kick");
            lores.add("\u00a7fthe island leader. Click here");
            lores.add("\u00a7fto remove this permission.");
        } else {
            meta2.setDisplayName("\u00a7cKick Players");
            lores.add("\u00a7fThis player \u00a7ccannot\u00a7f kick");
            lores.add("\u00a7fother players from the island.");
            lores.add("\u00a7fClick here to add this permission.");
        }
        meta2.setLore(lores);
        kick.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{kick});
        lores.clear();
        return menu;
    }

    public Inventory displayPartyGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79Island Group Members");
        IslandInfo islandInfo = skyBlock.getIslandInfo(player);
        final Set<String> memberList = islandInfo.getMembers();
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        final ItemMeta meta2 = sign.getItemMeta();
        meta2.setDisplayName("\u00a7aGroup Info");
        lores.add("Group Members: \u00a72" + islandInfo.getPartySize() + "\u00a77/\u00a7e" + islandInfo.getMaxPartySize());
        if (islandInfo.getPartySize() < islandInfo.getMaxPartySize()) {
            lores.add("\u00a7aMore players can be invited to this island.");
        } else {
            lores.add("\u00a7cThis island is full.");
        }
        lores.add("\u00a7eHover over a player's icon to");
        lores.add("\u00a7eview their permissions. The");
        lores.add("\u00a7eleader can change permissions");
        lores.add("\u00a7eby clicking a player's icon.");
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        for (String temp : memberList) {
            if (temp.equalsIgnoreCase(islandInfo.getLeader())) {
                meta3.setDisplayName("\u00a7f" + temp);
                lores.add("\u00a7a\u00a7lLeader");
                lores.add("\u00a7aCan \u00a7fchange the island's biome.");
                lores.add("\u00a7aCan \u00a7flock/unlock the island.");
                lores.add("\u00a7aCan \u00a7fset the island's warp.");
                lores.add("\u00a7aCan \u00a7ftoggle the island's warp.");
                lores.add("\u00a7aCan \u00a7finvite others to the island.");
                lores.add("\u00a7aCan \u00a7fkick others from the island.");
                meta3.setLore(lores);
                lores.clear();
            } else {
                meta3.setDisplayName("\u00a7f" + temp);
                lores.add("\u00a7e\u00a7lMember");
                if (islandInfo.hasPerm(temp, "canChangeBiome")) {
                    lores.add("\u00a7aCan \u00a7fchange the island's biome.");
                } else {
                    lores.add("\u00a7cCannot \u00a7fchange the island's biome.");
                }
                if (islandInfo.hasPerm(temp, "canToggleLock")) {
                    lores.add("\u00a7aCan \u00a7flock/unlock the island.");
                } else {
                    lores.add("\u00a7cCannot \u00a7flock/unlock the island.");
                }
                if (islandInfo.hasPerm(temp, "canChangeWarp")) {
                    lores.add("\u00a7aCan \u00a7fset the island's warp.");
                } else {
                    lores.add("\u00a7cCannot \u00a7fset the island's warp.");
                }
                if (islandInfo.hasPerm(temp, "canToggleWarp")) {
                    lores.add("\u00a7aCan \u00a7ftoggle the island's warp.");
                } else {
                    lores.add("\u00a7cCannot \u00a7ftoggle the island's warp.");
                }
                if (islandInfo.hasPerm(temp, "canInviteOthers")) {
                    lores.add("\u00a7aCan \u00a7finvite others to the island.");
                } else {
                    lores.add("\u00a7cCannot \u00a7finvite others to the island.");
                }
                if (islandInfo.hasPerm(temp, "canKickOthers")) {
                    lores.add("\u00a7aCan \u00a7fkick others from the island.");
                } else {
                    lores.add("\u00a7cCannot \u00a7fkick others from the island.");
                }
                if (player.getName().equalsIgnoreCase(islandInfo.getLeader())) {
                    lores.add("\u00a7e<Click to change this player's permissions>");
                }
                meta3.setLore(lores);
                lores.clear();
            }
            meta3.setOwner(temp);
            pHead.setItemMeta(meta3);
            menu.addItem(new ItemStack[]{pHead});
        }
        return menu;
    }

    public Inventory displayLogGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 9, "\u00a79Island Log");
        ItemMeta meta4 = sign.getItemMeta();
        meta4.setDisplayName("\u00a7lIsland Log");
        lores.add("\u00a7eClick here to return to");
        lores.add("\u00a7ethe main island screen.");
        meta4.setLore(lores);
        sign.setItemMeta(meta4);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        ItemStack menuItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7e\u00a7lIsland Log");
        for (String log : skyBlock.getIslandInfo(player).getLog()) {
            lores.add(log);
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem);
        lores.clear();
        return menu;
    }

    public Inventory displayBiomeGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79Island Biome");
        ItemMeta meta4 = sign.getItemMeta();
        meta4.setDisplayName("\u00a7hIsland Biome");
        lores.add("\u00a7eClick here to return to");
        lores.add("\u00a7ethe main island screen.");
        meta4.setLore(lores);
        sign.setItemMeta(meta4);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        ItemStack menuItem = new ItemStack(Material.RAW_FISH, 1, (short) 2);
        meta4 = menuItem.getItemMeta();
        String currentBiome = skyBlock.getCurrentBiome(player);
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.ocean", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Ocean");
            lores.add("\u00a7fThe ocean biome is the basic");
            lores.add("\u00a7fstarting biome for all islands.");
            lores.add("\u00a7fpassive mobs like animals will");
            lores.add("\u00a7fnot spawn. Hostile mobs will");
            lores.add("\u00a7fspawn normally.");
            if ("OCEAN".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Ocean");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The ocean biome is the basic");
            lores.add("\u00a77starting biome for all islands.");
            lores.add("\u00a77passive mobs like animals will");
            lores.add("\u00a77not spawn. Hostile mobs will");
            lores.add("\u00a77spawn normally.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SAPLING, 1, (short) 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.forst", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Forest");
            lores.add("\u00a7fThe forest biome will allow");
            lores.add("\u00a7fyour island to spawn passive.");
            lores.add("\u00a7fmobs like animals (including");
            lores.add("\u00a7fwolves). Hostile mobs will");
            lores.add("\u00a7fspawn normally.");
            if ("FOREST".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Forest");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The forest biome will allow");
            lores.add("\u00a77your island to spawn passive.");
            lores.add("\u00a77mobs like animals (including");
            lores.add("\u00a77wolves). Hostile mobs will");
            lores.add("\u00a77spawn normally.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SAND, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.desert", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Desert");
            lores.add("\u00a7fThe desert biome makes it so");
            lores.add("\u00a7fthat there is no rain or snow");
            lores.add("\u00a7fon your island. Passive mobs");
            lores.add("\u00a7fwon't spawn. Hostile mobs will");
            lores.add("\u00a7fspawn normally.");
            if ("DESERT".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Desert");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The desert biome makes it so");
            lores.add("\u00a77that there is no rain or snow");
            lores.add("\u00a77on your island. Passive mobs");
            lores.add("\u00a77won't spawn. Hostile mobs will");
            lores.add("\u00a77spawn normally.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.jungle", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Jungle");
            lores.add("\u00a7fThe jungle biome is bright");
            lores.add("\u00a7fand colorful. Passive mobs");
            lores.add("\u00a7f(including ocelots) will");
            lores.add("\u00a7fspawn. Hostile mobs will");
            lores.add("\u00a7fspawn normally.");
            if ("JUNGLE".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Jungle");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The jungle biome is bright");
            lores.add("\u00a77and colorful. Passive mobs");
            lores.add("\u00a77(including ocelots) will");
            lores.add("\u00a77spawn. Hostile mobs will");
            lores.add("\u00a77spawn normally.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.WATER_LILY, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.swampland", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Swampland");
            lores.add("\u00a7fThe swamp biome is dark");
            lores.add("\u00a7fand dull. Passive mobs");
            lores.add("\u00a7fwill spawn normally and");
            lores.add("\u00a7fslimes have a small chance");
            lores.add("\u00a7fto spawn at night depending");
            lores.add("\u00a7fon the moon phase.");
            if ("SWAMPLAND".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Swampland");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The swamp biome is dark");
            lores.add("\u00a77and dull. Passive mobs");
            lores.add("\u00a77will spawn normally and");
            lores.add("\u00a77slimes have a small chance");
            lores.add("\u00a77to spawn at night depending");
            lores.add("\u00a77on the moon phase.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SNOW, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.taiga", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Taiga");
            lores.add("\u00a7fThe taiga biome has snow");
            lores.add("\u00a7finstead of rain. Passive");
            lores.add("\u00a7fmobs will spawn normally");
            lores.add("\u00a7f(including wolves) and");
            lores.add("\u00a7fhostile mobs will spawn.");
            if ("TAIGA".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Taiga");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The taiga biome has snow");
            lores.add("\u00a77instead of rain. Passive");
            lores.add("\u00a77mobs will spawn normally");
            lores.add("\u00a77(including wolves) and");
            lores.add("\u00a77hostile mobs will spawn.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.RED_MUSHROOM, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.mushroom", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Mushroom");
            lores.add("\u00a7fThe mushroom biome is");
            lores.add("\u00a7fbright and colorful.");
            lores.add("\u00a7fMooshrooms are the only");
            lores.add("\u00a7fmobs that will spawn.");
            lores.add("\u00a7fNo other passive or");
            lores.add("\u00a7fhostile mobs will spawn.");
            if ("MUSHROOM".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Mushroom");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The mushroom biome is");
            lores.add("\u00a77bright and colorful.");
            lores.add("\u00a77Mooshrooms are the only");
            lores.add("\u00a77mobs that will spawn.");
            lores.add("\u00a77No other passive or");
            lores.add("\u00a77hostile mobs will spawn.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.NETHER_BRICK, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.hell", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Hell(Nether)");
            lores.add("\u00a7fThe hell biome looks");
            lores.add("\u00a7fdark and dead. Some");
            lores.add("\u00a7fmobs from the nether will");
            lores.add("\u00a7fspawn in this biome");
            lores.add("\u00a7f(excluding ghasts and");
            lores.add("\u00a7fblazes).");
            if ("HELL".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Hell(Nether)");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The hell biome looks");
            lores.add("\u00a77dark and dead. Some");
            lores.add("\u00a77mobs from the nether will");
            lores.add("\u00a77spawn in this biome");
            lores.add("\u00a77(excluding ghasts and");
            lores.add("\u00a77blazes).");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.EYE_OF_ENDER, 1);
        meta4 = menuItem.getItemMeta();
        if (VaultHandler.checkPerk(player.getName(), "usb.biome.sky", player.getWorld())) {
            meta4.setDisplayName("\u00a7aBiome: Sky(End)");
            lores.add("\u00a7fThe sky biome gives your");
            lores.add("\u00a7fisland a special dark sky.");
            lores.add("\u00a7fOnly endermen will spawn");
            lores.add("\u00a7fin this biome.");
            if ("SKY".equals(currentBiome)) {
                lores.add("\u00a72\u00a7lThis is your current biome.");
            } else {
                lores.add("\u00a7e\u00a7lClick to change to this biome.");
            }
        } else {
            meta4.setDisplayName("\u00a78Biome: Sky(End)");
            lores.add("\u00a7cYou cannot use this biome.");
            lores.add("\u00a77The sky biome gives your");
            lores.add("\u00a77island a special dark sky.");
            lores.add("\u00a77Only endermen will spawn");
            lores.add("\u00a77in this biome.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        return menu;
    }

    private void addExtraMenus(Player player, Inventory menu) {
        ConfigurationSection extras = skyBlock.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null) {
            return;
        }
        World world = skyBlock.getSkyBlockWorld();
        for (String sIndex : extras.getKeys(false)) {
            ConfigurationSection menuSection = extras.getConfigurationSection(sIndex);
            if (menuSection == null) {
                continue;
            }
            try {
                int index = Integer.parseInt(sIndex, 10);
                String title = menuSection.getString("title", "\u00a9Unknown");
                String icon = menuSection.getString("displayItem", "CHEST");
                List<String> lores = new ArrayList<>();
                for (String l : menuSection.getStringList("lore")) {
                    Matcher matcher = PERM_VALUE_PATTERN.matcher(l);
                    if (matcher.matches()) {
                        String perm = matcher.group("perm");
                        String lore = matcher.group("value");
                        boolean not = matcher.group("not") != null;
                        if (perm != null) {
                            boolean hasPerm = VaultHandler.checkPerm(player, perm, world);
                            if ((hasPerm && !not) || (!hasPerm && not)) {
                                lores.add(lore);
                            }
                        } else {
                            lores.add(lore);
                        }
                    }
                }
                // Only SIMPLE icons supported...
                ItemStack item = new ItemStack(Material.matchMaterial(icon), 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(title);
                meta.setLore(lores);
                item.setItemMeta(meta);
                menu.setItem(index, item);
            } catch (Exception e) {
                uSkyBlock.log(Level.INFO, "\u00a79[uSkyBlock]\u00a7r Unable to add extra-menu " + sIndex + ": " + e);
            }
        }
    }

    private boolean isExtraMenuAction(Player player, ItemStack currentItem) {
        ConfigurationSection extras = skyBlock.getConfig().getConfigurationSection("options.extra-menus");
        if (extras == null || currentItem == null || currentItem.getItemMeta() == null) {
            return false;
        }
        Material itemType = currentItem.getType();
        String itemTitle = currentItem.getItemMeta().getDisplayName();
        World world = skyBlock.getSkyBlockWorld();
        for (String sIndex : extras.getKeys(false)) {
            ConfigurationSection menuSection = extras.getConfigurationSection(sIndex);
            if (menuSection == null) {
                continue;
            }
            try {
                String title = menuSection.getString("title", "\u00a9Unknown");
                String icon = menuSection.getString("displayItem", "CHEST");
                Material material = Material.matchMaterial(icon);
                if (title.equals(itemTitle) && material == itemType) {
                    for (String command : menuSection.getStringList("commands")) {
                        Matcher matcher = PERM_VALUE_PATTERN.matcher(command);
                        if (matcher.matches()) {
                            String perm = matcher.group("perm");
                            String cmd = matcher.group("value");
                            boolean not = matcher.group("not") != null;
                            if (perm != null) {
                                boolean hasPerm = VaultHandler.checkPerm(player, perm, world);
                                if ((hasPerm && !not) || (!hasPerm && not)) {
                                    player.performCommand(cmd);
                                }
                            } else {
                                player.performCommand(cmd);
                            }
                        } else {
                            uSkyBlock.log(Level.INFO, "\u00a7a[uSkyBlock] Malformed menu " + title + ", invalid command : " + command);
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                uSkyBlock.log(Level.INFO, "\u00a79[uSkyBlock]\u00a7r Unable to execute commands for extra-menu " + sIndex + ": " + e);
            }
        }
        return false;
    }

    public Inventory displayChallengeGUI(final Player player) {
        Inventory menu = Bukkit.createInventory(null, 36, "\u00a79Challenge Menu");
        final PlayerInfo pi = skyBlock.getPlayerInfo(player);
        // TODO: 06/12/2014 - R4zorax: Support more challenge-ranks (i.e. pagination on "header item".
        challengeLogic.populateChallengeRank(menu, player, 0, Material.DIRT, 0, pi);
        challengeLogic.populateChallengeRank(menu, player, 1, Material.IRON_BLOCK, 9, pi);
        challengeLogic.populateChallengeRank(menu, player, 2, Material.GOLD_BLOCK, 18, pi);
        challengeLogic.populateChallengeRank(menu, player, 3, Material.DIAMOND_BLOCK, 27, pi);
        return menu;
    }

    public Inventory displayIslandGUI(final Player player) {
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79Island Menu");
        if (skyBlock.hasIsland(player.getName())) {
            addMainMenu(menu, player);
        } else {
            addInitMenu(menu);
        }
        return menu;
    }

    private void addInitMenu(Inventory menu) {
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.GRASS, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lStart an Island");
        lores.add("\u00a7fStart your skyblock journey");
        lores.add("\u00a7fby starting your own island.");
        lores.add("\u00a7fComplete challenges to earn");
        lores.add("\u00a7fitems and skybucks to help");
        lores.add("\u00a7fexpand your skyblock. You can");
        lores.add("\u00a7finvite others to join in");
        lores.add("\u00a7fbuilding your island empire!");
        lores.add("\u00a7e\u00a7lClick here to start!");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName("\u00a7a\u00a7lJoin an Island");
        lores.add("\u00a7fWant to join another player's");
        lores.add("\u00a7fisland instead of starting");
        lores.add("\u00a7fyour own? If another player");
        lores.add("\u00a7finvites you to their island");
        lores.add("\u00a7fyou can click here or use");
        lores.add("\u00a7e/island accept \u00a7fto join them.");
        lores.add("\u00a7e\u00a7lClick here to accept an invite!");
        lores.add("\u00a7e\u00a7l(You must be invited first)");
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.setItem(4, menuItem);
        lores.clear();
        menuItem = new ItemStack(Material.SIGN, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Help");
        lores.add("\u00a7fNeed help with skyblock");
        lores.add("\u00a7fconcepts or commands? View");
        lores.add("\u00a7fdetails about them here.");
        lores.add("\u00a7e\u00a7lClick here for help!");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem);
        lores.clear();
    }

    private void addMainMenu(Inventory menu, Player player) {
        List<String> lores = new ArrayList<>();
        ItemStack menuItem = new ItemStack(Material.WOOD_DOOR, 1);
        ItemMeta meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lReturn Home");
        lores.add("\u00a7fReturn to your island's home");
        lores.add("\u00a7fpoint. You can change your home");
        lores.add("\u00a7fpoint to any location on your");
        lores.add("\u00a7fisland using \u00a7b/island sethome");
        lores.add("\u00a7e\u00a7lClick here to return home.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        IslandInfo islandInfo = skyBlock.getIslandInfo(player);

        menuItem = new ItemStack(Material.DIAMOND_ORE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChallenges");
        lores.add("\u00a7fView a list of \u00a79challenges\u00a7f that");
        lores.add("\u00a7fyou can complete on your island");
        lores.add("\u00a7fto earn skybucks, items, perks,");
        lores.add("\u00a7fand titles.");
        lores.add("\u00a7e\u00a7lClick here to view challenges.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);

        lores.clear();
        menuItem = new ItemStack(Material.EXP_BOTTLE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Level");
        lores.add("\u00a7eCurrent Level: \u00a7a" + islandInfo.getLevel());
        lores.add("\u00a7fGain island levels by expanding");
        lores.add("\u00a7fyour skyblock and completing");
        lores.add("\u00a7fcertain challenges. Rarer blocks");
        lores.add("\u00a7fwill add more to your level.");
        lores.add("\u00a7e\u00a7lClick here to refresh.");
        lores.add("\u00a7e\u00a7l(must be on island)");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) menuItem.getItemMeta();
        meta2.setDisplayName("\u00a7a\u00a7lIsland Group");
        lores.add("\u00a7eMembers: \u00a72" + islandInfo.getPartySize() + "/" + islandInfo.getMaxPartySize());
        lores.add("\u00a7fView the members of your island");
        lores.add("\u00a7fgroup and their permissions. If");
        lores.add("\u00a7fyou are the island leader, you");
        lores.add("\u00a7fcan change the member permissions.");
        lores.add("\u00a7e\u00a7lClick here to view or change.");
        meta2.setLore(lores);
        menuItem.setItemMeta(meta2);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChange Island Biome");
        lores.add("\u00a7eCurrent Biome: \u00a7b" + islandInfo.getBiome());
        lores.add("\u00a7fThe island biome affects things");
        lores.add("\u00a7flike grass color and spawning");
        lores.add("\u00a7fof both animals and monsters.");
        if (islandInfo.hasPerm(player, "canChangeBiome")) {
            lores.add("\u00a7e\u00a7lClick here to change biomes.");
        } else {
            lores.add("\u00a7c\u00a7lYou can't change the biome.");
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.IRON_FENCE, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Lock");
        if (skyBlock.getIslandInfo(player).isLocked()) {
            lores.add("\u00a7eLock Status: \u00a7aActive");
            lores.add("\u00a7fYour island is currently \u00a7clocked.");
            lores.add("\u00a7fPlayers outside of your group");
            lores.add("\u00a7fare unable to enter your island.");
            if (islandInfo.hasPerm(player, "canToggleLock")) {
                lores.add("\u00a7e\u00a7lClick here to unlock your island.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the lock.");
            }
        } else {
            lores.add("\u00a7eLock Status: \u00a78Inactive");
            lores.add("\u00a7fYour island is currently \u00a7aunlocked.");
            lores.add("\u00a7fAll players are able to enter your");
            lores.add("\u00a7fisland, but only you and your group");
            lores.add("\u00a7fmembers may build there.");
            if (islandInfo.hasPerm(player, "canToggleLock")) {
                lores.add("\u00a7e\u00a7lClick here to lock your island.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the lock.");
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        if (skyBlock.getIslandInfo(player).hasWarp()) {
            menuItem = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName("\u00a7a\u00a7lIsland Warp");
            lores.add("\u00a7eWarp Status: \u00a7aActive");
            lores.add("\u00a7fOther players may warp to your");
            lores.add("\u00a7fisland at anytime to the point");
            lores.add("\u00a7fyou set using \u00a7d/island setwarp.");
            if (islandInfo.hasPerm(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", skyBlock.getSkyBlockWorld())) {
                lores.add("\u00a7e\u00a7lClick here to deactivate.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the warp.");
            }
        } else {
            menuItem = new ItemStack(Material.ENDER_STONE, 1);
            meta4 = menuItem.getItemMeta();
            meta4.setDisplayName("\u00a7a\u00a7lIsland Warp");
            lores.add("\u00a7eWarp Status: \u00a78Inactive");
            lores.add("\u00a7fOther players can't warp to your");
            lores.add("\u00a7fisland. Set a warp point using");
            lores.add("\u00a7d/island setwarp \u00a7fbefore activating.");
            if (islandInfo.hasPerm(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", skyBlock.getSkyBlockWorld())) {
                lores.add("\u00a7e\u00a7lClick here to activate.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the warp.");
            }
        }
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.addItem(menuItem);
        lores.clear();

        menuItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Log");
        lores.add("\u00a7fView a log of events from");
        lores.add("\u00a7fyour island such as member,");
        lores.add("\u00a7fbiome, and warp changes.");
        lores.add("\u00a7e\u00a7lClick to view the log.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(8, menuItem); // Last item, first line
        lores.clear();

        menuItem = new ItemStack(Material.BED, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChange Home Location");
        lores.add("\u00a7fWhen you teleport to your");
        lores.add("\u00a7fisland you will be taken to");
        lores.add("\u00a7fthis location.");
        lores.add("\u00a7e\u00a7lClick here to change.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(9, menuItem); // First item, 2nd line
        lores.clear();

        menuItem = new ItemStack(Material.HOPPER, 1);
        meta4 = menuItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChange Warp Location");
        lores.add("\u00a7fWhen your warp is activated,");
        lores.add("\u00a7fother players will be taken to");
        lores.add("\u00a7fthis point when they teleport");
        lores.add("\u00a7fto your island.");
        lores.add("\u00a7e\u00a7lClick here to change.");
        meta4.setLore(lores);
        menuItem.setItemMeta(meta4);
        menu.setItem(15, menuItem);
        lores.clear();
        addExtraMenus(player, menu);
    }

    public void onClick(InventoryClickEvent event) {
        if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null || event.getSlotType() != InventoryType.SlotType.CONTAINER) {
            return; // Bail out, nothing we can do anyway
        }
        Player p = (Player) event.getWhoClicked();
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        SkullMeta skull = meta instanceof SkullMeta ? (SkullMeta) meta : null;
        if (event.getInventory().getName().equalsIgnoreCase("\u00a79Island Group Members")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (meta == null || event.getCurrentItem().getType() == Material.SIGN) {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            } else if (meta.getLore().contains("\u00a7a\u00a7lLeader")) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else if (!uSkyBlock.getInstance().isPartyLeader(p)) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else if (skull != null) {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, skull.getOwner()));
            }
        } else if (event.getInventory().getName().contains("Permissions")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            String[] playerPerm = event.getInventory().getName().split(" ");
            IslandInfo islandInfo = skyBlock.getIslandInfo(p);
            // TODO: 19/12/2014 - R4zorax: Make this more robust!
            if (event.getCurrentItem().getTypeId() == 6) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canChangeBiome");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 101) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canToggleLock");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 90) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canChangeWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 69) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canToggleWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 398) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canInviteOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 301) {
                p.closeInventory();
                islandInfo.togglePerm(playerPerm[0], "canKickOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 323) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            }
        } else if (event.getInventory().getName().contains("Island Biome")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                p.closeInventory();
                p.performCommand("island biome jungle");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 1) {
                p.closeInventory();
                p.performCommand("island biome forest");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SAND) {
                p.closeInventory();
                p.performCommand("island biome desert");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SNOW) {
                p.closeInventory();
                p.performCommand("island biome taiga");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.EYE_OF_ENDER) {
                p.closeInventory();
                p.performCommand("island biome sky");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.WATER_LILY) {
                p.closeInventory();
                p.performCommand("island biome swampland");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.NETHER_BRICK) {
                p.closeInventory();
                p.performCommand("island biome hell");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RED_MUSHROOM) {
                p.closeInventory();
                p.performCommand("island biome mushroom");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.RAW_FISH) {
                p.closeInventory();
                p.performCommand("island biome ocean");
                p.openInventory(displayIslandGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            }
        } else if (event.getInventory().getName().contains("Challenge Menu")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (event.getCurrentItem().getType() != Material.DIRT && event.getCurrentItem().getType() != Material.IRON_BLOCK && event.getCurrentItem().getType() != Material.GOLD_BLOCK && event.getCurrentItem().getType() != Material.DIAMOND_BLOCK) {
                p.closeInventory();
                if (event.getCurrentItem().getItemMeta() != null) {
                    String challenge = event.getCurrentItem().getItemMeta().getDisplayName();
                    String challengeName = uSkyBlock.stripFormatting(challenge);
                    p.performCommand("c c " + challengeName);
                }
                p.openInventory(displayChallengeGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayIslandGUI(p));
            }
        } else if (event.getInventory().getName().contains("Island Log")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            p.closeInventory();
            p.openInventory(displayIslandGUI(p));
        } else if (event.getInventory().getName().contains("Island Menu")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            PlayerInfo playerInfo = skyBlock.getPlayerInfo(p);
            IslandInfo islandInfo = skyBlock.getIslandInfo(playerInfo);
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                p.closeInventory();
                p.performCommand("island biome");
            } else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                p.closeInventory();
                p.performCommand("island party");
            } else if (event.getCurrentItem().getType() == Material.BED) {
                p.closeInventory();
                p.performCommand("island sethome");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.HOPPER) {
                p.closeInventory();
                p.performCommand("island setwarp");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL) {
                p.closeInventory();
                p.performCommand("island log");
            } else if (event.getCurrentItem().getType() == Material.WOOD_DOOR) {
                p.closeInventory();
                p.performCommand("island home");
            } else if (event.getCurrentItem().getType() == Material.GRASS) {
                p.closeInventory();
                p.performCommand("island create");
            } else if (event.getCurrentItem().getType() == Material.EXP_BOTTLE) {
                p.closeInventory();
                p.performCommand("island level");
            } else if (event.getCurrentItem().getType() == Material.DIAMOND_ORE) {
                p.closeInventory();
                p.performCommand("c");
            } else if (event.getCurrentItem().getType() == Material.ENDER_STONE || event.getCurrentItem().getType() == Material.ENDER_PORTAL_FRAME) {
                p.closeInventory();
                p.performCommand("island togglewarp");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.IRON_FENCE && islandInfo.isLocked()) {
                p.closeInventory();
                p.performCommand("island unlock");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.IRON_FENCE && !islandInfo.isLocked()) {
                p.closeInventory();
                p.performCommand("island lock");
                p.openInventory(displayIslandGUI(p));
            } else if (event.getCurrentItem().getType() == Material.SIGN) {
                p.performCommand("chestcommands open island_help");
            } else {
                if (!isExtraMenuAction(p, event.getCurrentItem())) {
                    p.closeInventory();
                    p.openInventory(displayIslandGUI(p));
                }
            }
        }
    }

    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("\u00a79SB Island Group Members")) {
            event.setCancelled(true);
            SkullMeta meta = (SkullMeta) event.getCursor().getItemMeta();
            Player p = (Player) event.getWhoClicked();
            p.updateInventory();
            p.closeInventory();
            if (meta.getOwner() == null) {
                p.openInventory(displayPartyGUI(p));
            } else {
                p.openInventory(displayPartyPlayerGUI(p, meta.getOwner()));
            }
        }
    }
}

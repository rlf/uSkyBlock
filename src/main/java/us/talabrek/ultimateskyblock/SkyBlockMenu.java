package us.talabrek.ultimateskyblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

// TODO: Move all the texts to resource-files (translatable).
/**
 * The UI menu of uSkyBlock (using the inventory UI).
 */
public class SkyBlockMenu {
    List<String> lores;
    private uSkyBlock skyBlock;
    public Inventory GUIparty;
    public Inventory GUIpartyPlayer;
    public Inventory GUIisland;
    public Inventory GUIchallenge;
    public Inventory GUIbiome;
    public Inventory GUIlog;
    ItemStack pHead;
    ItemStack sign;
    ItemStack biome;
    ItemStack lock;
    ItemStack warpset;
    ItemStack warptoggle;
    ItemStack invite;
    ItemStack kick;
    ItemStack currentBiomeItem;
    ItemStack currentIslandItem;
    ItemStack currentLogItem;
    Iterator<String> tempIt;

    public SkyBlockMenu(uSkyBlock skyBlock) {
        this.skyBlock = skyBlock;
        this.GUIparty = null;
        this.GUIpartyPlayer = null;
        this.GUIisland = null;
        this.GUIchallenge = null;
        this.GUIbiome = null;
        this.GUIlog = null;
        this.pHead = new ItemStack(397, 1, (short) 3);
        this.sign = new ItemStack(323, 1);
        this.biome = new ItemStack(6, 1, (short) 3);
        this.lock = new ItemStack(101, 1);
        this.warpset = new ItemStack(90, 1);
        this.warptoggle = new ItemStack(69, 1);
        this.invite = new ItemStack(398, 1);
        this.kick = new ItemStack(301, 1);
        this.currentBiomeItem = null;
        this.currentIslandItem = null;
        this.currentLogItem = null;
        this.lores = new ArrayList<>();
    }

    public Inventory displayPartyPlayerGUI(final Player player, final String pname) {
        this.GUIpartyPlayer = Bukkit.createInventory(null, 9, pname + " <Permissions>");
        final ItemStack pHead = new ItemStack(397, 1, (short) 3);
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        ItemMeta meta2 = this.sign.getItemMeta();
        meta2.setDisplayName("\u00a7hPlayer Permissions");
        lores.add("\u00a7eClick here to return to");
        lores.add("\u00a7eyour island group's info.");
        meta2.setLore((List) this.lores);
        this.sign.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.sign});
        this.lores.clear();
        meta3.setDisplayName(pname + "'s Permissions");
        lores.add("\u00a7eHover over an icon to view");
        lores.add("\u00a7ea permission. Change the");
        lores.add("\u00a7epermission by clicking it.");
        meta3.setLore((List) this.lores);
        pHead.setItemMeta(meta3);
        this.GUIpartyPlayer.addItem(new ItemStack[]{pHead});
        this.lores.clear();
        meta2 = this.biome.getItemMeta();
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + pname + ".canChangeBiome")) {
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
        meta2.setLore((List) this.lores);
        this.biome.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.biome});
        this.lores.clear();
        meta2 = this.lock.getItemMeta();
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + pname + ".canToggleLock")) {
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
        meta2.setLore((List) this.lores);
        this.lock.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.lock});
        this.lores.clear();
        meta2 = this.warpset.getItemMeta();
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + pname + ".canChangeWarp")) {
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
        meta2.setLore((List) this.lores);
        this.warpset.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.warpset});
        this.lores.clear();
        meta2 = this.warptoggle.getItemMeta();
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + pname + ".canToggleWarp")) {
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
        meta2.setLore((List) this.lores);
        this.warptoggle.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.warptoggle});
        this.lores.clear();
        meta2 = this.invite.getItemMeta();
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + pname + ".canInviteOthers")) {
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
        meta2.setLore((List) this.lores);
        this.invite.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.invite});
        this.lores.clear();
        meta2 = this.kick.getItemMeta();
        if (skyBlock.getIslandConfig(player).getBoolean("party.members." + pname + ".canKickOthers")) {
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
        meta2.setLore((List) this.lores);
        this.kick.setItemMeta(meta2);
        this.GUIpartyPlayer.addItem(new ItemStack[]{this.kick});
        this.lores.clear();
        return this.GUIpartyPlayer;
    }

    public Inventory displayPartyGUI(final Player player) {
        this.GUIparty = Bukkit.createInventory(null, 18, "\u00a79Island Group Members");
        final Set<String> memberList = skyBlock.getIslandConfig(player).getConfigurationSection("party.members").getKeys(false);
        this.tempIt = memberList.iterator();
        final SkullMeta meta3 = (SkullMeta) this.pHead.getItemMeta();
        final ItemMeta meta2 = this.sign.getItemMeta();
        meta2.setDisplayName("\u00a7aGroup Info");
        lores.add("Group Members: \u00a72" + skyBlock.getIslandConfig(player).getInt("party.currentSize") + "\u00a77/\u00a7e" + skyBlock.getIslandConfig(player).getInt("party.maxSize"));
        if (skyBlock.getIslandConfig(player).getInt("party.currentSize") < skyBlock.getIslandConfig(player).getInt("party.maxSize")) {
            lores.add("\u00a7aMore players can be invited to this island.");
        } else {
            lores.add("\u00a7cThis island is full.");
        }
        lores.add("\u00a7eHover over a player's icon to");
        lores.add("\u00a7eview their permissions. The");
        lores.add("\u00a7eleader can change permissions");
        lores.add("\u00a7eby clicking a player's icon.");
        meta2.setLore((List) this.lores);
        this.sign.setItemMeta(meta2);
        this.GUIparty.addItem(new ItemStack[]{this.sign});
        this.lores.clear();
        while (this.tempIt.hasNext()) {
            final String temp = this.tempIt.next();
            if (temp.equalsIgnoreCase(skyBlock.getIslandConfig(player).getString("party.leader"))) {
                meta3.setDisplayName("\u00a7f" + temp);
                lores.add("\u00a7a\u00a7lLeader");
                lores.add("\u00a7aCan \u00a7fchange the island's biome.");
                lores.add("\u00a7aCan \u00a7flock/unlock the island.");
                lores.add("\u00a7aCan \u00a7fset the island's warp.");
                lores.add("\u00a7aCan \u00a7ftoggle the island's warp.");
                lores.add("\u00a7aCan \u00a7finvite others to the island.");
                lores.add("\u00a7aCan \u00a7fkick others from the island.");
                meta3.setLore((List) this.lores);
                this.lores.clear();
            } else {
                meta3.setDisplayName("\u00a7f" + temp);
                lores.add("\u00a7e\u00a7lMember");
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canChangeBiome")) {
                    lores.add("\u00a7aCan \u00a7fchange the island's biome.");
                } else {
                    lores.add("\u00a7cCannot \u00a7fchange the island's biome.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canToggleLock")) {
                    lores.add("\u00a7aCan \u00a7flock/unlock the island.");
                } else {
                    lores.add("\u00a7cCannot \u00a7flock/unlock the island.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canChangeWarp")) {
                    lores.add("\u00a7aCan \u00a7fset the island's warp.");
                } else {
                    lores.add("\u00a7cCannot \u00a7fset the island's warp.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canToggleWarp")) {
                    lores.add("\u00a7aCan \u00a7ftoggle the island's warp.");
                } else {
                    lores.add("\u00a7cCannot \u00a7ftoggle the island's warp.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canInviteOthers")) {
                    lores.add("\u00a7aCan \u00a7finvite others to the island.");
                } else {
                    lores.add("\u00a7cCannot \u00a7finvite others to the island.");
                }
                if (skyBlock.getIslandConfig(player).getBoolean("party.members." + temp + ".canKickOthers")) {
                    lores.add("\u00a7aCan \u00a7fkick others from the island.");
                } else {
                    lores.add("\u00a7cCannot \u00a7fkick others from the island.");
                }
                if (player.getName().equalsIgnoreCase(skyBlock.getIslandConfig(player).getString("party.leader"))) {
                    lores.add("\u00a7e<Click to change this player's permissions>");
                }
                meta3.setLore((List) this.lores);
                this.lores.clear();
            }
            meta3.setOwner(temp);
            this.pHead.setItemMeta(meta3);
            this.GUIparty.addItem(new ItemStack[]{this.pHead});
        }
        return this.GUIparty;
    }

    public Inventory displayLogGUI(final Player player) {
        this.GUIlog = Bukkit.createInventory(null, 9, "\u00a79Island Log");
        ItemMeta meta4 = this.sign.getItemMeta();
        meta4.setDisplayName("\u00a7lIsland Log");
        lores.add("\u00a7eClick here to return to");
        lores.add("\u00a7ethe main island screen.");
        meta4.setLore((List) this.lores);
        this.sign.setItemMeta(meta4);
        this.GUIlog.addItem(new ItemStack[]{this.sign});
        this.lores.clear();
        this.currentLogItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = this.currentLogItem.getItemMeta();
        meta4.setDisplayName("\u00a7e\u00a7lIsland Log");
        FileConfiguration islandConfig = skyBlock.getIslandConfig(player);
        for (int i = 1; i <= 10; ++i) {
            if (islandConfig.contains("log." + i)) {
                lores.add(islandConfig.getString("log." + i));
            } else {
                break;
            }
        }
        meta4.setLore((List) this.lores);
        this.currentLogItem.setItemMeta(meta4);
        this.GUIlog.setItem(8, this.currentLogItem);
        this.lores.clear();
        return this.GUIlog;
    }

    public Inventory displayBiomeGUI(final Player player) {
        this.GUIbiome = Bukkit.createInventory(null, 18, "\u00a79Island Biome");
        ItemMeta meta4 = this.sign.getItemMeta();
        meta4.setDisplayName("\u00a7hIsland Biome");
        lores.add("\u00a7eClick here to return to");
        lores.add("\u00a7ethe main island screen.");
        meta4.setLore((List) this.lores);
        this.sign.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.sign});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.WATER, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SAPLING, 1, (short) 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SAND, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.WATER_LILY, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.SNOW, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.RED_MUSHROOM, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.FIRE, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        this.currentBiomeItem = new ItemStack(Material.EYE_OF_ENDER, 1);
        meta4 = this.currentBiomeItem.getItemMeta();
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
        meta4.setLore((List) this.lores);
        this.currentBiomeItem.setItemMeta(meta4);
        this.GUIbiome.addItem(new ItemStack[]{this.currentBiomeItem});
        this.lores.clear();
        return this.GUIbiome;
    }

    public Inventory displayChallengeGUI(final Player player) {
        this.GUIchallenge = Bukkit.createInventory(null, 36, "\u00a79Challenge Menu");
        final PlayerInfo pi = skyBlock.getActivePlayers().get(player.getName());
        this.populateChallengeRank(player, 0, Material.DIRT, 0, pi);
        this.populateChallengeRank(player, 1, Material.IRON_BLOCK, 9, pi);
        this.populateChallengeRank(player, 2, Material.GOLD_BLOCK, 18, pi);
        this.populateChallengeRank(player, 3, Material.DIAMOND_BLOCK, 27, pi);
        return this.GUIchallenge;
    }

    public Inventory displayIslandGUI(final Player player) {
        this.GUIisland = Bukkit.createInventory(null, 18, "\u00a79Island Menu");
        if (skyBlock.hasIsland(player.getName())) {
            showMainMenu(player);
        } else if (hasAccess(player)) {
            showInitMenu();
        } else {
            this.currentIslandItem = new ItemStack(Material.BOOK, 1);
            final ItemMeta meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("\u00a7a\u00a7lWelcome to the Server!");
            lores.add("\u00a7fPlease read and accept the");
            lores.add("\u00a7fserver rules to become a");
            lores.add("\u00a7fmember and start your skyblock.");
            lores.add("\u00a7e\u00a7lClick here to read!");
            meta4.setLore((List) this.lores);
            this.currentIslandItem.setItemMeta(meta4);
            this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
            this.lores.clear();
        }
        return this.GUIisland;
    }

    private boolean hasAccess(Player player) {
        return VaultHandler.checkPerm(player, skyBlock.getConfig().getString("options.general.permission"), skyBlock.getSkyBlockWorld());
    }

    private void showInitMenu() {
        this.currentIslandItem = new ItemStack(Material.GRASS, 1);
        ItemMeta meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lStart an Island");
        lores.add("\u00a7fStart your skyblock journey");
        lores.add("\u00a7fby starting your own island.");
        lores.add("\u00a7fComplete challenges to earn");
        lores.add("\u00a7fitems and skybucks to help");
        lores.add("\u00a7fexpand your skyblock. You can");
        lores.add("\u00a7finvite others to join in");
        lores.add("\u00a7fbuilding your island empire!");
        lores.add("\u00a7e\u00a7lClick here to start!");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) this.currentIslandItem.getItemMeta();
        meta2.setDisplayName("\u00a7a\u00a7lJoin an Island");
        lores.add("\u00a7fWant to join another player's");
        lores.add("\u00a7fisland instead of starting");
        lores.add("\u00a7fyour own? If another player");
        lores.add("\u00a7finvites you to their island");
        lores.add("\u00a7fyou can click here or use");
        lores.add("\u00a7e/island accept \u00a7fto join them.");
        lores.add("\u00a7e\u00a7lClick here to accept an invite!");
        lores.add("\u00a7e\u00a7l(You must be invited first)");
        meta2.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta2);
        this.GUIisland.setItem(4, this.currentIslandItem);
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.SIGN, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Help");
        lores.add("\u00a7fNeed help with skyblock");
        lores.add("\u00a7fconcepts or commands? View");
        lores.add("\u00a7fdetails about them here.");
        lores.add("\u00a7e\u00a7lClick here for help!");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.setItem(8, this.currentIslandItem);
        this.lores.clear();
    }

    private void showMainMenu(Player player) {
        this.currentIslandItem = new ItemStack(Material.ENDER_PORTAL, 1);
        ItemMeta meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lReturn Home");
        lores.add("\u00a7fReturn to your island's home");
        lores.add("\u00a7fpoint. You can change your home");
        lores.add("\u00a7fpoint to any location on your");
        lores.add("\u00a7fisland using \u00a7b/island sethome");
        lores.add("\u00a7e\u00a7lClick here to return home.");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.DIAMOND_ORE, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChallenges");
        lores.add("\u00a7fView a list of challenges that");
        lores.add("\u00a7fyou can complete on your island");
        lores.add("\u00a7fto earn skybucks, items, perks,");
        lores.add("\u00a7fand titles.");
        lores.add("\u00a7e\u00a7lClick here to view challenges.");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.EXP_BOTTLE, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Level");
        lores.add("\u00a7eCurrent Level: \u00a7a" + this.showIslandLevel(player));
        lores.add("\u00a7fGain island levels by expanding");
        lores.add("\u00a7fyour skyblock and completing");
        lores.add("\u00a7fcertain challenges. Rarer blocks");
        lores.add("\u00a7fwill add more to your level.");
        lores.add("\u00a7e\u00a7lClick here to refresh.");
        lores.add("\u00a7e\u00a7l(must be on island)");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        final SkullMeta meta2 = (SkullMeta) this.currentIslandItem.getItemMeta();
        meta2.setDisplayName("\u00a7a\u00a7lIsland Group");
        lores.add("\u00a7eMembers: \u00a72" + this.showCurrentMembers(player) + "/" + this.showMaxMembers(player));
        lores.add("\u00a7fView the members of your island");
        lores.add("\u00a7fgroup and their permissions. If");
        lores.add("\u00a7fyou are the island leader, you");
        lores.add("\u00a7fcan change the member permissions.");
        lores.add("\u00a7e\u00a7lClick here to view or change.");
        meta2.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta2);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.SAPLING, 1, (short) 3);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChange Island Biome");
        lores.add("\u00a7eCurrent Biome: \u00a7b" + this.getCurrentBiome(player).toUpperCase());
        lores.add("\u00a7fThe island biome affects things");
        lores.add("\u00a7flike grass color and spawning");
        lores.add("\u00a7fof both animals and monsters.");
        if (this.checkIslandPermission(player, "canChangeBiome")) {
            lores.add("\u00a7e\u00a7lClick here to change biomes.");
        } else {
            lores.add("\u00a7c\u00a7lYou can't change the biome.");
        }
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.IRON_FENCE, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Lock");
        if (skyBlock.getIslandConfig(player).getBoolean("general.locked")) {
            lores.add("\u00a7eLock Status: \u00a7aActive");
            lores.add("\u00a7fYour island is currently \u00a7clocked.");
            lores.add("\u00a7fPlayers outside of your group");
            lores.add("\u00a7fare unable to enter your island.");
            if (this.checkIslandPermission(player, "canToggleLock")) {
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
            if (this.checkIslandPermission(player, "canToggleLock")) {
                lores.add("\u00a7e\u00a7lClick here to lock your island.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the lock.");
            }
        }
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        if (skyBlock.getIslandConfig(player).getBoolean("general.warpActive")) {
            this.currentIslandItem = new ItemStack(Material.PORTAL, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("\u00a7a\u00a7lIsland Warp");
            lores.add("\u00a7eWarp Status: \u00a7aActive");
            lores.add("\u00a7fOther players may warp to your");
            lores.add("\u00a7fisland at anytime to the point");
            lores.add("\u00a7fyou set using \u00a7d/island setwarp.");
            if (this.checkIslandPermission(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", skyBlock.getSkyBlockWorld())) {
                lores.add("\u00a7e\u00a7lClick here to deactivate.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the warp.");
            }
        } else {
            this.currentIslandItem = new ItemStack(Material.ENDER_STONE, 1);
            meta4 = this.currentIslandItem.getItemMeta();
            meta4.setDisplayName("\u00a7a\u00a7lIsland Warp");
            lores.add("\u00a7eWarp Status: \u00a78Inactive");
            lores.add("\u00a7fOther players can't warp to your");
            lores.add("\u00a7fisland. Set a warp point using");
            lores.add("\u00a7d/island setwarp \u00a7fbefore activating.");
            if (this.checkIslandPermission(player, "canToggleWarp") && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", skyBlock.getSkyBlockWorld())) {
                lores.add("\u00a7e\u00a7lClick here to activate.");
            } else {
                lores.add("\u00a7c\u00a7lYou can't change the warp.");
            }
        }
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.CHEST, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lBuy Perks");
        lores.add("\u00a7fVisit the perk shop to buy");
        lores.add("\u00a7fspecial abilities for your");
        lores.add("\u00a7fisland and character, as well");
        lores.add("\u00a7fas titles and more.");
        lores.add("\u00a7e\u00a7lClick here to open the shop!");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.ENDER_CHEST, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lBuy Donor Perks");
        lores.add("\u00a7fThis special perk shop is");
        lores.add("\u00a7fonly available to donors!");
        if (VaultHandler.checkPerk(player.getName(), "group.donor", player.getWorld())) {
            lores.add("\u00a7e\u00a7lClick here to open the shop!");
        } else {
            lores.add("\u00a7a\u00a7lClick here to become a donor!");
        }
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.setItem(16, this.currentIslandItem);
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.BOOK_AND_QUILL, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lIsland Log");
        lores.add("\u00a7fView a log of events from");
        lores.add("\u00a7fyour island such as member,");
        lores.add("\u00a7fbiome, and warp changes.");
        lores.add("\u00a7e\u00a7lClick to view the log.");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.BED, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChange Home Location");
        lores.add("\u00a7fWhen you teleport to your");
        lores.add("\u00a7fisland you will be taken to");
        lores.add("\u00a7fthis location.");
        lores.add("\u00a7e\u00a7lClick here to change.");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.addItem(new ItemStack[]{this.currentIslandItem});
        this.lores.clear();
        this.currentIslandItem = new ItemStack(Material.HOPPER, 1);
        meta4 = this.currentIslandItem.getItemMeta();
        meta4.setDisplayName("\u00a7a\u00a7lChange Warp Location");
        lores.add("\u00a7fWhen your warp is activated,");
        lores.add("\u00a7fother players will be taken to");
        lores.add("\u00a7fthis point when they teleport");
        lores.add("\u00a7fto your island.");
        lores.add("\u00a7e\u00a7lClick here to change.");
        meta4.setLore((List) this.lores);
        this.currentIslandItem.setItemMeta(meta4);
        this.GUIisland.setItem(15, this.currentIslandItem);
        this.lores.clear();
    }

    public boolean checkIslandPermission(final Player player, final String permission) {
        return skyBlock.getIslandConfig(player).getBoolean("party.members." + player.getName() + "." + permission);
    }

    public String getCurrentBiome(final Player player) {
        return skyBlock.getIslandConfig(player).getString("general.biome");
    }

    public int showIslandLevel(final Player player) {
        return skyBlock.getIslandConfig(player).getInt("general.level");
    }

    public int showCurrentMembers(final Player player) {
        return skyBlock.getIslandConfig(player).getInt("party.currentSize");
    }

    public int showMaxMembers(final Player player) {
        return skyBlock.getIslandConfig(player).getInt("party.maxSize");
    }

    public void populateChallengeRank(final Player player, final int rankIndex, final Material mat, int location, final PlayerInfo pi) {
        int rankComplete = 0;
        ItemStack currentChallengeItem = new ItemStack(mat, 1);
        ItemMeta meta4 = currentChallengeItem.getItemMeta();
        String currentRank = Settings.challenges_ranks[rankIndex];
        meta4.setDisplayName("\u00a7e\u00a7lRank: " + currentRank);
        lores.add("\u00a7fComplete most challenges in");
        lores.add("\u00a7fthis rank to unlock the next rank.");
        meta4.setLore((List) this.lores);
        currentChallengeItem.setItemMeta(meta4);
        this.GUIchallenge.setItem(location, currentChallengeItem);
        this.lores.clear();
        final String[] challengeList = skyBlock.getChallengesFromRank(player, currentRank).split(" - ");
        for (int i = 0; i < challengeList.length; ++i) {
            String challenge = skyBlock.correctFormatting(challengeList[i]);
            String challengeName = skyBlock.stripFormatting(challenge).toLowerCase();
            try {
                if (rankIndex > 0) {
                    rankComplete = skyBlock.checkRankCompletion(player, Settings.challenges_ranks[rankIndex - 1]);
                    if (rankComplete > 0) {
                        currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                        meta4 = currentChallengeItem.getItemMeta();
                        meta4.setDisplayName("\u00a74\u00a7lLocked Challenge");
                        lores.add("\u00a77Complete " + rankComplete + " more " + Settings.challenges_ranks[rankIndex - 1] + " challenges");
                        lores.add("\u00a77to unlock this rank.");
                        meta4.setLore((List) this.lores);
                        currentChallengeItem.setItemMeta(meta4);
                        this.GUIchallenge.setItem(++location, currentChallengeItem);
                        this.lores.clear();
                        continue;
                    }
                }
                FileConfiguration config = skyBlock.getConfig();
                if (isNormalChallenge(challenge)) {
                    currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName(challenge);
                } else if (isRepeatableChallenge(challenge)) {
                    if (!config.contains("options.challenges.challengeList." + challengeName + ".displayItem")) {
                        currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
                    } else {
                        currentChallengeItem = new ItemStack(Material.getMaterial(config.getInt("options.challenges.challengeList." + challengeName + ".displayItem")), 1);
                    }
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName(challenge);
                } else if (isCompletedChallenge(challenge)) {
                    currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 13);
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName(challenge);
                } else {
                    currentChallengeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
                    meta4 = currentChallengeItem.getItemMeta();
                    meta4.setDisplayName(challenge);
                }
                lores.add("\u00a77" + config.getString("options.challenges.challengeList." + challengeName + ".description"));
                lores.add("\u00a7eThis challenge requires the following:");
                final String[] reqList = config.getString("options.challenges.challengeList." + challengeName + ".requiredItems").split(" ");
                int reqItem = 0;
                int reqAmount = 0;
                int reqMod = -1;
                String[] array;
                for (int length = (array = reqList).length, j = 0; j < length; ++j) {
                    final String s = array[j];
                    final String[] sPart = s.split(":");
                    if (sPart.length == 2) {
                        reqItem = Integer.parseInt(sPart[0]);
                        final String[] sScale = sPart[1].split(";");
                        if (sScale.length == 1) {
                            reqAmount = Integer.parseInt(sPart[1]);
                        } else if (sScale.length == 2) {
                            if (sScale[1].charAt(0) == '+') {
                                reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName);
                            } else if (sScale[1].charAt(0) == '*') {
                                reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName));
                            } else if (sScale[1].charAt(0) == '-') {
                                reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName);
                            } else if (sScale[1].charAt(0) == '/') {
                                reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName));
                            }
                        }
                    } else if (sPart.length == 3) {
                        reqItem = Integer.parseInt(sPart[0]);
                        final String[] sScale = sPart[2].split(";");
                        if (sScale.length == 1) {
                            reqAmount = Integer.parseInt(sPart[2]);
                        } else if (sScale.length == 2) {
                            if (sScale[1].charAt(0) == '+') {
                                reqAmount = Integer.parseInt(sScale[0]) + Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName);
                            } else if (sScale[1].charAt(0) == '*') {
                                reqAmount = Integer.parseInt(sScale[0]) * (Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName));
                            } else if (sScale[1].charAt(0) == '-') {
                                reqAmount = Integer.parseInt(sScale[0]) - Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName);
                            } else if (sScale[1].charAt(0) == '/') {
                                reqAmount = Integer.parseInt(sScale[0]) / (Integer.parseInt(sScale[1].substring(1)) * skyBlock.getActivePlayers().get(player.getName()).checkChallengeSinceTimer(challengeName));
                            }
                        }
                        reqMod = Integer.parseInt(sPart[1]);
                    }
                    final ItemStack newItem = new ItemStack(reqItem, reqAmount, (short) reqMod);
                    lores.add("\u00a7f" + newItem.getAmount() + " " + newItem.getType().toString());
                }
                if (pi.checkChallenge(challengeName) > 0 && config.getBoolean("options.challenges.challengeList." + challengeName + ".repeatable")) {
                    if (pi.onChallengeCooldown(challengeName)) {
                        if (pi.getChallengeCooldownTime(challengeName) / 86400000L >= 1L) {
                            final int days = (int) pi.getChallengeCooldownTime(challengeName) / 86400000;
                            lores.add("\u00a74Requirements will reset in " + days + " days.");
                        } else {
                            final int hours = (int) pi.getChallengeCooldownTime(challengeName) / 3600000;
                            lores.add("\u00a74Requirements will reset in " + hours + " hours.");
                        }
                    }
                    lores.add("\u00a76Item Reward: \u00a7a" + config.getString("options.challenges.challengeList." + challengeName + ".repeatRewardText"));
                    lores.add("\u00a76Currency Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".repeatCurrencyReward"));
                    lores.add("\u00a76Exp Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".repeatXpReward"));
                    lores.add("\u00a7dTotal times completed: \u00a7f" + pi.getChallenge(challengeName).getTimesCompleted());
                    lores.add("\u00a7e\u00a7lClick to complete this challenge.");
                } else {
                    lores.add("\u00a76Item Reward: \u00a7a" + config.getString("options.challenges.challengeList." + challengeName + ".rewardText"));
                    lores.add("\u00a76Currency Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".currencyReward"));
                    lores.add("\u00a76Exp Reward: \u00a7a" + config.getInt("options.challenges.challengeList." + challengeName + ".xpReward"));
                    if (config.getBoolean("options.challenges.challengeList." + challengeName + ".repeatable")) {
                        lores.add("\u00a7e\u00a7lClick to complete this challenge.");
                    } else {
                        lores.add("\u00a74\u00a7lYou can't repeat this challenge.");
                    }
                }
                meta4.setLore((List) this.lores);
                currentChallengeItem.setItemMeta(meta4);
                this.GUIchallenge.setItem(++location, currentChallengeItem);
                this.lores.clear();
            } catch (NullPointerException e) {
                skyBlock.getLogger().log(Level.SEVERE, "Mis-configured challenge " + challenge, e);
            }
        }
    }

    private boolean isCompletedChallenge(String challengeName) {
        return challengeName.charAt(1) == '2';
    }

    private boolean isRepeatableChallenge(String challengeName) {
        return challengeName.charAt(1) == 'a';
    }

    private boolean isNormalChallenge(String challengeName) {
        return challengeName.charAt(1) == 'e';
    }
}

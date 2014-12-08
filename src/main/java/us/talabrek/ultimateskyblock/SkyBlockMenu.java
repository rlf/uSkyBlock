package us.talabrek.ultimateskyblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.talabrek.ultimateskyblock.handler.VaultHandler;

import java.util.ArrayList;
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
    private uSkyBlock skyBlock;
    ItemStack pHead;
    ItemStack sign;
    ItemStack biome;
    ItemStack lock;
    ItemStack warpset;
    ItemStack warptoggle;
    ItemStack invite;
    ItemStack kick;

    public SkyBlockMenu(uSkyBlock skyBlock) {
        this.skyBlock = skyBlock;
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
        meta2.setLore(lores);
        biome.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{biome});
        lores.clear();
        meta2 = lock.getItemMeta();
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
        meta2.setLore(lores);
        lock.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{lock});
        lores.clear();
        meta2 = warpset.getItemMeta();
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
        meta2.setLore(lores);
        warpset.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{warpset});
        lores.clear();
        meta2 = warptoggle.getItemMeta();
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
        meta2.setLore(lores);
        warptoggle.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{warptoggle});
        lores.clear();
        meta2 = invite.getItemMeta();
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
        meta2.setLore(lores);
        invite.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{invite});
        lores.clear();
        meta2 = kick.getItemMeta();
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
        meta2.setLore(lores);
        kick.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{kick});
        lores.clear();
        return menu;
    }

    public Inventory displayPartyGUI(final Player player) {
        List<String> lores = new ArrayList<>();
        Inventory menu = Bukkit.createInventory(null, 18, "\u00a79Island Group Members");
        final Set<String> memberList = skyBlock.getIslandConfig(player).getConfigurationSection("party.members").getKeys(false);
        final SkullMeta meta3 = (SkullMeta) pHead.getItemMeta();
        final ItemMeta meta2 = sign.getItemMeta();
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
        meta2.setLore(lores);
        sign.setItemMeta(meta2);
        menu.addItem(new ItemStack[]{sign});
        lores.clear();
        for (String temp : memberList) {
            if (temp.equalsIgnoreCase(skyBlock.getIslandConfig(player).getString("party.leader"))) {
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
                meta3.setLore(lores);
                lores.clear();
            }
            meta3.setOwner(temp);
            pHead.setItemMeta(meta3);
            menu.addItem(new ItemStack[]{pHead});
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

    public void onClick(InventoryClickEvent event) {
        if (event == null || event.getCurrentItem() == null || event.getWhoClicked() == null) {
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
            if (meta.getLore().contains("\u00a7a\u00a7lLeader")) {
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
            p = (Player) event.getWhoClicked();
            String[] playerPerm = event.getInventory().getName().split(" ");
            if (event.getCurrentItem().getTypeId() == 6) {
                p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(p, playerPerm[0], "canChangeBiome");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 101) {
                p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(p, playerPerm[0], "canToggleLock");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 90) {
                p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(p, playerPerm[0], "canChangeWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 69) {
                p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(p, playerPerm[0], "canToggleWarp");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 398) {
                p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(p, playerPerm[0], "canInviteOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 301) {
                p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(p, playerPerm[0], "canKickOthers");
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
            } else if (event.getCurrentItem().getTypeId() == 323) {
                p.closeInventory();
                p.openInventory(displayPartyGUI(p));
            } else {
                p.closeInventory();
                p.openInventory(displayPartyPlayerGUI(p, playerPerm[0]));
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

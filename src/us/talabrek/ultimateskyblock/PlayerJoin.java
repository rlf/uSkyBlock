package us.talabrek.ultimateskyblock;

import org.bukkit.inventory.meta.*;
import java.io.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.event.inventory.*;

public class PlayerJoin implements Listener
{
    private Player hungerman;
    private SkullMeta meta;
    int randomNum;
    Player p;
    String[] playerPerm;
    
    public PlayerJoin() {
        super();
        this.hungerman = null;
        this.meta = null;
        this.randomNum = 0;
        this.p = null;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final File f = new File(uSkyBlock.getInstance().directoryPlayers, event.getPlayer().getName());
        final PlayerInfo pi = new PlayerInfo(event.getPlayer().getName());
        if (f.exists()) {
            final PlayerInfo pi2 = uSkyBlock.getInstance().readPlayerFile(event.getPlayer().getName());
            if (pi2 != null) {
                pi.setIslandLocation(pi2.getIslandLocation());
                pi.setHomeLocation(pi2.getHomeLocation());
                pi.setHasIsland(pi2.getHasIsland());
                if (uSkyBlock.getInstance().getIslandConfig(pi.locationForParty()) == null) {
                    uSkyBlock.getInstance().createIslandConfig(pi.locationForParty(), event.getPlayer().getName());
                }
                uSkyBlock.getInstance().clearIslandConfig(pi.locationForParty(), event.getPlayer().getName());
                if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                    WorldGuardHandler.protectIsland(event.getPlayer(), event.getPlayer().getName(), pi);
                }
            }
            f.delete();
        }
        uSkyBlock.getInstance().addActivePlayer(event.getPlayer().getName(), pi);
        if (pi.getHasIsland() && !uSkyBlock.getInstance().getTempIslandConfig(pi.locationForParty()).contains("general.level")) {
            uSkyBlock.getInstance().createIslandConfig(pi.locationForParty(), event.getPlayer().getName());
            System.out.println("Creating new Config File");
        }
        uSkyBlock.getInstance().getIslandConfig(pi.locationForParty());
        System.out.print("Loaded player file for " + event.getPlayer().getName());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (uSkyBlock.getInstance().hasIsland(event.getPlayer().getName()) && !uSkyBlock.getInstance().checkForOnlineMembers(event.getPlayer())) {
            System.out.print("Removing island config from memory.");
            uSkyBlock.getInstance().removeIslandConfig(uSkyBlock.getInstance().getActivePlayers().get(event.getPlayer().getName()).locationForParty());
        }
        uSkyBlock.getInstance().removeActivePlayer(event.getPlayer().getName());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFoodChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            this.hungerman = (Player)event.getEntity();
            if (this.hungerman.getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && this.hungerman.getFoodLevel() > event.getFoodLevel() && uSkyBlock.getInstance().playerIsOnIsland(this.hungerman)) {
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger4", this.hungerman.getWorld())) {
                    event.setCancelled(true);
                    return;
                }
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger3", this.hungerman.getWorld())) {
                    this.randomNum = 1 + (int)(Math.random() * 100.0);
                    if (this.randomNum <= 75) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger2", this.hungerman.getWorld())) {
                    this.randomNum = 1 + (int)(Math.random() * 100.0);
                    if (this.randomNum <= 50) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger", this.hungerman.getWorld())) {
                    this.randomNum = 1 + (int)(Math.random() * 100.0);
                    if (this.randomNum <= 25) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (Settings.extras_obsidianToLava && uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && event.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getTypeId() == 325 && event.getClickedBlock().getType() == Material.OBSIDIAN && !uSkyBlock.getInstance().testForObsidian(event.getClickedBlock())) {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Changing your obsidian back into lava. Be careful!");
            event.getClickedBlock().setType(Material.AIR);
            event.getPlayer().getInventory().removeItem(new ItemStack[] { new ItemStack(325, 1) });
            event.getPlayer().getInventory().addItem(new ItemStack[] { new ItemStack(327, 1) });
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && event.getEntity().getType().equals((Object)EntityType.ITEM_FRAME)) {
            for (final Entity temp : event.getEntity().getNearbyEntities(3.0, 3.0, 3.0)) {
                if (temp instanceof Player) {
                    final Player p = (Player)temp;
                    if (!uSkyBlock.getInstance().locationIsOnIsland(p, event.getEntity().getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                    continue;
                }
                else {
                    if (temp instanceof Arrow) {
                        event.setCancelled(true);
                        return;
                    }
                    if (temp instanceof Snowball) {
                        event.setCancelled(true);
                        return;
                    }
                    if (temp instanceof SmallFireball) {
                        event.setCancelled(true);
                        return;
                    }
                    if (temp instanceof Creeper) {
                        event.setCancelled(true);
                        return;
                    }
                    if (temp instanceof Fireball) {
                        event.setCancelled(true);
                        return;
                    }
                    continue;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void guiClick(final InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("\u00a79Island Group Members")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (event.getCurrentItem().getTypeId() == 397) {
                this.meta = (SkullMeta)event.getCurrentItem().getItemMeta();
            }
            this.p = (Player)event.getWhoClicked();
            if (this.meta == null || event.getCurrentItem().getType() == Material.SIGN) {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (this.meta.getLore().contains("\u00a7a\u00a7lLeader")) {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(this.p));
            }
            else if (!uSkyBlock.getInstance().isPartyLeader(this.p)) {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(this.p));
            }
            else {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.meta.getOwner()));
                this.meta = null;
            }
        }
        else if (event.getInventory().getName().contains("Permissions")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            this.p = (Player)event.getWhoClicked();
            this.playerPerm = event.getInventory().getName().split(" ");
            if (event.getCurrentItem().getTypeId() == 6) {
                this.p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(this.p, this.playerPerm[0], "canChangeBiome");
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
            else if (event.getCurrentItem().getTypeId() == 101) {
                this.p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(this.p, this.playerPerm[0], "canToggleLock");
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
            else if (event.getCurrentItem().getTypeId() == 90) {
                this.p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(this.p, this.playerPerm[0], "canChangeWarp");
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
            else if (event.getCurrentItem().getTypeId() == 69) {
                this.p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(this.p, this.playerPerm[0], "canToggleWarp");
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
            else if (event.getCurrentItem().getTypeId() == 398) {
                this.p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(this.p, this.playerPerm[0], "canInviteOthers");
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
            else if (event.getCurrentItem().getTypeId() == 301) {
                this.p.closeInventory();
                uSkyBlock.getInstance().changePlayerPermission(this.p, this.playerPerm[0], "canKickOthers");
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
            else if (event.getCurrentItem().getTypeId() == 323) {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(this.p));
            }
            else {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.playerPerm[0]));
            }
        }
        else if (event.getInventory().getName().contains("Island Biome")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            this.p = (Player)event.getWhoClicked();
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                this.p.closeInventory();
                this.p.performCommand("island biome jungle");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 1) {
                this.p.closeInventory();
                this.p.performCommand("island biome forest");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.SAND) {
                this.p.closeInventory();
                this.p.performCommand("island biome desert");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.SNOW) {
                this.p.closeInventory();
                this.p.performCommand("island biome taiga");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.EYE_OF_ENDER) {
                this.p.closeInventory();
                this.p.performCommand("island biome sky");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.WATER_LILY) {
                this.p.closeInventory();
                this.p.performCommand("island biome swampland");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.FIRE) {
                this.p.closeInventory();
                this.p.performCommand("island biome hell");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.RED_MUSHROOM) {
                this.p.closeInventory();
                this.p.performCommand("island biome mushroom");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.WATER) {
                this.p.closeInventory();
                this.p.performCommand("island biome ocean");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
        }
        else if (event.getInventory().getName().contains("Challenge Menu")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            if (event.getCurrentItem().getType() != Material.DIRT && event.getCurrentItem().getType() != Material.IRON_BLOCK && event.getCurrentItem().getType() != Material.GOLD_BLOCK && event.getCurrentItem().getType() != Material.DIAMOND_BLOCK) {
                (this.p = (Player)event.getWhoClicked()).closeInventory();
                if (event.getCurrentItem().getItemMeta() != null) {
                    this.p.performCommand("c c " + event.getCurrentItem().getItemMeta().getDisplayName().replace("\u00a7e", "").replace("\u00a78", "").replace("\u00a7a", "").replace("\u00a72", "").replace("\u00a7l", ""));
                }
                this.p.openInventory(uSkyBlock.getInstance().displayChallengeGUI(this.p));
            }
            else {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
        }
        else if (event.getInventory().getName().contains("Island Log")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            this.p.closeInventory();
            this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
        }
        else if (event.getInventory().getName().contains("Island Menu")) {
            event.setCancelled(true);
            if (event.getSlot() < 0 || event.getSlot() > 35) {
                return;
            }
            this.p = (Player)event.getWhoClicked();
            if (event.getCurrentItem().getType() == Material.SAPLING && event.getCurrentItem().getDurability() == 3) {
                this.p.closeInventory();
                this.p.performCommand("island biome");
            }
            else if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                this.p.closeInventory();
                this.p.performCommand("island party");
            }
            else if (event.getCurrentItem().getType() == Material.BED) {
                this.p.closeInventory();
                this.p.performCommand("island sethome");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.HOPPER) {
                this.p.closeInventory();
                this.p.performCommand("island setwarp");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL) {
                this.p.closeInventory();
                this.p.performCommand("island log");
            }
            else if (event.getCurrentItem().getType() == Material.ENDER_PORTAL) {
                this.p.closeInventory();
                this.p.performCommand("island home");
            }
            else if (event.getCurrentItem().getType() == Material.GRASS) {
                this.p.closeInventory();
                this.p.performCommand("island create");
            }
            else if (event.getCurrentItem().getType() == Material.CHEST) {
                this.p.closeInventory();
                this.p.performCommand("chc open perks");
            }
            else if (event.getCurrentItem().getType() == Material.ENDER_CHEST) {
                this.p.closeInventory();
                if (VaultHandler.checkPerk(this.p.getName(), "group.donor", this.p.getWorld())) {
                    this.p.performCommand("chc open donor");
                }
                else {
                    this.p.performCommand("donate");
                }
            }
            else if (event.getCurrentItem().getType() == Material.EXP_BOTTLE) {
                this.p.closeInventory();
                this.p.performCommand("island level");
            }
            else if (event.getCurrentItem().getType() == Material.DIAMOND_ORE) {
                this.p.closeInventory();
                this.p.performCommand("c");
            }
            else if (event.getCurrentItem().getType() == Material.ENDER_STONE || event.getCurrentItem().getType() == Material.PORTAL) {
                this.p.closeInventory();
                this.p.performCommand("island togglewarp");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.IRON_FENCE && uSkyBlock.getInstance().getIslandConfig(uSkyBlock.getInstance().getActivePlayers().get(event.getWhoClicked().getName()).locationForParty()).getBoolean("general.locked")) {
                this.p.closeInventory();
                this.p.performCommand("island unlock");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else if (event.getCurrentItem().getType() == Material.IRON_FENCE && !uSkyBlock.getInstance().getIslandConfig(uSkyBlock.getInstance().getActivePlayers().get(event.getWhoClicked().getName()).locationForParty()).getBoolean("general.locked")) {
                this.p.closeInventory();
                this.p.performCommand("island lock");
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
            else {
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayIslandGUI(this.p));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("\u00a79SB Island Group Members")) {
            event.setCancelled(true);
            this.meta = (SkullMeta)event.getCursor().getItemMeta();
            this.p = (Player)event.getWhoClicked();
            if (this.meta.getOwner() == null) {
                this.p.updateInventory();
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyGUI(this.p));
            }
            else {
                this.p.updateInventory();
                this.p.closeInventory();
                this.p.openInventory(uSkyBlock.getInstance().displayPartyPlayerGUI(this.p, this.meta.getOwner()));
            }
        }
    }
}

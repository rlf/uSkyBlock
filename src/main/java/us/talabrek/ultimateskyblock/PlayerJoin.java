package us.talabrek.ultimateskyblock;

import java.io.*;

import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;

import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;

public class PlayerJoin implements Listener {
    private Player hungerman;
    int randomNum;

    public PlayerJoin() {
        super();
        this.hungerman = null;
        this.randomNum = 0;
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
            this.hungerman = (Player) event.getEntity();
            if (this.hungerman.getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && this.hungerman.getFoodLevel() > event.getFoodLevel() && uSkyBlock.getInstance().playerIsOnIsland(this.hungerman)) {
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger4", this.hungerman.getWorld())) {
                    event.setCancelled(true);
                    return;
                }
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger3", this.hungerman.getWorld())) {
                    this.randomNum = 1 + (int) (Math.random() * 100.0);
                    if (this.randomNum <= 75) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger2", this.hungerman.getWorld())) {
                    this.randomNum = 1 + (int) (Math.random() * 100.0);
                    if (this.randomNum <= 50) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(this.hungerman.getName(), "usb.extra.hunger", this.hungerman.getWorld())) {
                    this.randomNum = 1 + (int) (Math.random() * 100.0);
                    if (this.randomNum <= 25) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (Settings.extras_obsidianToLava && uSkyBlock.getInstance().playerIsOnIsland(event.getPlayer()) && event.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getTypeId() == 325 && event.getClickedBlock().getType() == Material.OBSIDIAN && !uSkyBlock.getInstance().testForObsidian(event.getClickedBlock())) {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Changing your obsidian back into lava. Be careful!");
            event.getClickedBlock().setType(Material.AIR);
            event.getPlayer().getInventory().removeItem(new ItemStack[]{new ItemStack(325, 1)});
            event.getPlayer().getInventory().addItem(new ItemStack[]{new ItemStack(327, 1)});
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase(Settings.general_worldName) && event.getEntity().getType().equals(EntityType.ITEM_FRAME)) {
            for (final Entity temp : event.getEntity().getNearbyEntities(3.0, 3.0, 3.0)) {
                if (temp instanceof Player) {
                    final Player p = (Player) temp;
                    if (!uSkyBlock.getInstance().locationIsOnIsland(p, event.getEntity().getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                    continue;
                } else {
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
}

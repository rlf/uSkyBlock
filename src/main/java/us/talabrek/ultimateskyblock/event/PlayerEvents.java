package us.talabrek.ultimateskyblock.event;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.*;
import us.talabrek.ultimateskyblock.handler.VaultHandler;

import java.util.*;

public class PlayerEvents implements Listener {
    private static final Random RANDOM = new Random();
    
    private final uSkyBlock plugin;

    public PlayerEvents(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (plugin.isSkyWorld(event.getPlayer().getWorld())) {
            plugin.loadPlayerData(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        plugin.unloadPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFoodChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && plugin.isSkyWorld(event.getEntity().getWorld())) {
            Player hungerman = (Player) event.getEntity();
            float randomNum = RANDOM.nextFloat();
            if (plugin.isSkyWorld(hungerman.getWorld()) && hungerman.getFoodLevel() > event.getFoodLevel() && plugin.playerIsOnIsland(hungerman)) {
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger4", hungerman.getWorld())) {
                    event.setCancelled(true);
                    return;
                }
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger3", hungerman.getWorld())) {
                    if (randomNum <= 0.75f) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger2", hungerman.getWorld())) {
                    if (randomNum <= 0.50f) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (VaultHandler.checkPerk(hungerman.getName(), "usb.extra.hunger", hungerman.getWorld())) {
                    if (randomNum <= 0.25f) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDropEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        addDropInfo(player, event.getItemDrop().getItemStack());
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        // Take over the drop, since Bukkit don't do this in a Metadatable format.
        if (!event.getKeepInventory()) {
            for (ItemStack stack : event.getDrops()) {
                addDropInfo(player, stack);
            }
        }
    }

    private void addDropInfo(Player player, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add("Dropped by: " + player.getName());
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    private void clearDropInfo(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            if (lore.get(lore.size()-1).startsWith("Dropped by: ")) {
                lore.remove(lore.size()-1);
            }
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onPickupInventoryEvent(InventoryPickupItemEvent event) {
        if (!plugin.isSkyWorld(event.getItem().getWorld())) {
            return;
        }
        // I.e. hoppers...
        clearDropInfo(event.getItem().getItemStack());
    }

    @EventHandler
    public void onPickupEvent(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        if (wasDroppedBy(player, event)) {
            clearDropInfo(event.getItem().getItemStack());
            return; // Allowed
        }
        if (plugin.playerIsOnIsland(player) || plugin.playerIsInSpawn(player)) {
            clearDropInfo(event.getItem().getItemStack());
            return;
        }
        // You are on anothers island, and the stuff dropped weren't yours.
        event.setCancelled(true);
        plugin.notifyPlayer(player, "You can only trade in spawn");
    }

    private boolean wasDroppedBy(Player player, PlayerPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            String lastLine = lore.get(lore.size()-1);
            return lastLine.equalsIgnoreCase("Dropped by: " + player.getName());
        }
        return false;
    }

    @EventHandler
    public void onShearEvent(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return; // Not our concern
        }
        if (!plugin.playerIsOnIsland(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.isSkyWorld(event.getDamager().getWorld())) {
            return;
        }
        if (event.getDamager() instanceof Player
                && event.getEntity() instanceof Creature
                && !plugin.playerIsOnIsland((Player)event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (Settings.extras_obsidianToLava && plugin.playerIsOnIsland(player)
                && plugin.isSkyWorld(player.getWorld())
                && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && player.getItemInHand().getType() == Material.BUCKET
                && event.getClickedBlock().getType() == Material.OBSIDIAN
                && !plugin.testForObsidian(event.getClickedBlock())) {
            player.sendMessage("\u00a7eChanging your obsidian back into lava. Be careful!");
            event.getClickedBlock().setType(Material.AIR);
            player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.BUCKET, 1)});
            player.getInventory().addItem(new ItemStack[]{new ItemStack(Material.LAVA_BUCKET, 1)});
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void guiClick(final InventoryClickEvent event) {
        plugin.getMenu().onClick(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(final InventoryDragEvent event) {
        plugin.getMenu().onDrag(event);
    }
}

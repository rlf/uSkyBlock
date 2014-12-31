package us.talabrek.ultimateskyblock.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the internal item-drop protection.
 */
public class ItemDropEvents implements Listener {
    private final uSkyBlock plugin;

    public ItemDropEvents(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDropEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        addDropInfo(player, event.getItemDrop().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR)
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickupInventoryEvent(InventoryPickupItemEvent event) {
        if (!plugin.isSkyWorld(event.getItem().getWorld())) {
            return;
        }
        // I.e. hoppers...
        clearDropInfo(event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickupEvent(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        if (wasDroppedBy(player, event)) {
            clearDropInfo(event.getItem().getItemStack());
            return; // Allowed
        }
        if (player.hasPermission("usb.mod.bypassprotection")) {
            clearDropInfo(event.getItem().getItemStack());
            return;
        }
        if (plugin.playerIsOnIsland(player) || plugin.playerIsInSpawn(player)) {
            clearDropInfo(event.getItem().getItemStack());
            return;
        }
        // You are on another's island, and the stuff dropped weren't yours.
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
}

package us.talabrek.ultimateskyblock.event;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Handles the internal item-drop protection.
 */
public class ItemDropEvents implements Listener {
    private final uSkyBlock plugin;
    private final boolean visitorsCanDrop;

    public ItemDropEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        visitorsCanDrop = plugin.getConfig().getBoolean("options.protection.visitors.item-drops", true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onDropEvent(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        if (!visitorsCanDrop && !plugin.playerIsOnIsland(player) && !plugin.playerIsInSpawn(player)) {
            event.setCancelled(true);
            plugin.notifyPlayer(player, tr("\u00a7eVisitors can't drop items!"));
            return;
        }
        addDropInfo(player, event.getItemDrop().getItemStack());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.isSkyWorld(player.getWorld())) {
            return;
        }
        if (!visitorsCanDrop && !plugin.playerIsOnIsland(player) && !plugin.playerIsInSpawn(player)) {
            event.setKeepInventory(true);
            return;
        }
        // Take over the drop, since Bukkit don't do this in a Metadatable format.
        for (ItemStack stack : event.getDrops()) {
            addDropInfo(player, stack);
        }
    }

    private void addDropInfo(Player player, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            String ownerTag = tr("Owner: {0}", player.getName());
            if (!lore.contains(ownerTag)) {
                lore.add(ownerTag);
            }
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
    }

    private void clearDropInfo(Item item) {
        ItemStack stack = item.getItemStack();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                lore.removeIf(line -> line.contains(tr("Owner: {0}", "")));
                meta.setLore(lore);
                stack.setItemMeta(meta);
                item.setItemStack(stack);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onPickupInventoryEvent(InventoryPickupItemEvent event) {
        if (!plugin.isSkyWorld(event.getItem().getWorld())) {
            return;
        }
        // I.e. hoppers...
        clearDropInfo(event.getItem());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onPickupEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (event.isCancelled() || !plugin.isSkyWorld(player.getWorld())) {
            clearDropInfo(event.getItem());
            return;
        }
        if (wasDroppedBy(player, event) || player.hasPermission("usb.mod.bypassprotection") || plugin.playerIsOnIsland(player) || plugin.playerIsInSpawn(player)) {
            clearDropInfo(event.getItem());
            return; // Allowed
        }
        // You are on another's island, and the stuff dropped weren't yours.
        event.setCancelled(true);
        plugin.notifyPlayer(player, tr("You cannot pick up other players' loot when you are a visitor!"));
    }

    private boolean wasDroppedBy(Player player, EntityPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                String lastLine = lore.get(lore.size() - 1);
                return lastLine.equalsIgnoreCase(tr("Owner: {0}", player.getName()));
            }
        }
        return false;
    }
}

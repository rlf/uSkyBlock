package us.talabrek.ultimateskyblock.event;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.*;

import java.util.Random;
import java.util.logging.Level;

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
        if (event.getEntity() instanceof Player) {
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (Settings.extras_obsidianToLava && plugin.playerIsOnIsland(player)
                && plugin.isSkyWorld(player.getWorld())
                && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && player.getItemInHand().getType() == Material.BUCKET
                && event.getClickedBlock().getType() == Material.OBSIDIAN
                && !plugin.testForObsidian(event.getClickedBlock())) {
            player.sendMessage(ChatColor.YELLOW + "Changing your obsidian back into lava. Be careful!");
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

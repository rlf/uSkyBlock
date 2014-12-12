package us.talabrek.ultimateskyblock;

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
        loadPlayerData(event.getPlayer());
    }

    private void loadPlayerData(Player player) {
        final PlayerInfo pi = loadPlayerAndIsland(player.getName());
        WorldGuardHandler.protectIsland(player, player.getName(), pi);
        plugin.addActivePlayer(player.getName(), pi);
        uSkyBlock.log(Level.INFO, "Loaded player file for " + player.getName());
    }

    private PlayerInfo loadPlayerAndIsland(String playerName) {
        final PlayerInfo pi = new PlayerInfo(playerName);
        FileConfiguration islandConfig = plugin.getIslandConfig(pi.locationForParty());
        if (islandConfig == null || !islandConfig.contains("general.level")) {
            uSkyBlock.log(Level.INFO, "Creating new Island-config File");
            plugin.createIslandConfig(pi.locationForParty(), playerName);
        }
        plugin.clearIslandConfig(pi.locationForParty(), playerName);
        return pi;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.isSkyWorld(player.getWorld())) {
            unloadPlayerData(player);
        }
    }

    private void unloadPlayerData(Player player) {
        if (plugin.hasIsland(player.getName()) && !plugin.checkForOnlineMembers(player)) {
            plugin.removeIslandConfig(plugin.getActivePlayers().get(player.getName()).locationForParty());
        }
        plugin.removeActivePlayer(player.getName());
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

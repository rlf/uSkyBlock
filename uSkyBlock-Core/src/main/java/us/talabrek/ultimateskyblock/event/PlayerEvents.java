package us.talabrek.ultimateskyblock.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.ChatColor;

import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class PlayerEvents implements Listener {
    private static final String CN = PlayerEvents.class.getName();
    private static final Set<EntityDamageEvent.DamageCause> FIRE_TRAP = new HashSet<>(
            Arrays.asList(EntityDamageEvent.DamageCause.LAVA, EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK));
    private static final Random RANDOM = new Random();
    private static final int OBSIDIAN_SPAM = 10000; // Max once every 10 seconds.

    private final uSkyBlock plugin;
    private final boolean visitorFallProtected;
    private final boolean visitorFireProtected;
    private final boolean protectLava;
    private final Map<UUID, Long> obsidianClick = new WeakHashMap<>();
    private final int hopperlimit;

    public PlayerEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        visitorFallProtected = plugin.getConfig().getBoolean("options.protection.visitors.fall", true);
        visitorFireProtected = plugin.getConfig().getBoolean("options.protection.visitors.fire-damage", true);
        protectLava = plugin.getConfig().getBoolean("options.protection.protect-lava", true);
        hopperlimit = plugin.getConfig().getInt("options.island.hopperlimit", 10);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFoodChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && plugin.isSkyWorld(event.getEntity().getWorld())) {
            Player hungerman = (Player) event.getEntity();
            float randomNum = RANDOM.nextFloat();
            if (plugin.isSkyWorld(hungerman.getWorld()) && hungerman.getFoodLevel() > event.getFoodLevel() && plugin.playerIsOnIsland(hungerman)) {
                Perk perk = plugin.getPerkLogic().getPerk(hungerman);
                if (randomNum <= perk.getHungerReduction()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickOnObsidian(final PlayerInteractEvent event) {
        if (!plugin.isSkyWorld(event.getPlayer().getWorld())) {
            return;
        }
        long now = System.currentTimeMillis();
        Player player = event.getPlayer();
        PlayerInventory inventory = player != null ? player.getInventory() : null;
        Block block = event.getClickedBlock();
        Long lastClick = obsidianClick.get(player.getUniqueId());
        if (lastClick != null && (lastClick + OBSIDIAN_SPAM) >= now) {
            plugin.notifyPlayer(player, tr("\u00a74You can only convert obsidian once every 10 seconds"));
            return;
        }
        if (Settings.extras_obsidianToLava && plugin.playerIsOnIsland(player)
                && plugin.isSkyWorld(player.getWorld())
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && player.getItemInHand() != null
                && player.getItemInHand().getType() == Material.BUCKET
                && block != null
                && block.getType() == Material.OBSIDIAN
                && !testForObsidian(block)) {
            if (inventory.firstEmpty() != -1) {
                obsidianClick.put(player.getUniqueId(), now);
                player.sendMessage(tr("\u00a7eChanging your obsidian back into lava. Be careful!"));
                inventory.removeItem(new ItemStack(Material.BUCKET, 1));
                inventory.addItem(new ItemStack(Material.LAVA_BUCKET, 1));
                player.updateInventory();
                block.setType(Material.AIR);
                event.setCancelled(true); // Don't execute the click anymore (since that would re-place the lava).
            } else {
                player.sendMessage(tr("\u00a7eYour inventory must have another empty space!"));
            }
        }
    }

    /**
     * Tests for more than one obsidian close by.
     */
    public boolean testForObsidian(final Block block) {
        for (int x = -3; x <= 3; ++x) {
            for (int y = -3; y <= 3; ++y) {
                for (int z = -3; z <= 3; ++z) {
                    final Block testBlock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
                    if ((x != 0 || y != 0 || z != 0) && testBlock.getType() == Material.OBSIDIAN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onLavaReplace(BlockPlaceEvent event) {
        if (!protectLava || event.getPlayer() == null || !plugin.isSkyWorld(event.getPlayer().getWorld())) {
            return; // Skip
        }
        if (event.getBlockReplacedState() != null &&
                isLavaSource(event.getBlockReplacedState().getType(), event.getBlockReplacedState().getRawData())) {
            plugin.notifyPlayer(event.getPlayer(), tr("\u00a74It's a bad idea to replace your lava!"));
            event.setCancelled(true);
        }
    }

    private boolean isLavaSource(Material type, byte data) {
        return (type == Material.STATIONARY_LAVA || type == Material.LAVA) && data == 0;
    }

    @EventHandler
    public void onLavaAbsorption(EntityChangeBlockEvent event) {
        if (!plugin.isSkyWorld(event.getBlock().getWorld())) {
            return;
        }
        if (isLavaSource(event.getBlock().getType(), event.getBlock().getData())) {
            if (event.getTo() != Material.LAVA && event.getTo() != Material.STATIONARY_LAVA) {
                event.setCancelled(true);
                ItemStack item = new ItemStack(event.getTo(), 1, event.getData());
                Location above = event.getBlock().getLocation().add(0, 1, 0);
                event.getBlock().getWorld().dropItemNaturally(above, item);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVisitorDamage(final EntityDamageEvent event) {
        if (!plugin.isSkyWorld(event.getEntity().getWorld())) {
            return;
        }
        // Only protect visitors against damage, if pvp is disabled
        if (!Settings.island_allowPvP
                && ((visitorFireProtected && FIRE_TRAP.contains(event.getCause()))
                || (visitorFallProtected && (event.getCause() == EntityDamageEvent.DamageCause.FALL)))
                && event.getEntity() instanceof Player
                && !plugin.playerIsOnIsland((Player) event.getEntity())) {
            event.setDamage(-event.getDamage());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMemberDamage(final EntityDamageByEntityEvent event) {
        if (!plugin.isSkyWorld(event.getEntity().getWorld())) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player p2 = (Player) event.getEntity();
        if (event.getDamager() instanceof Player) {
            Player p1 = (Player) event.getDamager();
            cancelMemberDamage(p1, p2, event);
        } else if (event.getDamager() instanceof Projectile
                && !(event.getDamager() instanceof EnderPearl)) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Player) {
                Player p1 = (Player) shooter;
                cancelMemberDamage(p1, p2, event);
            }
        }
    }

    private void cancelMemberDamage(Player p1, Player p2, EntityDamageByEntityEvent event) {
        IslandInfo is1 = plugin.getIslandInfo(p1);
        IslandInfo is2 = plugin.getIslandInfo(p2);
        if (is1 != null && is2 != null && is1.getName().equals(is2.getName())) {
            plugin.notifyPlayer(p1, tr("\u00a7eYou cannot hurt island-members."));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (Settings.extras_sendToSpawn) {
            return;
        }
        if (plugin.isSkyWorld(event.getPlayer().getWorld())) {
            event.setRespawnLocation(plugin.getSkyBlockWorld().getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled() || !plugin.isSkyWorld(event.getTo().getWorld())) {
            return;
        }
        final Player player = event.getPlayer();
        boolean isAdmin = player.isOp() || hasPermission(player, "usb.mod.bypassprotection");
        IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(WorldGuardHandler.getIslandNameAt(event.getTo()));
        if (!isAdmin && islandInfo != null && islandInfo.isBanned(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(tr("\u00a74That player has forbidden you from teleporting to their island."));
        }
        if (!isAdmin && islandInfo != null && islandInfo.isLocked() && !islandInfo.getMembers().contains(player.getName()) && !islandInfo.getTrustees().contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(tr("\u00a74That island is \u00a7clocked.\u00a7e No teleporting to the island."));
        }
        if (!event.isCancelled()) {
            final PlayerInfo playerInfo = plugin.getPlayerInfo(player);
            playerInfo.onTeleport(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeafBreak(BlockBreakEvent event) {
        if (event == null || event.isCancelled() || event.getPlayer() == null || !plugin.isSkyWorld(event.getPlayer().getWorld())) {
            return;
        }
        if (event.getBlock().getType() != Material.LEAVES || (event.getBlock().getData() & 0x3) != 0) {
            return;
        }
        // Ok, a player broke an OAK LEAF in the Skyworld
        String islandName = WorldGuardHandler.getIslandNameAt(event.getPlayer().getLocation());
        IslandInfo islandInfo = plugin.getIslandInfo(islandName);
        if (islandInfo != null && islandInfo.getLeafBreaks() == 0) {
            // Add an oak-sapling
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SAPLING, 1));
            islandInfo.setLeafBreaks(islandInfo.getLeafBreaks() + 1);
        }
    }
    
    @EventHandler
    public void onHopperPlace(BlockPlaceEvent event) 
    {
        if (Material.HOPPER.equals(event.getBlock().getType()))
        {
            IslandInfo isInfo = plugin.getIslandInfo(event.getBlock().getLocation());
            if(isInfo.getHopperCount() > hopperlimit)
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(tr("\u00a74You've hit the hopper limit! You can't have more hoppers!"));
            }
            else
            {
                isInfo.setHopperCount(isInfo.getHopperCount() + 1);
            }
        }
    }
    
    @EventHandler
    public void onHopperDestroy(BlockBreakEvent event){
    	if (Material.HOPPER.equals(event.getBlock().getType())){
    		IslandInfo isInfo = plugin.getIslandInfo(event.getBlock().getLocation());
    		isInfo.setHopperCount(isInfo.getHopperCount() - 1);
    	}
    }
}

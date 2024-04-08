package us.talabrek.ultimateskyblock.event;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.api.event.IslandInfoEvent;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.BlockLimitLogic;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PatienceTester;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.world.WorldManager;

import java.util.*;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class PlayerEvents implements Listener {
    private static final Set<EntityDamageEvent.DamageCause> FIRE_TRAP = new HashSet<>(Arrays.asList(
        EntityDamageEvent.DamageCause.LAVA,
        EntityDamageEvent.DamageCause.FIRE,
        EntityDamageEvent.DamageCause.FIRE_TICK,
        EntityDamageEvent.DamageCause.HOT_FLOOR));
    private static final Random RANDOM = new Random();
    private static final int OBSIDIAN_SPAM = 10000; // Max once every 10 seconds.

    private final uSkyBlock plugin;
    private final boolean visitorFallProtected;
    private final boolean visitorFireProtected;
    private final boolean visitorMonsterProtected;
    private final boolean protectLava;
    private final Map<UUID, Long> obsidianClick = new WeakHashMap<>();
    private final boolean blockLimitsEnabled;
    private final Map<Material, Material> leafSaplings = Map.of(
        Material.OAK_LEAVES, Material.OAK_SAPLING,
        Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING,
        Material.BIRCH_LEAVES, Material.BIRCH_SAPLING,
        Material.ACACIA_LEAVES, Material.ACACIA_SAPLING,
        Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING,
        Material.DARK_OAK_LEAVES, Material.DARK_OAK_SAPLING);

    public PlayerEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        visitorFallProtected = config.getBoolean("options.protection.visitors.fall", true);
        visitorFireProtected = config.getBoolean("options.protection.visitors.fire-damage", true);
        visitorMonsterProtected = config.getBoolean("options.protection.visitors.monster-damage", false);
        protectLava = config.getBoolean("options.protection.protect-lava", true);
        blockLimitsEnabled = config.getBoolean("options.island.block-limits.enabled", false);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFoodChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getFoodLevel() > event.getFoodLevel() && plugin.playerIsOnIsland(player)) {
                if (RANDOM.nextFloat() <= plugin.getPerkLogic().getPerk(player).getHungerReduction()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClickOnObsidian(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (plugin.playerIsOnIsland(player)
            && Settings.extras_obsidianToLava
            && event.hasBlock()
            && event.hasItem()
            && event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getMaterial() == Material.BUCKET
            && block != null
            && block.getType() == Material.OBSIDIAN
            && !testForObsidian(block)) {
            long now = System.currentTimeMillis();
            Long lastClick = obsidianClick.get(player.getUniqueId());
            if (lastClick != null && (lastClick + OBSIDIAN_SPAM) >= now) {
                plugin.notifyPlayer(player, tr("\u00a74You can only convert obsidian once every 10 seconds"));
                return;
            }
            PlayerInventory inventory = player.getInventory();
            if (inventory.firstEmpty() != -1) {
                HashMap<Integer, ItemStack> leftover = inventory.removeItem(new ItemStack(Material.BUCKET));
                if (leftover.isEmpty()) {
                    obsidianClick.put(player.getUniqueId(), now);
                    player.sendMessage(tr("\u00a7eChanging your obsidian back into lava. Be careful!"));
                    leftover = inventory.addItem(new ItemStack(Material.LAVA_BUCKET));
                    // Just in case, drop the item if their inventory somehow filled before we could add it
                    if (!leftover.isEmpty()) {
                        player.getWorld().dropItem(block.getLocation(), new ItemStack(Material.LAVA_BUCKET));
                    }
                    block.setType(Material.AIR);
                    event.setCancelled(true);
                }
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

    // Prevent re-placing lava that was picked up in the last tick
    @EventHandler(ignoreCancelled = true)
    public void onLavaPlace(final PlayerBucketEmptyEvent event) {
        if (Settings.extras_obsidianToLava && event.getBucket() == Material.LAVA_BUCKET) {
            long now = System.currentTimeMillis();
            Long lastClick = obsidianClick.get(event.getPlayer().getUniqueId());
            if (lastClick != null && (now - lastClick < 50)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLavaReplace(BlockPlaceEvent event) {
        if (!protectLava || !plugin.getWorldManager().isSkyWorld(event.getPlayer().getWorld())) {
            return;
        }
        if (isLavaSource(event.getBlockReplacedState().getBlockData())) {
            plugin.notifyPlayer(event.getPlayer(), tr("\u00a74It''s a bad idea to replace your lava!"));
            event.setCancelled(true);
        }
    }

    private boolean isLavaSource(BlockData blockData) {
        return (blockData.getMaterial() == Material.LAVA
            && blockData instanceof Levelled level
            && level.getLevel() == 0);
    }

    // If an entity, such as an Enderman, attempts to replace a lava source block then cancel it and drop the item instead
    @EventHandler
    public void onLavaAbsorption(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        if (!protectLava || !plugin.getWorldManager().isSkyWorld(block.getWorld())) {
            return;
        }
        if (isLavaSource(block.getBlockData())) {
            if (event.getTo() != Material.LAVA) {
                event.setCancelled(true);
                // Drop the item diagonally above to reduce the risk of the item falling into the lava
                block.getWorld().dropItemNaturally(block.getLocation().add(1, 1, 1), new ItemStack(event.getTo()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVisitorDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
            && plugin.getWorldManager().isSkyAssociatedWorld(player.getWorld())
            && !plugin.playerIsOnIsland(player)) {
            if ((visitorFireProtected && FIRE_TRAP.contains(event.getCause()))
                || (visitorFallProtected && (event.getCause() == EntityDamageEvent.DamageCause.FALL))) {
                event.setDamage(-event.getDamage());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVisitorDamageByEntity(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player
            && plugin.getWorldManager().isSkyAssociatedWorld(player.getWorld())
            && !plugin.playerIsOnIsland(player)
            && !(event.getDamager() instanceof Player && Settings.island_allowPvP)) {
            if (visitorMonsterProtected &&
                (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                    || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                    || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                    || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                    || event.getCause() == EntityDamageEvent.DamageCause.MAGIC
                    || event.getCause() == EntityDamageEvent.DamageCause.POISON)) {
                event.setDamage(-event.getDamage());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && plugin.playerIsInSpawn(player) && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(-event.getDamage());
            event.setCancelled(true);
            player.setFallDistance(0);
            plugin.getTeleportLogic().spawnTeleport(player, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMemberDamage(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && plugin.getWorldManager().isSkyAssociatedWorld(victim.getWorld())) {
            if (event.getDamager() instanceof Player attacker) {
                cancelMemberDamage(attacker, victim, event);
            }
            else if (event.getDamager() instanceof Projectile && !(event.getDamager() instanceof EnderPearl)) {
                ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                if (shooter instanceof Player attacker) {
                    cancelMemberDamage(attacker, victim, event);
                }
            }
        }
    }

    private void cancelMemberDamage(Player attacker, Player victim, EntityDamageByEntityEvent event) {
        IslandInfo is1 = plugin.getIslandInfo(attacker);
        IslandInfo is2 = plugin.getIslandInfo(victim);
        if (is1 != null && is2 != null && is1.getName().equals(is2.getName())) {
            plugin.notifyPlayer(attacker, tr("\u00a7eYou cannot hurt island-members."));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        WorldManager wm = plugin.getWorldManager();
        World pWorld = event.getPlayer().getWorld();
        if (!wm.isSkyAssociatedWorld(pWorld)) {
            return;
        }

        if (Settings.extras_respawnAtIsland) {
            PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());
            if (playerInfo.getHasIsland()) {
                Location homeLocation = LocationUtil.findNearestSafeLocation(playerInfo.getHomeLocation(), null);
                if (homeLocation == null) {
                    homeLocation = LocationUtil.findNearestSafeLocation(playerInfo.getIslandLocation(), null);
                }
                // If homeLocation is somehow still null, we intentionally fallthrough
                if (homeLocation != null) {
                    event.setRespawnLocation(homeLocation);
                    return;
                }
            }
        }
        if (!Settings.extras_sendToSpawn && wm.isSkyWorld(pWorld)) {
            event.setRespawnLocation(plugin.getWorldManager().getWorld().getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || !plugin.getWorldManager().isSkyWorld(event.getTo().getWorld())) {
            return;
        }
        final Player player = event.getPlayer();
        boolean isAdmin = player.isOp() || player.hasPermission("usb.mod.bypassprotection");
        IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(WorldGuardHandler.getIslandNameAt(event.getTo()));
        if (!isAdmin && islandInfo != null && islandInfo.isBanned(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(tr("\u00a74That player has forbidden you from teleporting to their island."));
        }
        if (!isAdmin && islandInfo != null && islandInfo.isLocked() && !islandInfo.getMembers().contains(player.getName()) && !islandInfo.isTrusted(player)) {
            event.setCancelled(true);
            player.sendMessage(tr("\u00a74That island is \u00a7clocked.\u00a7e No teleporting to the island."));
        }
        if (!event.isCancelled()) {
            final PlayerInfo playerInfo = plugin.getPlayerInfo(player);
            playerInfo.onTeleport(player);
        }
    }

    /**
     * This EventHandler handles {@link BlockBreakEvent} to detect if a player broke leaves in the skyworld,
     * and will drop a sapling if so. This will prevent cases where the default generated tree on a new
     * island drops no saplings.
     * @param event BlockBreakEvent to handle.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLeafBreak(BlockBreakEvent event) {
        if (plugin.playerIsOnIsland(event.getPlayer()) && leafSaplings.containsKey(event.getBlock().getType())) {
            IslandInfo islandInfo = plugin.getIslandInfo(event.getBlock().getLocation());
            if (islandInfo != null && islandInfo.getLeafBreaks() == 0) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(leafSaplings.get(event.getBlock().getType())));
                islandInfo.setLeafBreaks(1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (!blockLimitsEnabled || !plugin.getWorldManager().isSkyAssociatedWorld(player.getWorld())) {
            return; // Skip
        }

        IslandInfo islandInfo = plugin.getIslandInfo(event.getBlock().getLocation());
        if (islandInfo == null) {
            return;
        }
        Material type = event.getBlock().getType();
        BlockLimitLogic.CanPlace canPlace = plugin.getBlockLimitLogic().canPlace(type, islandInfo);
        if (canPlace == BlockLimitLogic.CanPlace.UNCERTAIN) {
            event.setCancelled(true);
            final String key = "usb.block-limits";
            if (!PatienceTester.isRunning(player, key)) {
                PatienceTester.startRunning(player, key);
                player.sendMessage(tr("\u00a74{0} is limited. \u00a7eScanning your island to see if you are allowed to place more, please be patient", ItemStackUtil.getItemName(new ItemStack(type))));
                plugin.fireAsyncEvent(new IslandInfoEvent(player, islandInfo.getIslandLocation(), new Callback<>() {
                    @Override
                    public void run() {
                        player.sendMessage(tr("\u00a7e... Scanning complete, you can try again"));
                        PatienceTester.stopRunning(player, key);
                    }
                }));
            }
            return;
        }
        if (canPlace == BlockLimitLogic.CanPlace.NO) {
            event.setCancelled(true);
            player.sendMessage(tr("\u00a74You''ve hit the {0} limit!\u00a7e You can''t have more of that type on your island!\u00a79 Max: {1,number}", ItemStackUtil.getItemName(new ItemStack(type)), plugin.getBlockLimitLogic().getLimit(type)));
            return;
        }
        plugin.getBlockLimitLogic().incBlockCount(islandInfo.getIslandLocation(), type);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        if (!blockLimitsEnabled || !plugin.getWorldManager().isSkyAssociatedWorld(event.getBlock().getWorld())) {
            return; // Skip
        }
        IslandInfo islandInfo = plugin.getIslandInfo(event.getBlock().getLocation());
        if (islandInfo == null) {
            return;
        }
        plugin.getBlockLimitLogic().decBlockCount(islandInfo.getIslandLocation(), event.getBlock().getType());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event) {
        if (blockLimitsEnabled && plugin.getWorldManager().isSkyAssociatedWorld(event.getLocation().getWorld())) {
            IslandInfo islandInfo = plugin.getIslandInfo(event.getLocation());
            if (islandInfo != null) {
                for (Block block : event.blockList()) {
                    plugin.getBlockLimitLogic().decBlockCount(islandInfo.getIslandLocation(), block.getType());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplodeUnknown(BlockExplodeEvent event) {
        if (blockLimitsEnabled && plugin.getWorldManager().isSkyAssociatedWorld(event.getBlock().getWorld())) {
            IslandInfo islandInfo = plugin.getIslandInfo(event.getBlock().getLocation());
            if (islandInfo != null) {
                for (Block block : event.blockList()) {
                    plugin.getBlockLimitLogic().decBlockCount(islandInfo.getIslandLocation(), block.getType());
                }
            }
        }
    }
}

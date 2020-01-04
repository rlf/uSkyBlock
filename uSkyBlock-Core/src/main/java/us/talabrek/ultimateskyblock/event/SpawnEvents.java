package us.talabrek.ultimateskyblock.event;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wither;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MonsterEggs;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;
import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Responsible for controlling spawns on uSkyBlock islands.
 */
public class SpawnEvents implements Listener {
    private static final Set<Action> RIGHT_CLICKS = new HashSet<>(Arrays.asList(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK));
    private static final Set<CreatureSpawnEvent.SpawnReason> PLAYER_INITIATED = new HashSet<>(Arrays.asList(
            CreatureSpawnEvent.SpawnReason.BREEDING,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM, CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
            CreatureSpawnEvent.SpawnReason.BUILD_WITHER
    ));
    private static final Set<CreatureSpawnEvent.SpawnReason> ADMIN_INITIATED = new HashSet<>(Arrays.asList(
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
    ));

    private final uSkyBlock plugin;

    private boolean phantomsInOverworld;
    private boolean phantomsInNether;

    public SpawnEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        phantomsInOverworld = plugin.getConfig().getBoolean("options.spawning.phantoms.overworld", true);
        phantomsInNether = plugin.getConfig().getBoolean("options.spawning.phantoms.nether", false);
    }

    @EventHandler
    public void onSpawnEggEvent(PlayerInteractEvent event) {
        Player player = event != null ? event.getPlayer() : null;
        if (player == null || event.isCancelled() || !plugin.getWorldManager().isSkyWorld(player.getWorld())) {
            return; // Bail out, we don't care
        }
        if (player.hasPermission("usb.mod.bypassprotection") || player.isOp()) {
            return;
        }
        ItemStack item = event.getItem();
        if (RIGHT_CLICKS.contains(event.getAction()) && item != null && isSpawnEgg(item)) {
            if (!plugin.playerIsOnIsland(player)) {
                event.setCancelled(true);
                plugin.notifyPlayer(player, tr("\u00a7eYou can only use spawn-eggs on your own island."));
                return;
            }
            SpawnEgg spawnEgg = (SpawnEgg) item.getData();
            checkLimits(event, spawnEgg.getSpawnedType(), player.getLocation());
            if (event.isCancelled()) {
                plugin.notifyPlayer(player, tr("\u00a7cYou have reached your spawn-limit for your island."));
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }

    private boolean isSpawnEgg(ItemStack item) {
        return item.getType().name().endsWith("_SPAWN_EGG") && item.getData() instanceof MonsterEggs;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event == null || !plugin.getWorldManager().isSkyAssociatedWorld(event.getLocation().getWorld())) {
            return; // Bail out, we don't care
        }
        if (!event.isCancelled() && ADMIN_INITIATED.contains(event.getSpawnReason())) {
            return; // Allow it, the above method would have blocked it if it should be blocked.
        }
        checkLimits(event, event.getEntity().getType(), event.getLocation());
        if (event.getEntity() instanceof WaterMob) {
            Location loc = event.getLocation();
            if (isDeepOceanBiome(loc) && isPrismarineRoof(loc)) {
                loc.getWorld().spawnEntity(loc, EntityType.GUARDIAN);
                event.setCancelled(true);
            }
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER && event.getEntity() instanceof Wither) {
            IslandInfo islandInfo = plugin.getIslandInfo(event.getLocation());
            if (islandInfo != null && islandInfo.getLeader() != null) {
                event.getEntity().setCustomName(I18nUtil.tr("{0}''s Wither", islandInfo.getLeader()));
                event.getEntity().setMetadata("fromIsland", new FixedMetadataValue(plugin, islandInfo.getName()));
            }
        }
    }

    private boolean isPrismarineRoof(Location loc) {
        List<Material> prismarineBlocks = Arrays.asList(Material.PRISMARINE, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE);
        return prismarineBlocks.contains(LocationUtil.findRoofBlock(loc).getType());
    }

    private boolean isDeepOceanBiome(Location loc) {
        List<Biome> deepOceans = Arrays.asList(Biome.DEEP_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.DEEP_WARM_OCEAN);
        return deepOceans.contains(loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    private void checkLimits(Cancellable event, EntityType entityType, Location location) {
        if (entityType == null) {
            return; // Only happens on "other-plugins", i.e. EchoPet
        }
        String islandName = WorldGuardHandler.getIslandNameAt(location);
        if (islandName == null) {
            event.setCancelled(true); // Only allow spawning on active islands...
            return;
        }
        if (entityType.getEntityClass().isAssignableFrom(Ghast.class) && location.getWorld().getEnvironment() != World.Environment.NETHER) {
            // Disallow ghasts for now...
            event.setCancelled(true);
            return;
        }
        us.talabrek.ultimateskyblock.api.IslandInfo islandInfo = plugin.getIslandInfo(islandName);
        if (islandInfo == null) {
            // Disallow spawns on inactive islands
            event.setCancelled(true);
            return;
        }
        if (!plugin.getLimitLogic().canSpawn(entityType, islandInfo)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Phantom) ||
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        World spawnWorld = event.getEntity().getWorld();
        if (!phantomsInOverworld && plugin.getWorldManager().isSkyWorld(spawnWorld)) {
            event.setCancelled(true);
        }

        if (!phantomsInNether && plugin.getWorldManager().isSkyNether(spawnWorld)) {
            event.setCancelled(true);
        }
    }

    /**
     * Changes the setting that allows Phantoms to spawn in the overworld. Used for testing purposes.
     * @param state True/enabled means spawning is allowed, false disallowed.
     */
    void setPhantomsInOverworld(boolean state) {
        this.phantomsInOverworld = state;
    }

    /**
     * Changes the setting that allows Phantoms to spawn in the nether. Used for testing purposes.
     * @param state True/enabled means spawning is allowed, false disallowed.
     */
    void setPhantomsInNether(boolean state) {
        this.phantomsInNether = state;
    }
}

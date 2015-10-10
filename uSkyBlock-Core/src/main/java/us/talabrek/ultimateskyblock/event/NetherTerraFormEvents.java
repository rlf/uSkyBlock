package us.talabrek.ultimateskyblock.event;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;
import us.talabrek.ultimateskyblock.util.MaterialUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Responsible for forming the correct blocks in nether on block-breaks.
 */
public class NetherTerraFormEvents implements Listener {
    private final uSkyBlock plugin;
    private final Map<Material,List<MaterialUtil.MaterialProbability>> terraFormMap = new HashMap<>();
    private static final Random RND = new Random(System.currentTimeMillis());
    private final double maxScan;
    private final double chanceWither;
    private final double chanceSkeleton;
    private final double chanceBlaze;

    public NetherTerraFormEvents(uSkyBlock plugin) {
        this.plugin = plugin;
        // TODO: 23/09/2015 - R4zorax: Allow this to be perk-based?
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("nether.terraform");
        if (config != null) {
            for (String key : config.getKeys(false)) {
                Material mat = Material.getMaterial(key);
                if (mat != null) {
                    terraFormMap.put(mat, MaterialUtil.createProbabilityList(config.getStringList(key)));
                }
            }
        }
        maxScan = plugin.getConfig().getInt("nether.terraform-distance", 7);
        config = plugin.getConfig().getConfigurationSection("nether.spawn-chances");
        if (config != null) {
            chanceBlaze = config.getDouble("blaze", 0.2);
            chanceWither = config.getDouble("wither", 0.4);
            chanceSkeleton = config.getDouble("skeleton", 0.1);
        } else {
            chanceBlaze = 0.2;
            chanceWither = 0.4;
            chanceSkeleton = 0.1;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event == null) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block == null || player == null || !plugin.isSkyNether(block.getWorld()) || !plugin.isSkyNether(player.getWorld())) {
            return; // Bail out, not our problem
        }
        // TODO: 23/09/2015 - R4zorax: Test that the player is actually on his own island
        if (!terraFormMap.containsKey(block.getType())) {
            return; // Not a block we terra-form on.
        }
        Location playerLocation = player.getLocation();
        Location blockLocation = LocationUtil.centerOnBlock(block.getLocation());
        Vector v = new Vector(blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
        v.subtract(new Vector(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()));
        v.normalize();
        // Disable spawning above the player... enabling the player to clear a region
        if (v.getY() <= 0.865) {
            List<Material> yield = getYield(block.getType());
            for (Material mat : yield) {
                spawnBlock(mat, blockLocation, v);
            }
        }
    }

    private void spawnBlock(Material type, Location location, Vector v) {
        Location spawnLoc = null;
        if (MaterialUtil.isFallingMaterial(type)) {
            spawnLoc = findSolidSpawnLocation(location, v);
        } else {
            spawnLoc = findAirSpawnLocation(location, v);
        }
        if (spawnLoc != null) {
            spawnLoc.getWorld().getBlockAt(spawnLoc).setType(type);
        }
    }

    private Location findAirSpawnLocation(Location location, Vector v) {
        // Searches in a cone for an air block
        Location lookAt = new Location(location.getWorld(),
                Math.round(location.getX() + v.getX()),
                Math.round(location.getY() + v.getY()),
                Math.round(location.getZ() + v.getZ()));
        while (v.length() < maxScan) {
            for (Location loc : getLocationsInPlane(lookAt, v)) {
                if (loc.getBlock().getType() == Material.AIR && isAdjacentToSolid(loc)) {
                    return loc;
                }
            }
            double n = v.length();
            v.normalize().multiply(n+1);
        }
        return null;
    }

    private boolean isAdjacentToSolid(Location loc) {
        for (BlockFace face : Arrays.asList(BlockFace.DOWN, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH)) {
            if (loc.getBlock().getRelative(face).getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    private Location findSolidSpawnLocation(Location location, Vector v) {
        // Searches in a cone for an air block

        while (v.length() < maxScan) {
            for (Location loc : getLocationsInPlane(location, v)) {
                if (loc.getBlock().getType() == Material.AIR && loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                    return loc;
                }
            }
            double n = v.length();
            v.normalize().multiply(n+1);
        }
        return null;
    }

    private List<Location> getLocationsInPlane(Location location, Vector v) {
        Location lookAt = new Location(location.getWorld(),
                Math.round(location.getX() + v.getX()),
                Math.round(location.getY() + v.getY()),
                Math.round(location.getZ() + v.getZ()));
        List<Location> locs = new ArrayList<>();
        boolean xFixed = Math.abs(v.getX()) > Math.abs(v.getZ());
        for (int r = 1; r <= v.length(); r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dxz = -r; dxz <= r; dxz++) {
                    if (xFixed) {
                        locs.add(lookAt.clone().add(0, dy, dxz));
                    } else {
                        locs.add(lookAt.clone().add(dxz, dy, 0));
                    }
                }
            }
        }
        Collections.shuffle(locs);
        locs = locs.subList(0, locs.size()/2); // Only try half
        return locs;
    }

    public List<Material> getYield(Material material) {
        List<Material> copy = new ArrayList<>();
        for (MaterialUtil.MaterialProbability e : terraFormMap.get(material)) {
            if (RND.nextDouble() < e.getProbability()) {
                copy.add(e.getMaterial());
            }
        }
        return copy;
    }
    @EventHandler
    public void onGhastExplode(EntityExplodeEvent event) {
        if (event == null || event.getEntity() == null || !plugin.isSkyNether(event.getEntity().getWorld())) {
            return; // Bail out, not our problem
        }
        // TODO: 23/09/2015 - R4zorax: Perhaps enable this when island has a certain level?
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();
            fireball.setIsIncendiary(false);
            fireball.setFireTicks(0);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e == null || e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL || e.getEntity() == null) {
            return;
        }
        if (!plugin.isSkyNether(e.getLocation().getWorld())) {
            return;
        }
        if (e.getLocation().getBlockY() > 127) {
            // Block spawning above nether...
            e.setCancelled(true);
            return;
        }
        // TODO: 23/09/2015 - R4zorax: obey the spawn-limits
        if (e.getEntity() instanceof PigZombie) {
            Block block = e.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (isNetherFortressWalkway(block)) {
                e.setCancelled(true);
                double p = RND.nextDouble();
                if (p <= chanceWither) {
                    // Spawn Wither.
                    Skeleton mob = (Skeleton) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.SKELETON);
                    mob.setSkeletonType(Skeleton.SkeletonType.WITHER);
                } else if (p <= chanceWither+chanceBlaze) {
                    // Spawn Blaze
                    Blaze mob = (Blaze) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.BLAZE);
                } else if (p <= chanceWither+chanceBlaze+chanceSkeleton) {
                    // Spawn Skeleton
                    Skeleton mob = (Skeleton) e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.SKELETON);
                } else {
                    e.setCancelled(false); // Spawn PigZombie
                }
            }
        }
    }

    private boolean isNetherFortressWalkway(Block block) {
        // TODO: 23/09/2015 - R4zorax: More intelligently please...
        // NS          NS    NS =
        // NB NB NB NB NB    NB = NetherBrick
        return block.getType() == Material.NETHER_BRICK;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e == null || e.getPlayer() == null || e.getTo() == null || !plugin.isSkyNether(e.getTo().getWorld())) {
            return; // Bail out.
        }
        if (e.getTo().getBlockY() > 127) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(tr("\u00a7cNo Access! \u00a7eYou are trying to teleport to the roof of the \u00a7cNETHER\u00a7e, that is not allowed."));
        }
    }
}

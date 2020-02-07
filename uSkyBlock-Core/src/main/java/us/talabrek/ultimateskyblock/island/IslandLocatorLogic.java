package us.talabrek.ultimateskyblock.island;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Responsible for keeping track of and locating island locations for new islands.
 */
public class IslandLocatorLogic {
    private static final Logger log = Logger.getLogger(IslandLocatorLogic.class.getName());
    private final uSkyBlock plugin;
    private final File configFile;
    private final YmlConfiguration config;
    private final Map<String, Long> reservations = new ConcurrentHashMap<>();
    private Location lastIsland = null;
    private long reservationTimeout;

    public IslandLocatorLogic(final uSkyBlock plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "lastIslandConfig.yml");
        this.config = new YmlConfiguration();
        FileUtil.readConfig(config, configFile);

        if (!config.contains("options.general.lastIslandX") ) {
           // Backward compatibility
           if (plugin.getConfig().contains("options.general.lastIslandX")) {
               config.set("options.general.lastIslandX", plugin.getConfig().getInt("options.general.lastIslandX"));
               config.set("options.general.lastIslandZ", plugin.getConfig().getInt("options.general.lastIslandZ"));
               plugin.getConfig().set("options.general.lastIslandX", null);
               plugin.getConfig().set("options.general.lastIslandZ", null);
           }
           else {
               config.set("options.general.lastIslandX", Settings.orgx );
               config.set("options.general.lastIslandZ", Settings.orgz );
           }
        }
        reservationTimeout = plugin.getConfig().getLong("options.island.reservationTimeout", 5 * 60000);
    }

    private Location getLastIsland() {
        if (lastIsland == null) {
            lastIsland = new Location(plugin.getWorldManager().getWorld(),
                    config.getInt("options.general.lastIslandX", 0), Settings.island_height,
                    config.getInt("options.general.lastIslandZ", 0));
        }
        return LocationUtil.alignToDistance(lastIsland, Settings.island_distance);
    }

    public synchronized Location getNextIslandLocation(Player player) {
        Location islandLocation = getNext(player);
        reserve(islandLocation);
        return islandLocation.clone();
    }

    private void reserve(Location islandLocation) {
        final String islandName = LocationUtil.getIslandName(islandLocation);
        final long tstamp = System.currentTimeMillis();
        reservations.put(islandName, tstamp);
        plugin.async(new Runnable() {
            @Override
            public void run() {
                synchronized (reservations) {
                    Long tReserved = reservations.get(islandName);
                    if (tReserved != null && tReserved == tstamp) {
                        reservations.remove(islandName);
                    }
                }
            }
        }, reservationTimeout);
    }

    private synchronized Location getNext(Player player) {
        Location last = getLastIsland();
        if (plugin.getWorldManager().isSkyWorld(player.getWorld()) && !plugin.islandInSpawn(player.getLocation())) {
            Location location = LocationUtil.alignToDistance(player.getLocation(), Settings.island_distance);
            if (isAvailableLocation(location)) {
                player.sendMessage(tr("\u00a79Creating an island at your location"));
                return location;
            }
            Vector v = player.getLocation().getDirection().normalize();
            location = LocationUtil.alignToDistance(location.add(v.multiply(Settings.island_distance)), Settings.island_distance);
            if (isAvailableLocation(location)) {
                player.sendMessage(tr("\u00a79Creating an island \u00a77{0}\u00a79 of you", LocationUtil.getCardinalDirection(player.getLocation().getYaw())));
                return location;
            }
        }
        Location next = plugin.getOrphanLogic().getNextValidOrphan();
        if (next == null) {
            next = last;
            // Ensure the found location is valid (or find one that is).
            while (!isAvailableLocation(next)) {
                next = nextIslandLocation(next);
            }
        }
        lastIsland = next;
        save();
        return next;
    }

    private void save() {
        final Location locationToSave = lastIsland;
        plugin.async(new Runnable() {
            @Override
            public void run() {
                try {
                    config.set("options.general.lastIslandX", locationToSave.getBlockX());
                    config.set("options.general.lastIslandZ", locationToSave.getBlockZ());
                    config.save(configFile);
                } catch (IOException e) {
                    log.warning("Unable to save " + configFile);
                }
            }
        });
    }

    public boolean isAvailableLocation(Location next) {
        return !(plugin.islandInSpawn(next) || plugin.islandAtLocation(next) || isReserved(next));
    }

    private boolean isReserved(Location next) {
        return reservations.containsKey(LocationUtil.getIslandName(next));
    }
    /**
     * <pre>
     *  [NW]                      -z                     [NE]
     *   x = z                    ^                    x = -z
     *        \        -x < -z    |     x < -z        /
     *           \               [N]               /
     *              \             |             /
     *  forbid=0b1000  \          |          / forbid=0b0001
     *  quadrant 3        \       |       /    quadrant 0
     *        -x > -z        \    |    /             x > -z
     *                        /~~~^~~~\
     *     -------[W]--------<orgx,orgz>----------[E]-----------> x
     *                        \---v---/
     *        -x > z         /    |    \       quadrant 1
     *        (x < -z)    /       |       \          x >  z
     *  forbid=0b0100  /          |          \ forbid=0b0010
     *  quadrant 2  /             |             \
     *           /     -x < z     |   x < z        \
     *       x = -z              [S]               x = z
     *       /                    |                    \
     *    [SW]                    v                   [SE]
     * </pre>
     */
    static Location nextIslandLocation(final Location lastIsland) {
        int d = Settings.island_distance;
        boolean isforbidden = false;
        LocationUtil.alignToDistance(lastIsland, d);
        int x = lastIsland.getBlockX()-Settings.orgx;
        int z = lastIsland.getBlockZ()-Settings.orgz;
        do {
           // Snake, counterclockwise
           if (x < z) {
              if (-x < z)  x += d;  // E
              else         z += d;  // S
           } 
           else if (x > z) {
              if (-x >= z) x -= d;  // W
              else         z -= d;  // N
           } 
           else { // x == z
              if (x <= 0)  z += d;  // S
              else         z -= d;  // N
           }
           if( Settings.forbid == 0 ) break;
           // Check the new location for forbidden areas
           isforbidden = false;
           if( x>0 && z<0 && (Settings.forbid & 0b0001) !=0 ) isforbidden = true;
           if( x>0 && z>0 && (Settings.forbid & 0b0010) !=0 ) isforbidden = true;
           if( x<0 && z>0 && (Settings.forbid & 0b0100) !=0 ) isforbidden = true;
           if( x<0 && z<0 && (Settings.forbid & 0b1000) !=0 ) isforbidden = true;
        } while( isforbidden );
        //
        // A BIG FAT WARNING!
        // ==================
        // This code is a Quick And Dirty solution!
        // In case of large SkyBlock, it might cause severe LAGG!
        // Volunteers! It is a TODO for one step path-finding :-)
        // (scrolling through the path this way is quite brainless)
        // 
        lastIsland.setX(x+Settings.orgx);
        lastIsland.setZ(z+Settings.orgz);
        return lastIsland;
    }
}

package us.talabrek.ultimateskyblock.island;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.io.IOException;
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
    private final Cache<String, Location> reservations;
    private Location lastIsland = null;

    public IslandLocatorLogic(final uSkyBlock plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "lastIslandConfig.yml");
        this.config = new YmlConfiguration();
        FileUtil.readConfig(config, configFile);
        // Backward compatibility
        if (!config.contains("options.general.lastIslandX") && plugin.getConfig().contains("options.general.lastIslandX")) {
            config.set("options.general.lastIslandX", plugin.getConfig().getInt("options.general.lastIslandX"));
            config.set("options.general.lastIslandZ", plugin.getConfig().getInt("options.general.lastIslandZ"));
            plugin.getConfig().set("options.general.lastIslandX", null);
            plugin.getConfig().set("options.general.lastIslandZ", null);
        }
        // Lazy loading of lastIsland
        reservations = CacheBuilder
                .from(plugin.getConfig().getString("options.advanced.lastIslandCache", "maximumSize=1000,expireAfterWrite=5m"))
                .removalListener(new RemovalListener<String, Location>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, Location> removal) {
                        if (!plugin.islandAtLocation(removal.getValue())) {
                            // If an island has not been created within the timeout - we add the island to the orphans
                            plugin.getOrphanLogic().addOrphan(removal.getValue());
                        }
                    }})
                .build();
    }

    private Location getLastIsland() {
        if (lastIsland == null) {
            lastIsland = new Location(plugin.getWorld(),
                    config.getInt("options.general.lastIslandX", 0), Settings.island_height,
                    config.getInt("options.general.lastIslandZ", 0));
        }
        return LocationUtil.alignToDistance(lastIsland, Settings.island_distance);
    }

    public synchronized Location getNextIslandLocation(Player player) {
        Location islandLocation = getNext(player);
        reserve(islandLocation);
        return islandLocation;
    }

    private void reserve(Location islandLocation) {
        reservations.put(LocationUtil.getIslandName(islandLocation), islandLocation);
    }

    private synchronized Location getNext(Player player) {
        Location last = getLastIsland();
        if (plugin.isSkyWorld(player.getWorld()) && !plugin.islandInSpawn(player.getLocation())) {
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
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
        return reservations.getIfPresent(LocationUtil.getIslandName(next)) != null;
    }

    /**
     * <pre>
     *                            z
     *   x = -z                   ^                    x = z
     *        \        -x < z     |     x < z         /
     *           \                |                /
     *              \             |             /
     *                 \          |          /
     *                    \       |       /          x > z
     *        -x > z         \    |    /
     *                          \ | /
     *     -----------------------+-----------------------------> x
     *                          / | \
     *        -x > -z        /    |    \
     *        (x < z)     /       |       \          x > -z
     *                 /          |          \
     *              /             |             \
     *           /     -x < -z    |   x < -z       \
     *       x = z                |                x = -z
     *                            |
     *                            v
     * </pre>
     */
    static Location nextIslandLocation(final Location lastIsland) {
        int d = Settings.island_distance;
        LocationUtil.alignToDistance(lastIsland, d);
        int x = lastIsland.getBlockX();
        int z = lastIsland.getBlockZ();
        if (x < z) {
            if (-1 * x < z) {
                x += d;
            } else {
                z += d;
            }
        } else if (x > z) {
            if (-1 * x >= z) {
                x -= d;
            } else {
                z -= d;
            }
        } else { // x == z
            if (x <= 0) {
                z += d;
            } else {
                z -= d;
            }
        }
        lastIsland.setX(x);
        lastIsland.setZ(z);
        return lastIsland;
    }
}

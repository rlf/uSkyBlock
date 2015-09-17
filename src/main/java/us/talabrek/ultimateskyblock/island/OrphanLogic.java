package us.talabrek.ultimateskyblock.island;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Responsible for storing, accessing and handling orphans.
 */
public class OrphanLogic {
    // Used in a HACKY way to indicate the origin of an island location as an orphan.
    public static final float ORPHAN_PITCH = -30;
    public static final float ORPHAN_YAW = 90;

    private final uSkyBlock plugin;
    private final FileConfiguration config;
    private final File configFile;
    private SortedSet<Orphan> orphaned = new TreeSet<>(new OrphanComparator());

    public OrphanLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "orphans.yml");
        config = FileUtil.getFileConfiguration("orphans.yml");
        readOrphans();
    }

    private void readOrphans() {
        if (config.contains("orphans.list")) {
            // Old format
            final String fullOrphan = config.getString("orphans.list");
            if (!fullOrphan.isEmpty()) {
                final String[] orphanArray = fullOrphan.split(";");
                for (String loc : orphanArray) {
                    orphaned.add(new Orphan(loc));
                }
                config.set("orphans.list", null); // delete config-node
                save();
            }
        } else if (config.isList("orphans")) {
            for (String loc : config.getStringList("orphans")) {
                orphaned.add(new Orphan(loc));
            }
        }
    }

    public void save() {
        // TODO: 17/09/2015 - R4zorax: Perhaps not save to file EVERY time?
        ArrayList<Object> value = new ArrayList<>();
        for (Orphan orphan : new ArrayList<>(orphaned)) {
            value.add(orphan.toString());
        }
        config.set("orphans", value);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Unable to store orphans: " + e);
        }
    }

    public void addOrphan(String loc) {
        orphaned.add(new Orphan(loc));
    }

    public void addOrphan(Location location) {
        if (location != null) {
            orphaned.add(new Orphan(location.getBlockX(), location.getBlockZ()));
            save();
        }
    }

    public Location getNextValidOrphan() {
        if (orphaned.isEmpty()) {
            return null;
        }
        try {
            World world = plugin.getWorld();
            for (Iterator<Orphan> it = orphaned.iterator(); it.hasNext(); ) {
                Orphan candidate = it.next();
                if (candidate != null) {
                    it.remove();
                    Location loc = new Location(world, candidate.getX(), Settings.island_height, candidate.getZ(), ORPHAN_YAW, ORPHAN_PITCH);
                    if (!plugin.islandInSpawn(loc) && !plugin.islandAtLocation(loc)) {
                        return loc;
                    }
                }
            }
            return null;
        } finally {
            save();
        }
    }

    public void clear() {
        orphaned.clear();
        save();
    }

    public boolean wasOrphan(Location loc) {
        return loc != null && loc.getYaw() == ORPHAN_YAW && loc.getPitch() == ORPHAN_PITCH;
    }

    public List<Orphan> getOrphans() {
        return Collections.unmodifiableList(new ArrayList<>(orphaned));
    }

    public static class Orphan {
        private final int x;
        private final int z;

        public Orphan(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public Orphan(String loc) {
            String[] xy = loc != null ? loc.split(",") : new String[]{"0", "0"};
            x = Integer.parseInt(xy[0], 10);
            z = Integer.parseInt(xy[1], 10);
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public int distanceSquared() {
            return x*x + z * z;
        }

        @Override
        public String toString() {
            return x + "," + z;
        }
    }
}

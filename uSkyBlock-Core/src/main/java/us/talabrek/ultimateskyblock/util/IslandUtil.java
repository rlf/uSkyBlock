package us.talabrek.ultimateskyblock.util;

import org.bukkit.Location;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by R4zorax on 04/11/2015.
 */
public enum IslandUtil {;

    public static FilenameFilter createIslandFilenameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null
                        && name.matches("-?[0-9]+,-?[0-9]+.yml")
                        && !"null.yml".equalsIgnoreCase(name)
                        && !"0,0.yml".equalsIgnoreCase(name);
            }
        };
    }

    public static Location getIslandLocation(String islandName) {
        if (islandName == null || islandName.isEmpty()) {
            return null;
        }
        World world = uSkyBlock.getInstance().getWorld();
        String[] cords = islandName.split(",");
        return new Location(world, Long.parseLong(cords[0], 10), Settings.island_height, Long.parseLong(cords[1], 10));
    }
}

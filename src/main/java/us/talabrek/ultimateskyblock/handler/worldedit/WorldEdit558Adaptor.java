package us.talabrek.ultimateskyblock.handler.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * The World Edit 5.5.8 specific adaptations.
 */
public class WorldEdit558Adaptor implements WorldEditAdaptor {
    private WorldEditPlugin worldEditPlugin;

    public WorldEdit558Adaptor() {
    }

    @Override
    public void init(WorldEditPlugin worldEditPlugin) {
        this.worldEditPlugin = worldEditPlugin;
    }
    @Override
    public boolean loadIslandSchematic(World world, File file, Location origin) {
        return false;
    }
}

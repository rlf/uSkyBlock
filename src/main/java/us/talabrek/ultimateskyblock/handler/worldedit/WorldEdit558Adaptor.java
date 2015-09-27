package us.talabrek.ultimateskyblock.handler.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Location;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

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
    public void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
        // TODO: 22/09/2015 - R4zorax: Perhaps we should actually support this version?
    }
}

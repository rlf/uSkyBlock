package us.talabrek.ultimateskyblock.handler.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Location;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.util.VersionUtil;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API allowing us to support both WE 5 and WE 6.
 */
public interface WorldEditAdaptor {
    /**
     * Initializes the API.
     * @param worldEditPlugin
     */
    void init(WorldEditPlugin worldEditPlugin);

    /**
     * Loads and pastes a schematic at the designated position.
     * Returns <code>true</code> if successful.
     * @return <code>true</code> if successful.
     */
    boolean loadIslandSchematic(World world, File file, Location origin, PlayerPerk playerPerk);

    public static class Factory {
        private static final Logger log = Logger.getLogger(Factory.class.getName());
        public static WorldEditAdaptor create(WorldEditPlugin worldEdit) {
            if (worldEdit != null && worldEdit.isEnabled() && worldEdit.getDescription() != null) {
                VersionUtil.Version version = VersionUtil.getVersion(worldEdit.getDescription().getVersion());
                String apiClass = null;
                if (version.isGTE("6.0")) {
                    apiClass = "us.talabrek.ultimateskyblock.handler.worldedit.WorldEdit6Adaptor";
                } else if (version.isGTE("5.5.8")) {
                    apiClass = "us.talabrek.ultimateskyblock.handler.worldedit.WorldEdit558Adaptor";
                }
                if (apiClass != null) {
                    try {
                        WorldEditAdaptor api = (WorldEditAdaptor) Class.forName(apiClass).newInstance();
                        api.init(worldEdit);
                        return api;
                    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                        // Should not happen...
                        log.log(Level.WARNING, "Unable to instantiate appropriate WorldEdit adaptor", e);
                    }
                }
            }
            return null;
        }
    }
}

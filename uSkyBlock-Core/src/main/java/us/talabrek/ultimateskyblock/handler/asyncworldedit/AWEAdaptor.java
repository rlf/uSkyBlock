package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

import java.io.File;

/**
 * Interface for various AWE version-adaptors.
 */
public interface AWEAdaptor {
    void onEnable(Plugin plugin);

    void onDisable(Plugin plugin);

    void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk);
    void registerCompletion(Player player);

    EditSession createEditSession(World world, int maxBlocks);

    void regenerate(Region region, Runnable onCompletion);
}

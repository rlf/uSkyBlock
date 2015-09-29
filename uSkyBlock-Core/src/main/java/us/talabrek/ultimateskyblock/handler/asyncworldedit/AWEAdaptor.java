package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.player.PlayerPerk;

import java.io.File;

/**
 * Interface for various AWE version-adaptors.
 */
public interface AWEAdaptor {
    public void onEnable(Plugin plugin);

    public void registerCompletion(Player player);

    public void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk);

    public void onDisable(Plugin plugin);
}

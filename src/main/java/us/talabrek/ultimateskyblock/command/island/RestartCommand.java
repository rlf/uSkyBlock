package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class RestartCommand extends RequireIslandCommand {
    public RestartCommand(uSkyBlock plugin) {
        super(plugin, "restart|reset", "delete your island and start a new one.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.getPartySize() > 1) {
            if (!island.isLeader(player)) {
                player.sendMessage("\u00a74Only the owner may restart this island. Leave this island in order to start your own (/island leave).");
            } else {
                player.sendMessage("\u00a7eYou must remove all players from your island before you can restart it (/island kick <player>). See a list of players currently part of your island using /island party.");
            }
            return true;
        }
        if (!plugin.onRestartCooldown(player) || Settings.general_cooldownRestart == 0) {
            return plugin.restartPlayerIsland(player, pi.getIslandLocation());
        }
        player.sendMessage("\u00a7eYou can restart your island in " + plugin.getRestartCooldownTime(player) / 1000L + " seconds.");
        return true;

    }
}

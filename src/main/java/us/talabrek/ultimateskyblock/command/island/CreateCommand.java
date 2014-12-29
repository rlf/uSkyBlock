package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class CreateCommand extends RequirePlayerCommand {
    private final uSkyBlock plugin;

    public CreateCommand(uSkyBlock plugin) {
        super("create|c", "usb.island.create", "create an island");
        this.plugin = plugin;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        PlayerInfo pi = plugin.getPlayerInfo(player);
        if (pi.getIslandLocation() == null) {
            plugin.createIsland(player, pi);
            return true;
        }
        if (pi.getIslandLocation().getBlockX() == 0 && pi.getIslandLocation().getBlockY() == 0 && pi.getIslandLocation().getBlockZ() == 0) {
            plugin.createIsland(player, pi);
            return true;
        } else if (pi.getHasIsland()) {
            IslandInfo island = plugin.getIslandInfo(pi);
            if (island.isLeader(player)) {
                player.sendMessage("\u00a74Island found!" +
                        "\u00a7e You already have an island. If you want a fresh island, type" +
                        "\u00a7b /is restart\u00a7e to get one");
            } else {
                player.sendMessage("\u00a74Island found!" +
                        "\u00a7e You are already a member of an island. To start your own, first" +
                        "\u00a7b /is leave");
            }
        }
        return true;
    }
}

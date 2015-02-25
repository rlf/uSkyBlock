package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class SetWarpCommand extends RequireIslandCommand {
    public SetWarpCommand(uSkyBlock plugin) {
        super(plugin, "setwarp|warpset", "usb.extra.addwarp", "set your island's warp location");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.hasPerm(player, "canChangeWarp")) {
            island.setWarpLocation(player.getLocation());
            island.sendMessageToIslandGroup("\u00a7b" +player.getName() + "\u00a7d changed the island warp location.");
        } else {
            player.sendMessage("\u00a7cYou do not have permission to set your island's warp point!");
        }
        return true;
    }
}

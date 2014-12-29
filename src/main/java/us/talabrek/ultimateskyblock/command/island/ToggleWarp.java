package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class ToggleWarp extends RequireIslandCommand {
    public ToggleWarp(uSkyBlock plugin) {
        super(plugin, "togglewarp|tw", "usb.extra.addwarp", "enable/disable warping to your island.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (island.hasPerm(player, "canToggleWarp")) {
            if (!island.hasWarp()) {
                if (island.isLocked()) {
                    player.sendMessage("\u00a74Your island is locked. You must unlock it before enabling your warp.");
                    return true;
                }
                island.sendMessageToIslandGroup(player.getName() + " activated the island warp.");
                island.setWarpActive(true);
            } else {
                island.sendMessageToIslandGroup(player.getName() + " deactivated the island warp.");
                island.setWarpActive(false);
            }
        } else {
            player.sendMessage("\u00a7cYou do not have permission to enable/disable your island's warp!");
        }
        return true;
    }
}

package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.Map;

public class SetWarpCommand extends RequireIslandCommand {
    public SetWarpCommand(uSkyBlock plugin) {
        super(plugin, "setwarp|warpset", "usb.extra.addwarp", I18nUtil.tr("set your island's warp location"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (!island.hasPerm(player, "canChangeWarp")) {
            player.sendMessage(I18nUtil.tr("\u00a7cYou do not have permission to set your island's warp point!"));
        } else if (!plugin.playerIsOnIsland(player)) {
            player.sendMessage(I18nUtil.tr("\u00a7cYou need to be on your own island to set the warp!"));
        } else {
            island.setWarpLocation(player.getLocation());
            island.sendMessageToIslandGroup(true, I18nUtil.marktr("\u00a7b{0}\u00a7d changed the island warp location."), player.getName());
        }
        return true;
    }
}

package us.talabrek.ultimateskyblock.command.island;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

public class HomeCommand extends RequireIslandCommand {
    public HomeCommand(uSkyBlock plugin) {
        super(plugin, "home|h", "usb.island.home", marktr("teleport to the island home"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (pi.isIslandGenerating()) {
            player.sendMessage(I18nUtil.tr("\u00a7cYour island is in the process of generating, you cannot teleport home right now."));
            return true;
        }
        if (pi.getHomeLocation() == null) {
            pi.setHomeLocation(pi.getIslandLocation());
        }
        plugin.homeTeleport(player, false);
        return true;
    }
}

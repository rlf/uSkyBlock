package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class SetHomeCommand extends RequireIslandCommand {
    public SetHomeCommand(uSkyBlock plugin) {
        super(plugin, "sethome|tpset", "usb.island.sethome", marktr("set the island-home"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getWorldManager().getWorld().getName())) {
            player.sendMessage(tr("\u00a74You must be closer to your island to set your skyblock home!"));
            return true;
        }
        if (!plugin.playerIsOnOwnIsland(player)) {
            player.sendMessage(tr("\u00a74You must be closer to your island to set your skyblock home!"));
            return true;
        }
        if (pi == null || !LocationUtil.isSafeLocation(player.getLocation())) {
            player.sendMessage(tr("\u00a74Your current location is not a safe home-location."));
            return true;
        }

        pi.setHomeLocation(player.getLocation());
        pi.save();
        player.sendMessage(tr("\u00a7aYour skyblock home has been set to your current location."));
        return true;
    }
}

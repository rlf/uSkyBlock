package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

public class SetHomeCommand extends RequireIslandCommand {
    public SetHomeCommand(uSkyBlock plugin) {
        super(plugin, "sethome|tpset", "usb.island.sethome", marktr("set the island-home"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        return plugin.homeSet(player);
    }
}

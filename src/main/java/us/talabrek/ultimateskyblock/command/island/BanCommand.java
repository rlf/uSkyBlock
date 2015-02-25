package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class BanCommand extends RequireIslandCommand {
    public BanCommand(uSkyBlock plugin) {
        super(plugin, "ban|banned|balist|b", "usb.island.ban", "player", "ban/unban a player from your island.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage("\u00a7eThe following players are banned from warping to your island:");
            player.sendMessage("\u00a74" + island.getBans());
            player.sendMessage("\u00a7eTo ban/unban from your island, use /island ban <player>");
            return true;
        } else if (args.length == 1) {
            String name = args[0];
            if (island.getMembers().contains(name)) {
                player.sendMessage("\u00a74You can't ban members. Remove them first!");
                return true;
            }
            if (!island.hasPerm(player.getName(), "canKickOthers")) {
                player.sendMessage("\u00a74You do not have permission to kick/ban players.");
                return true;
            }
            if (!island.isBanned(name)) {
                island.banPlayer(name);
                player.sendMessage("\u00a7eYou have banned \u00a74" + name + "\u00a7e from warping to your island.");
            } else {
                island.unbanPlayer(name);
                player.sendMessage("\u00a7eYou have unbanned \u00a7a" + name + "\u00a7e from warping to your island.");
            }
            return true;
        }
        return false;
    }
}

package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class BanCommand extends RequireIslandCommand {
    public BanCommand(uSkyBlock plugin) {
        super(plugin, "ban|unban", "usb.island.ban", "player", tr("ban/unban a player from your island."));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage(tr("\u00a7eThe following players are banned from warping to your island:"));
            player.sendMessage(tr("\u00a74{0}", island.getBans()));
            player.sendMessage(tr("\u00a7eTo ban/unban from your island, use /island ban <player>"));
            return true;
        } else if (args.length == 1) {
            String name = args[0];
            if (island.getMembers().contains(name)) {
                player.sendMessage(tr("\u00a74You can't ban members. Remove them first!"));
                return true;
            }
            if (!island.hasPerm(player.getName(), "canKickOthers")) {
                player.sendMessage(tr("\u00a74You do not have permission to kick/ban players."));
                return true;
            }
            if (!island.isBanned(name)) {
                island.banPlayer(name);
                player.sendMessage(tr("\u00a7eYou have banned \u00a74{0}\u00a7e from warping to your island.", name));
            } else {
                island.unbanPlayer(name);
                player.sendMessage(tr("\u00a7eYou have unbanned \u00a7a{0}\u00a7e from warping to your island.", name));
            }
            return true;
        }
        return false;
    }
}

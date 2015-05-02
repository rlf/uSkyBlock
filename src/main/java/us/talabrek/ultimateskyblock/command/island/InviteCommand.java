package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.InviteHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class InviteCommand extends RequireIslandCommand {
    private final InviteHandler inviteHandler;

    public InviteCommand(uSkyBlock plugin, InviteHandler inviteHandler) {
        super(plugin, "invite", "usb.party.create", "oplayer", tr("invite a player to your island"));
        this.inviteHandler = inviteHandler;
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage(tr("\u00a7eUse\u00a7f /island invite <playername>\u00a7e to invite a player to your island."));
            if (!island.isParty()) {
                return true;
            }
            if (!island.isLeader(player) || !island.hasPerm(player, "canInviteOthers")) {
                player.sendMessage(tr("\u00a74Only the island's owner can invite!"));
                return true;
            }
            int diff = island.getMaxPartySize() - island.getPartySize();
            if (diff > 0) {
                player.sendMessage(tr("\u00a7aYou can invite {0} more players.", diff));
            } else {
                player.sendMessage(tr("\u00a74You can't invite any more players."));
            }
        }
        if (args.length == 1) {
            Player otherPlayer = Bukkit.getPlayer(args[0]);
            if (!island.hasPerm(player, "canInviteOthers")) {
                player.sendMessage(tr("\u00a74You do not have permission to invite others to this island!"));
                return true;
            }
            if (otherPlayer == null || !otherPlayer.isOnline()) {
                player.sendMessage(tr("\u00a74That player is offline or doesn't exist."));
                return true;
            }
            if (player.getName().equalsIgnoreCase(otherPlayer.getName())) {
                player.sendMessage(tr("\u00a74You can't invite yourself!"));
                return true;
            }
            if (island.isLeader(otherPlayer)) {
                player.sendMessage(tr("\u00a74That player is the leader of your island!"));
                return true;
            }
            if (inviteHandler.invite(player, island, otherPlayer)) {
                island.sendMessageToIslandGroup(tr("{0}\u00a7d invited {1}", player.getDisplayName(), otherPlayer.getDisplayName()));
            }
        }
        return true;
    }
}

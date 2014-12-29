package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class InviteCommand extends RequireIslandCommand {
    private final InviteHandler inviteHandler;

    public InviteCommand(uSkyBlock plugin, InviteHandler inviteHandler) {
        super(plugin, "invite", "usb.party.create", "oplayer", "invite a player to your island");
        this.inviteHandler = inviteHandler;
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage("\u00a7eUse" + "\u00a7f /island invite <playername>\u00a7e to invite a player to your island.");
            if (!island.isParty()) {
                return true;
            }
            if (!island.isLeader(player) || !island.hasPerm(player, "canInviteOthers")) {
                player.sendMessage("\u00a74Only the island's owner can invite!");
                return true;
            }
            int diff = island.getMaxPartySize() - island.getPartySize();
            if (diff > 0) {
                player.sendMessage(ChatColor.GREEN + "You can invite " + diff + " more players.");
            } else {
                player.sendMessage("\u00a74You can't invite any more players.");
            }
        }
        if (args.length == 1) {
            Player otherPlayer = Bukkit.getPlayer(args[0]);
            if (!island.hasPerm(player, "canInviteOthers")) {
                player.sendMessage("\u00a74You do not have permission to invite others to this island!");
                return true;
            }
            if (otherPlayer == null || !otherPlayer.isOnline()) {
                player.sendMessage("\u00a74That player is offline or doesn't exist.");
                return true;
            }
            if (player.getName().equalsIgnoreCase(otherPlayer.getName())) {
                player.sendMessage("\u00a74You can't invite yourself!");
                return true;
            }
            if (island.isLeader(otherPlayer)) {
                player.sendMessage("\u00a74That player is the leader of your island!");
                return true;
            }
            if (inviteHandler.invite(player, island, otherPlayer)) {
                island.sendMessageToIslandGroup(player.getDisplayName() + " invited " + otherPlayer.getDisplayName());
            }
        }
        return true;
    }
}

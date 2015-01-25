package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("deprecation")
public class KickCommand extends RequireIslandCommand {
    public KickCommand(uSkyBlock plugin) {
        super(plugin, "kick|remove", "usb.party.kick", "player", "remove a member from your island.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            if (island == null || !island.hasPerm(player, "canKickOthers")) {
                player.sendMessage("\u00a74You do not have permission to kick others from this island!");
                return true;
            }
            String playerName = args[0];
            Player otherPlayer = Bukkit.getPlayer(playerName);
            if (otherPlayer == null && Bukkit.getOfflinePlayer(playerName) == null) {
                player.sendMessage("\u00a74That player doesn't exist.");
                return true;
            }
            if (!island.isParty()) {
                player.sendMessage("\u00a74No one else is on your island, are you seeing things?");
                return true;
            }
            if (island.isLeader(playerName)) {
                player.sendMessage("\u00a74You can't remove the leader from the Island!");
                return true;
            }
            if (player.getName().equalsIgnoreCase(playerName)) {
                player.sendMessage("\u00a74Stop kickin' yourself!");
                return true;
            }
            if (island.getMembers().contains(playerName)) {
                if (otherPlayer != null) {
                    plugin.clearPlayerInventory(otherPlayer);
                    otherPlayer.sendMessage("\u00a74" + player.getName() + " has removed you from their island!");
                    plugin.spawnTeleport(otherPlayer);
                }
                if (Bukkit.getPlayer(island.getLeader()) != null) {
                    Bukkit.getPlayer(island.getLeader()).sendMessage("\u00a74" + playerName + " has been removed from the island.");
                }
                island.removeMember(plugin.getPlayerInfo(playerName));
                uSkyBlock.log(Level.INFO, "Removing from " + island.getLeader() + "'s Island");
            } else {
                player.sendMessage("\u00a74That player is not part of your island group!");
            }
            return true;
        }
        return false;
    }
}

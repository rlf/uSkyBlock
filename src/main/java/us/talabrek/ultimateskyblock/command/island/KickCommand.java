package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

@SuppressWarnings("deprecation")
public class KickCommand extends RequireIslandCommand {
    public KickCommand(uSkyBlock plugin) {
        super(plugin, "kick|remove", "usb.party.kick", "player", tr("remove a member from your island."));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            if (island == null || !island.hasPerm(player, "canKickOthers")) {
                player.sendMessage(tr("\u00a74You do not have permission to kick others from this island!"));
                return true;
            }
            String playerName = args[0];
            Player otherPlayer = Bukkit.getPlayer(playerName);
            if (island.isLeader(playerName)) {
                player.sendMessage(tr("\u00a74You can't remove the leader from the Island!"));
                return true;
            }
            if (player.getName().equalsIgnoreCase(playerName)) {
                player.sendMessage(tr("\u00a74Stop kickin' yourself!"));
                return true;
            }
            if (island.getMembers().contains(playerName)) {
                if (otherPlayer != null) {
                    plugin.clearPlayerInventory(otherPlayer);
                    otherPlayer.sendMessage(tr("\u00a74" + player.getName() + " has removed you from their island!"));
                    plugin.spawnTeleport(otherPlayer);
                }
                if (Bukkit.getPlayer(island.getLeader()) != null) {
                    Bukkit.getPlayer(island.getLeader()).sendMessage(tr("\u00a74{0} has been removed from the island.", playerName));
                }
                island.removeMember(plugin.getPlayerInfo(playerName));
                uSkyBlock.log(Level.INFO, "Removing from " + island.getLeader() + "'s Island");
            } else if (otherPlayer != null && plugin.locationIsOnIsland(player, otherPlayer.getLocation())) {
                plugin.spawnTeleport(otherPlayer);
                otherPlayer.sendMessage(tr("\u00a74" + player.getName() + " has kicked you from their island!"));
                player.sendMessage(tr("\u00a74{0} has been kicked from the island.", playerName));
            } else {
                player.sendMessage(tr("\u00a74That player is not part of your island group, and not on your island!"));
            }
            return true;
        }
        return false;
    }
}

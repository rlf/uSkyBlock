package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.util.Map;

@SuppressWarnings("deprecation")
public class KickCommand extends RequireIslandCommand {
    public KickCommand(uSkyBlock plugin) {
        super(plugin, "kick|remove", "usb.party.kick", "player", I18nUtil.tr("remove a member from your island."));
    }

    @Override
    protected boolean doExecute(String alias, final Player player, PlayerInfo pi, final IslandInfo island, Map<String, Object> data, final String... args) {
        if (args.length == 1) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    if (island == null || !island.hasPerm(player, "canKickOthers")) {
                        player.sendMessage(I18nUtil.tr("\u00a74You do not have permission to kick others from this island!"));
                        return;
                    }
                    String targetPlayerName = args[0];
                    if (island.isLeader(targetPlayerName)) {
                        player.sendMessage(I18nUtil.tr("\u00a74You can't remove the leader from the Island!"));
                        return;
                    }
                    if (player.getName().equalsIgnoreCase(targetPlayerName)) {
                        player.sendMessage(I18nUtil.tr("\u00a74Stop kickin' yourself!"));
                        return;
                    }
                    
                    Player onlineTargetPlayer = Bukkit.getPlayer(targetPlayerName);
                    
                    if (island.getMembers().contains(targetPlayerName)) {
                        PlayerInfo targetPlayerInfo = plugin.getPlayerInfo(targetPlayerName);
                        boolean isOnIsland = false;
                        if (targetPlayerInfo == null) {
                            targetPlayerInfo = plugin.getPlayerLogic().loadPlayerData(targetPlayerName);
                        }
                        if (onlineTargetPlayer != null && onlineTargetPlayer.isOnline()) {
                            onlineTargetPlayer.sendMessage(I18nUtil.tr("\u00a74{0} has removed you from their island!", player.getDisplayName()));
                            isOnIsland = plugin.playerIsOnIsland(onlineTargetPlayer);
                        }
                        if (Bukkit.getPlayer(island.getLeader()) != null) {
                            Bukkit.getPlayer(island.getLeader()).sendMessage(I18nUtil.tr("\u00a74{0} has been removed from the island.", targetPlayerName));
                        }
                        island.removeMember(targetPlayerInfo);
                        if (isOnIsland && onlineTargetPlayer.isOnline()) {
                            plugin.spawnTeleport(onlineTargetPlayer, true);
                        }
                    } else if (onlineTargetPlayer != null && onlineTargetPlayer.isOnline() && plugin.locationIsOnIsland(player, onlineTargetPlayer.getLocation())) {
                        onlineTargetPlayer.sendMessage(I18nUtil.tr("\u00a74" + player.getName() + " has kicked you from their island!"));
                        player.sendMessage(I18nUtil.tr("\u00a74{0} has been kicked from the island.", targetPlayerName));
                        plugin.spawnTeleport(onlineTargetPlayer, true);
                    } else {
                        player.sendMessage(I18nUtil.tr("\u00a74That player is not part of your island group, and not on your island!"));
                    }
                }
            });
            return true;
        }
        return false;
    }
}

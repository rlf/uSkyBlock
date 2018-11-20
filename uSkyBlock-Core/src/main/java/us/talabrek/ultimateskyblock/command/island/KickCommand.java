package us.talabrek.ultimateskyblock.command.island;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

@SuppressWarnings("deprecation")
public class KickCommand extends RequireIslandCommand {
    public KickCommand(uSkyBlock plugin) {
        super(plugin, "kick|remove", "usb.party.kick", "player", marktr("remove a member from your island."));
    }

    @Override
    protected boolean doExecute(String alias, final Player player, PlayerInfo pi, final IslandInfo island, Map<String, Object> data, final String... args) {
        if (args.length == 1) {
            if (island == null || !island.hasPerm(player, "canKickOthers")) {
                player.sendMessage(I18nUtil.tr("\u00a74You do not have permission to kick others from this island!"));
                return true;
            }
            String targetPlayerName = args[0];
            if (island.isLeader(targetPlayerName)) {
                player.sendMessage(I18nUtil.tr("\u00a74You can't remove the leader from the Island!"));
                return true;
            }
            if (player.getName().equalsIgnoreCase(targetPlayerName)) {
                player.sendMessage(I18nUtil.tr("\u00a74Stop kickin' yourself!"));
                return true;
            }

            Player onlineTargetPlayer = Bukkit.getPlayer(targetPlayerName);

            if (island.getMembers().contains(targetPlayerName)) {
                PlayerInfo targetPlayerInfo = plugin.getPlayerInfo(targetPlayerName);
                boolean isOnIsland = false;
                if (onlineTargetPlayer != null && onlineTargetPlayer.isOnline()) {
                    onlineTargetPlayer.sendMessage(I18nUtil.tr("\u00a74{0} has removed you from their island!", player.getDisplayName()));
                    isOnIsland = plugin.playerIsOnIsland(onlineTargetPlayer);
                }
                if (Bukkit.getPlayer(island.getLeader()) != null) {
                    Bukkit.getPlayer(island.getLeader()).sendMessage(I18nUtil.tr("\u00a74{0} has been removed from the island.", targetPlayerName));
                }
                island.removeMember(targetPlayerInfo);
                if (isOnIsland && onlineTargetPlayer.isOnline()) {
                    plugin.getTeleportLogic().spawnTeleport(onlineTargetPlayer, true);
                }
            } else if (onlineTargetPlayer != null
                    && onlineTargetPlayer.isOnline()
                    && (plugin.locationIsOnIsland(player, onlineTargetPlayer.getLocation())
                    || plugin.locationIsOnNetherIsland(player, onlineTargetPlayer.getLocation()))
                    ) {
                if (hasPermission(onlineTargetPlayer, "usb.exempt.kick")) {
                    onlineTargetPlayer.sendMessage(I18nUtil.tr("\u00a74{0} tried to kick you from their island!",  player.getName()));
                    player.sendMessage(I18nUtil.tr("\u00a74{0} is exempt from being kicked.", targetPlayerName));
                    return true;
                }
                onlineTargetPlayer.sendMessage(I18nUtil.tr("\u00a74{0} has kicked you from their island!",  player.getName()));
                player.sendMessage(I18nUtil.tr("\u00a74{0} has been kicked from the island.", targetPlayerName));
                plugin.getTeleportLogic().spawnTeleport(onlineTargetPlayer, true);
            } else {
                player.sendMessage(I18nUtil.tr("\u00a74That player is not part of your island group, and not on your island!"));
            }
            return true;
        }
        return false;
    }
}

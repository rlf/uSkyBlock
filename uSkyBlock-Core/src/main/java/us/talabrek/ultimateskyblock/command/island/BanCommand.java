package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;
import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class BanCommand extends RequireIslandCommand {
    public BanCommand(uSkyBlock plugin) {
        super(plugin, "ban|unban", "usb.island.ban", "player", marktr("ban/unban a player from your island."));
        addFeaturePermission("usb.exempt.ban", tr("exempts user from being banned"));
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
            if (!island.hasPerm(player, "canKickOthers")) {
                player.sendMessage(tr("\u00a74You do not have permission to kick/ban players."));
                return true;
            }
            if (!island.isBanned(name)) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                if (offlinePlayer == null) {
                    player.sendMessage(tr("\u00a7eUnable to ban unknown player {0}", name));
                    return true;
                }
                if (offlinePlayer.isOnline() && hasPermission(offlinePlayer.getPlayer(), "usb.exempt.ban")) {
                    offlinePlayer.getPlayer().sendMessage(tr("\u00a74{0} tried to ban you from their island!", player.getName()));
                    player.sendMessage(tr("\u00a74{0} is exempt from being banned.", name));
                    return true;
                }
                island.banPlayer(offlinePlayer.getUniqueId());
                player.sendMessage(tr("\u00a7eYou have banned \u00a74{0}\u00a7e from warping to your island.", name));
                if (offlinePlayer != null && offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(tr("\u00a7eYou have been \u00a7cBANNED\u00a7e from {0}\u00a7e''s island.", player.getDisplayName()));
                    if (plugin.locationIsOnIsland(player, offlinePlayer.getPlayer().getLocation())) {
                        plugin.spawnTeleport(offlinePlayer.getPlayer(), true);
                    }
                }
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                if (offlinePlayer == null) {
                    player.sendMessage(tr("\u00a7eUnable to ban unknown player {0}", name));
                    return true;
                }
                island.unbanPlayer(offlinePlayer.getUniqueId());
                player.sendMessage(tr("\u00a7eYou have unbanned \u00a7a{0}\u00a7e from warping to your island.", name));
                if (offlinePlayer != null && offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(tr("\u00a7eYou have been \u00a7aUNBANNED\u00a7e from {0}\u00a7e''s island.", player.getDisplayName()));
                }
            }
            WorldGuardHandler.updateRegion(island);
            return true;
        }
        return false;
    }
}

package us.talabrek.ultimateskyblock.command.island;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.api.IslandRank;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandScore;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.perm.PermissionUtil.hasPermission;
import static dk.lockfuglsang.minecraft.po.I18nUtil.*;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class LevelCommand extends RequireIslandCommand {
    public LevelCommand(uSkyBlock plugin) {
        super(plugin, "level", "usb.island.level", "?island", tr("check your or anothers island level"));
        addFeaturePermission("usb.island.level.other", tr("allows user to query for others levels"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (!Settings.island_useIslandLevel) {
            player.sendMessage(tr("\u00a74Island level has been disabled, contact an administrator."));
            return true;
        }
        if (args.length == 0) {
            if (!plugin.playerIsOnIsland(player)) {
                player.sendMessage(tr("\u00a7eYou must be on your island to use this command."));
                return true;
            }
            if (!island.isParty() && !pi.getHasIsland()) {
                player.sendMessage(tr("\u00a74You do not have an island!"));
            } else {
                getIslandLevel(player, player.getName(), alias);
            }
            return true;
        } else if (args.length == 1) {
            if (hasPermission(player, "usb.island.level.other")) {
                getIslandLevel(player, args[0], alias);
            } else {
                player.sendMessage(tr("\u00a74You do not have access to that command!"));
            }
            return true;
        }
        return false;
    }

    public boolean getIslandLevel(final Player player, final String islandPlayer, final String cmd) {
        final PlayerInfo info = plugin.getPlayerInfo(islandPlayer);
        if (info == null || !info.getHasIsland()) {
            player.sendMessage(tr("\u00a74That player is invalid or does not have an island!"));
            return false;
        }
        final us.talabrek.ultimateskyblock.api.IslandInfo islandInfo = plugin.getIslandInfo(info);
        if (islandInfo == null || islandInfo.getIslandLocation() == null) {
            player.sendMessage(tr("\u00a74That player is invalid or does not have an island!"));
            return false;
        }
        final boolean shouldRecalculate = player.getName().equals(info.getPlayerName()) || hasPermission(player, "usb.admin.island");
        final Runnable showInfo = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isOnline() && info != null) {
                    player.sendMessage(tr("\u00a7eInformation about {0}'s Island:", islandPlayer));
                    if (cmd.equalsIgnoreCase("level")) {
                        IslandRank rank = plugin.getIslandLogic().getRank(info.locationForParty());
                        player.sendMessage(new String[]{
                                tr("\u00a7aIsland level is {0,number,###.##}", rank.getScore()),
                                tr("\u00a79Rank is {0}", rank.getRank())
                        });
                    }
                }
            }
        };
        if (shouldRecalculate) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.calculateScoreAsync(player, info.locationForParty(), new Callback<IslandScore>() {
                        @Override
                        public void run() {
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, showInfo, 10L);
                        }
                    });
                }
            }, 1L);
        } else {
            showInfo.run();
        }
        return true;
    }
}

package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.BlockScore;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.IslandScore;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class InfoCommand extends RequireIslandCommand {
    public InfoCommand(uSkyBlock plugin) {
        super(plugin, "info", "usb.island.info", "?island", "check your or anothers island info");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (!Settings.island_useIslandLevel) {
            player.sendMessage(tr("\u00a74Island level has been disabled, contact an administrator."));
            return true;
        }
        if (args.length == 0 || (args.length == 1 && args[0].matches("\\d*"))) {
            if (!plugin.playerIsOnIsland(player)) {
                player.sendMessage(tr("\u00a7eYou must be on your island to use this command."));
                return true;
            }
            if (!plugin.onInfoCooldown(player) || Settings.general_cooldownInfo == 0) {
                plugin.setInfoCooldown(player);
                if (!island.isParty() && !pi.getHasIsland()) {
                    player.sendMessage(tr("\u00a74You do not have an island!"));
                } else {
                    getIslandInfo(player, player.getName(), alias, args.length == 1 ? Integer.parseInt(args[0], 10) : 1);
                }
                return true;
            }
            player.sendMessage(tr("\u00a7eYou can use that command again in {0,number,##.#} seconds.", plugin.getInfoCooldownTime(player) / 1000L));
            return true;
        } else if (args.length == 1 || (args.length == 2 && args[1].matches("\\d*"))) {
            if (player.hasPermission("usb.island.info.other") && (!plugin.onInfoCooldown(player) || Settings.general_cooldownInfo == 0)) {
                plugin.setInfoCooldown(player);
                getIslandInfo(player, args[0], alias, args.length == 2 ? Integer.parseInt(args[1], 10) : 1);
            } else {
                player.sendMessage(tr("\u00a74You do not have access to that command!"));
            }
            return true;
        }
        return false;
    }

    public boolean getIslandInfo(final Player player, final String islandPlayer, final String cmd, final int page) {
        PlayerInfo info = plugin.getPlayerInfo(islandPlayer);
        if (info == null || !info.getHasIsland()) {
            player.sendMessage(tr("\u00a74That player is invalid or does not have an island!"));
            return false;
        }
        final PlayerInfo playerInfo = islandPlayer.equals(player.getName()) ? plugin.getPlayerInfo(player) : plugin.getPlayerInfo(islandPlayer);
        plugin.getIslandLogic().loadIslandChunks(playerInfo.getIslandLocation(), Settings.island_radius);
        final Callback<IslandScore> showInfo = new Callback<IslandScore>() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    int maxPage = ((getState().getSize()-1) / 10) + 1;
                    int currentPage = page;
                    if (currentPage < 1) {
                        currentPage = 1;
                    }
                    if (currentPage > maxPage) {
                        currentPage = maxPage;
                    }
                    player.sendMessage(tr("\u00a7eBlocks on {0}s Island (page {1,number} of {2,number}):", islandPlayer, currentPage, maxPage));
                    if (cmd.equalsIgnoreCase("info") && getState() != null) {
                        player.sendMessage(tr("Score Count Block"));
                        for (BlockScore score : getState().getTop((currentPage-1)*10, 10)) {
                            player.sendMessage(score.getState().getColor() + String.format("%05.2f  %d %s",
                                    score.getScore(), score.getCount(),
                                    VaultHandler.getItemName(score.getBlock())));
                        }
                        player.sendMessage(String.format("\u00a7aIsland level is %5.2f", getState().getScore()));
                    }
                }
            }
        };
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.calculateScoreAsync(player, playerInfo.locationForParty(), showInfo);
                } catch (Exception e) {
                    uSkyBlock.log(Level.SEVERE, "Error while calculating Island Level", e);
                }
            }
        }, 1L);
        return true;
    }


}
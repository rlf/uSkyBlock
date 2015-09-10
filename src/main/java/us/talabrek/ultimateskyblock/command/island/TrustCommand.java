package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class TrustCommand extends RequireIslandCommand {
    public TrustCommand(uSkyBlock plugin) {
        super(plugin, "trust|untrust", "usb.island.trust", "player", tr("trust/untrust a player to help on your island."));
    }

    @Override
    protected boolean doExecute(final String alias, final Player player, PlayerInfo pi, final IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage(tr("\u00a7eThe following players are trusted on your island:"));
            player.sendMessage(tr("\u00a74{0}", island.getTrustees()));
            player.sendMessage(tr("\u00a7eTo trust/untrust from your island, use /island trust <player>"));
            return true;
        } else if (args.length == 1) {
            final String name = args[0];
            if (island.getMembers().contains(name)) {
                player.sendMessage(tr("\u00a74Members are already trusted!"));
                return true;
            }
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                    if (offlinePlayer == null) {
                        player.sendMessage(tr("\u00a74Unknown player {0}", name));
                        return;
                    }
                    if (alias.equals("trust")) {
                        island.trust(name);
                        player.sendMessage(tr("\u00a7eYou have trusted \u00a74{0}\u00a7e on your island.", name));
                        if (offlinePlayer.isOnline()) {
                            offlinePlayer.getPlayer().sendMessage(tr("\u00a7eYou are now trusted on \u00a74{0}s island.", name));
                        }
                    } else {
                        island.untrust(name);
                        player.sendMessage(tr("\u00a7eYou have revoked your trust in \u00a7a{0}\u00a7e on your island.", name));
                        if (offlinePlayer.isOnline()) {
                            offlinePlayer.getPlayer().sendMessage(tr("\u00a7eYou are no longer trusted on \u00a74{0}s island.", name));
                        }
                    }
                    WorldGuardHandler.updateRegion(player, island);
                }
            });
            return true;
        }
        return false;
    }
}

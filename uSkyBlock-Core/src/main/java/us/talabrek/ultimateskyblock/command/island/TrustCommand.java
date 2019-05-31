package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class TrustCommand extends RequireIslandCommand {
    public TrustCommand(uSkyBlock plugin) {
        super(plugin, "trust|untrust", "usb.island.trust", "?player", marktr("trust/untrust a player to help on your island."));
    }

    @Override
    protected boolean doExecute(final String alias, final Player player, final PlayerInfo pi, final IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            player.sendMessage(tr("\u00a7eThe following players are trusted on your island:"));
            player.sendMessage(tr("\u00a74{0}", island.getTrustees()));
            player.sendMessage(tr("\u00a7eThe following leaders trusts you:"));
            player.sendMessage(tr("\u00a74{0}", getLeaderNames(pi)));
            player.sendMessage(tr("\u00a7eTo trust/untrust from your island, use /island trust <player>"));
            return true;
        } else if (args.length == 1) {
            final String name = args[0];
            if (island.getMembers().contains(name)) {
                player.sendMessage(tr("\u00a74Members are already trusted!"));
                return true;
            }
            //noinspection deprecation
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                player.sendMessage(tr("\u00a74Unknown player {0}", name));
                return true;
            }
            if (alias.equals("trust")) {
                island.trustPlayer(offlinePlayer, player);
                if (offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(tr("\u00a7eYou are now trusted on \u00a74{0}''s \u00a7eisland.", pi.getDisplayName()));
                }
                island.sendMessageToIslandGroup(true, marktr("\u00a7a{0} trusted {1} on the island"), player.getName(), name);
            } else {
                island.untrustPlayer(offlinePlayer, player);
                if (offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(tr("\u00a7eYou are no longer trusted on \u00a74{0}''s \u00a7eisland.", pi.getDisplayName()));
                }
                island.sendMessageToIslandGroup(true, marktr("\u00a7c{0} revoked trust in {1} on the island"), player.getName(), name);
            }
            WorldGuardHandler.updateRegion(island);
            return true;
        }
        return false;
    }

    private List<String> getLeaderNames(PlayerInfo pi) {
        List<String> trustedOn = pi.getTrustedOn();
        List<String> leaderNames = new ArrayList<>();
        for (String islandName : trustedOn) {
            us.talabrek.ultimateskyblock.api.IslandInfo islandInfo = plugin.getIslandInfo(islandName);
            if (islandInfo != null && islandInfo.getLeader() != null) {
                leaderNames.add(islandInfo.getLeader());
            }
        }
        return leaderNames;
    }
}

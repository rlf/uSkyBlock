package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class WarpCommand extends RequirePlayerCommand {

    private final uSkyBlock plugin;

    public WarpCommand(uSkyBlock plugin) {
        super("warp|w", "usb.island.warp", "island", "warp to another player's island");
        this.plugin = plugin;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            IslandInfo island = plugin.getIslandInfo(player);
            if (island != null && VaultHandler.checkPerk(player.getName(), "usb.extra.addwarp", player.getWorld())) {
                if (island.hasWarp()) {
                    player.sendMessage(tr("\u00a7aYour incoming warp is active, players may warp to your island."));
                } else {
                    player.sendMessage(tr("\u00a74Your incoming warp is inactive, players may not warp to your island."));
                }
                player.sendMessage(tr("\u00a7fSet incoming warp to your current location using \u00a7e/island setwarp"));
                player.sendMessage(tr("\u00a7fToggle your warp on/off using \u00a7e/island togglewarp"));
            } else {
                player.sendMessage(tr("\u00a74You do not have permission to create a warp on your island!"));
            }
            if (VaultHandler.checkPerk(player.getName(), "usb.island.warp", player.getWorld())) {
                player.sendMessage(tr("\u00a7fWarp to another island using \u00a7e/island warp <player>"));
            } else {
                player.sendMessage(tr("\u00a74You do not have permission to warp to other islands!"));
            }
            return true;
        } else if (args.length == 1) {
            if (VaultHandler.checkPerk(player.getName(), "usb.island.warp", player.getWorld())) {
                PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
                if (playerInfo == null || !playerInfo.getHasIsland()) {
                    player.sendMessage(tr("\u00a74That player does not exist!"));
                    return true;
                }
                IslandInfo island = plugin.getIslandInfo(playerInfo);
                if (!island.hasWarp()) {
                    player.sendMessage(tr("\u00a74That player does not have an active warp."));
                    return true;
                }
                if (!island.isBanned(player)) {
                    plugin.warpTeleport(player, playerInfo, false);
                } else {
                    player.sendMessage(tr("\u00a74That player has forbidden you from warping to their island."));
                }
            } else {
                player.sendMessage(tr("\u00a74You do not have permission to warp to other islands!"));
            }
            return true;
        }
        return false;
    }
}

package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class WarpCommand extends RequirePlayerCommand {

    private final uSkyBlock plugin;

    public WarpCommand(uSkyBlock plugin) {
        super("warp|w", "usb.island.warp", "island", marktr("warp to another player''s island"));
        this.plugin = plugin;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            IslandInfo island = plugin.getIslandInfo(player);
            if (island != null && hasPermission(player, "usb.island.setwarp")) {
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
            if (hasPermission(player, "usb.island.warp")) {
                player.sendMessage(tr("\u00a7fWarp to another island using \u00a7e/island warp <player>"));
            } else {
                player.sendMessage(tr("\u00a74You do not have permission to warp to other islands!"));
            }
            return true;
        } else if (args.length == 1) {
            if (hasPermission(player, "usb.island.warp")) {
                PlayerInfo senderPlayerInfo = plugin.getPlayerInfo(player);
                if (senderPlayerInfo.isIslandGenerating()) {
                    player.sendMessage(tr("\u00a7cYour island is in the process of generating, you cannot warp to other players islands right now."));
                    return true;
                }

                PlayerInfo targetPlayerInfo = plugin.getPlayerInfo(args[0]);
                if (targetPlayerInfo == null || !targetPlayerInfo.getHasIsland()) {
                    player.sendMessage(tr("\u00a74That player does not exist!"));
                    return true;
                }
                IslandInfo island = plugin.getIslandInfo(targetPlayerInfo);
                if (island == null || (!island.hasWarp() && !island.getTrustees().contains(player.getName()))) {
                    player.sendMessage(tr("\u00a74That player does not have an active warp."));
                    return true;
                }
                if (targetPlayerInfo.isIslandGenerating()) {
                    player.sendMessage(tr("\u00a7cThat players island is in the process of generating, you cannot warp to it right now."));
                    return true;
                }
                if (!island.isBanned(player)) {
                    if (plugin.getConfig().getBoolean("options.protection.visitors.warn-on-warp", true)) {
                        island.sendMessageToOnlineMembers(tr("\u00a7cWARNING: \u00a79{0}\u00a7e is warping to your island!", player.getDisplayName()));
                    }
                    plugin.warpTeleport(player, targetPlayerInfo, false);
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

package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Teleports to the player's island.
 */
public class GotoIslandCommand extends AbstractPlayerInfoCommand {
    private final uSkyBlock plugin;

    public GotoIslandCommand(uSkyBlock plugin) {
        super("goto", "usb.mod.goto", I18nUtil.tr("teleport to another players island"));
        this.plugin = plugin;
    }

    @Override
    protected void doExecute(final CommandSender sender, final PlayerInfo playerInfo) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18nUtil.tr("\u00a74Only supported for players"));
        }
        final Player player = (Player) sender;
        if (playerInfo.getHomeLocation() != null) {
            sender.sendMessage(tr("\u00a7aTeleporting to {0}s island.", playerInfo.getPlayerName()));
            plugin.safeTeleport(player, playerInfo.getHomeLocation(), true);
        } else if (playerInfo.getIslandLocation() != null) {
            sender.sendMessage(tr("\u00a7aTeleporting to {0}s island.", playerInfo.getPlayerName()));
            plugin.safeTeleport(player, playerInfo.getIslandLocation(), true);
        } else {
            sender.sendMessage(I18nUtil.tr("\u00a74That player does not have an island!"));
        }
    }
}
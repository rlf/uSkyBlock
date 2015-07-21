package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Teleports to the player's island.
 */
public class GotoIslandCommand extends AbstractAsyncPlayerInfoCommand {
    private final uSkyBlock plugin;

    public GotoIslandCommand(uSkyBlock plugin) {
        super("goto", "usb.mod.goto", tr("Teleport to another players island"));
        this.plugin = plugin;
    }

    @Override
    protected void doExecute(final CommandSender sender, final PlayerInfo playerInfo) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("\u00a74Only supported for players"));
        }
        Player player = (Player) sender;
        if (playerInfo.getHomeLocation() != null) {
            sender.sendMessage(tr("\u00a7aTeleporting to {0}'s island.", playerInfo.getPlayerName()));

            Location homeLocation = playerInfo.getHomeLocation();
            if (!homeLocation.getWorld().isChunkLoaded(homeLocation.getBlockX() >> 4, homeLocation.getBlockZ() >> 4)) {
                homeLocation.getWorld().loadChunk(homeLocation.getBlockX() >> 4, homeLocation.getBlockZ() >> 4);
            }
            player.teleport(homeLocation);
            return;
        }
        if (playerInfo.getIslandLocation() != null) {
            sender.sendMessage(tr("\u00a7aTeleporting to {0}'s island.", playerInfo.getPlayerName()));
            Location islandLocation = playerInfo.getIslandLocation();
            if (!islandLocation.getWorld().isChunkLoaded(islandLocation.getBlockX() >> 4, islandLocation.getBlockZ() >> 4)) {
                islandLocation.getWorld().loadChunk(islandLocation.getBlockX() >> 4, islandLocation.getBlockZ() >> 4);
            }
            player.teleport(islandLocation);
            return;
        }
        sender.sendMessage(tr("\u00a74That player does not have an island!"));
    }
}
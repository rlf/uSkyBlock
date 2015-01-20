package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Tries to clear an area of flatland.
 */
public class FlatlandFixCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public FlatlandFixCommand(uSkyBlock plugin) {
        super("fix-flatland", "usb.admin.remove", "?player", "tries to fix the the area of flatland.");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        String playerName = null;
        if (args.length == 1) {
            playerName = args[0];
        } else if (args.length == 0 && sender instanceof Player) {
            playerName = WorldGuardHandler.getIslandNameAt(((Player) sender).getLocation());
        }
        PlayerInfo playerInfo = playerName != null ? plugin.getPlayerInfo(playerName) : null;
        if (!tryFlatlandFix(sender, playerName, playerInfo)) {
            sender.sendMessage("\u00a74No valid island found");
        }
        return true;
    }

    private boolean tryFlatlandFix(CommandSender sender, String playerName, PlayerInfo playerInfo) {
        if (playerInfo.getHasIsland() && playerInfo.getIslandLocation() != null) {
            // TODO: 29/12/2014 - R4zorax: Load chunks first?
            if (!plugin.getIslandLogic().clearFlatland(sender, playerInfo.getIslandLocation(), 0)) {
                sender.sendMessage("\u00a74No flatland detected at " + playerName + "'s island!");
            }
            return true;
        }
        return false;
    }
}

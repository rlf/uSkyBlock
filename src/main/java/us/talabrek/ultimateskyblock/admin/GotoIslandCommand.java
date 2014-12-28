package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Teleports to the player's island.
 */
public class GotoIslandCommand extends AbstractPlayerInfoCommand {
    private final uSkyBlock plugin;

    public GotoIslandCommand(uSkyBlock plugin) {
        super("goto", "usb.mod.goto", "Teleport to another players island");
        this.plugin = plugin;
    }

    @Override
    protected void doExecute(CommandSender sender, PlayerInfo playerInfo) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only supported for players");
        }
        Player player = (Player) sender;
        if (playerInfo.getHomeLocation() != null) {
            sender.sendMessage(ChatColor.GREEN + "Teleporting to " + playerInfo.getPlayerName() + "'s island.");
            player.teleport(playerInfo.getHomeLocation());
            return;
        }
        if (playerInfo.getIslandLocation() != null) {
            sender.sendMessage(ChatColor.GREEN + "Teleporting to " + playerInfo.getPlayerName() + "'s island.");
            player.teleport(playerInfo.getIslandLocation());
            return;
        }
        sender.sendMessage(ChatColor.RED + "That player does not have an island!");
    }
}

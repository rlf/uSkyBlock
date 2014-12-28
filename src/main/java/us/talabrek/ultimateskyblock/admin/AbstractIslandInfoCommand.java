package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Command that lookup island info given the player name.
 */
public abstract class AbstractIslandInfoCommand extends AbstractPlayerInfoCommand {

    protected AbstractIslandInfoCommand(String name, String permission, String description) {
        super(name, permission, description);
    }

    protected abstract void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args);

    @Override
    protected final void doExecute(CommandSender sender, PlayerInfo playerInfo) {
        // Not used
    }

    @Override
    public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
        if (super.execute(sender, data, args)) {
            PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
            if (playerInfo != null) {
                IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(playerInfo);
                if (islandInfo != null && args.length > 0) {
                    String[] subArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                    doExecute(sender, playerInfo, islandInfo, subArgs);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Player " + playerInfo.getPlayerName() + " has no island!");
                }
            }
        }
        return false;
    }
}

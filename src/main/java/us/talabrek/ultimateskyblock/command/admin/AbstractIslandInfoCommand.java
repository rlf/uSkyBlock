package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
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
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (super.execute(sender, alias, data, args)) {
            PlayerInfo playerInfo = (PlayerInfo) data.get("playerInfo");
            if (playerInfo != null) {
                IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(playerInfo);
                if (islandInfo != null && args.length > 0) {
                    String[] subArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                    doExecute(sender, playerInfo, islandInfo, subArgs);
                    return true;
                } else {
                    sender.sendMessage("\u00a7ePlayer " + playerInfo.getPlayerName() + " has no island!");
                }
            }
        } else if (sender instanceof Player && WorldGuardHandler.getIslandNameAt(((Player) sender).getLocation()) != null) {
            IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(WorldGuardHandler.getIslandNameAt(((Player) sender).getLocation()));
            if (islandInfo != null) {
                doExecute(sender, null, islandInfo, args);
                return true;
            }
        }
        return false;
    }
}

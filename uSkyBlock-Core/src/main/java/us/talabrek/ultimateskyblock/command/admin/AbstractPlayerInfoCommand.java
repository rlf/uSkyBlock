package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command that has <code>player</code> as first argument, and uses playerInfo.
 */
public abstract class AbstractPlayerInfoCommand extends AbstractCommand {
    protected AbstractPlayerInfoCommand(String name, String permission, String description) {
        super(name, permission, "player", description);
    }

    protected abstract void doExecute(CommandSender sender, PlayerInfo playerInfo);

    @Override
    public boolean execute(final CommandSender sender, String alias, final Map<String, Object> data, final String... args) {
        if (args.length > 0) {
            String playerName = args[0];
            PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(playerName);
            if (playerInfo != null) {
                data.put("playerInfo", playerInfo);
                doExecute(sender, playerInfo);
                return true;
            }
            sender.sendMessage(tr("\u00a7eInvalid player {0} supplied.", args[0]));
        }
        return false;
    }
}

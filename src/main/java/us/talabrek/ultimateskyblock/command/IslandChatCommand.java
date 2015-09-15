package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.AbstractCommandExecutor;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FormatUtil;

import java.util.List;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The chat command for party messages
 */
public class IslandChatCommand extends AbstractCommandExecutor {
    private final uSkyBlock plugin;

    public IslandChatCommand(uSkyBlock plugin) {
        super("islandtalk|istalk|it", "usb.party.talk", tr("talk to your party"));
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (args == null || args.length == 0) {
                return false;
            }
            IslandInfo islandInfo = plugin.getIslandInfo(player);
            if (islandInfo == null) {
                player.sendMessage(tr("\u00a74No Island. \u00a7eUse \u00a7b/is create\u00a7e to get one"));
                return true;
            }
            String message = args[0];
            for (int ix = 1; ix < args.length; ix++) {
                message += " " + args[ix]; // Java 8 has String.join - not guaranteed here
            }
            String format = plugin.getConfig().getString("options.party.chat-format", "&9{DISPLAYNAME} &f>&e {MESSAGE}");
            format = FormatUtil.normalize(format);
            format = format.replaceAll("\\{DISPLAYNAME\\}", player.getDisplayName());
            message = format.replaceAll("\\{MESSAGE\\}", message);
            List<Player> onlineMembers = islandInfo.getOnlineMembers();
            if (onlineMembers.size() <= 1) {
                player.sendMessage("\u00a7cSorry! \u00a9But you are ALLLLLLL ALOOOOONE!");
            } else {
                islandInfo.sendMessageToOnlineMembers(message);
            }
            return true;
        }
        return false;
    }
}

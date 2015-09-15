package us.talabrek.ultimateskyblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.AbstractCommandExecutor;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FormatUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The chat command for party messages
 */
public abstract class IslandChatCommand extends AbstractCommandExecutor {
    private final uSkyBlock plugin;
    private final List<String> ALONE = Arrays.asList(
            tr("But you are ALLLLLLL ALOOOOONE!"),
            tr("But you are Yelling in the wind!"),
            tr("But your fantasy friends are gone!"),
            tr("But you are Talking to your self!")
    );

    public IslandChatCommand(uSkyBlock plugin, String name, String permission, String description) {
        super(name, permission, description);
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
            String format = getFormat();
            format = FormatUtil.normalize(format);
            format = format.replaceAll("\\{DISPLAYNAME\\}", player.getDisplayName());
            message = format.replaceAll("\\{MESSAGE\\}", message);
            List<Player> onlineMembers = getRecipients(islandInfo);
            if (onlineMembers.size() <= 1) {
                player.sendMessage(tr("\u00a7cSorry! {0}",  "\u00a79" + ALONE.get(((int)Math.round(Math.random() * ALONE.size())) % ALONE.size())));
            } else {
                islandInfo.sendMessageToOnlineMembers(message);
            }
            return true;
        }
        return false;
    }

    protected abstract String getFormat();

    protected abstract List<Player> getRecipients(IslandInfo islandInfo);
}

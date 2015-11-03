package us.talabrek.ultimateskyblock.command;

import dk.lockfuglsang.minecraft.command.AbstractCommandExecutor;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FormatUtil;

import java.util.Arrays;
import java.util.List;

/**
 * The chat command for party messages
 */
public abstract class IslandChatCommand extends AbstractCommandExecutor {
    private final uSkyBlock plugin;
    private final List<String> ALONE = Arrays.asList(
            I18nUtil.tr("But you are ALLLLLLL ALOOOOONE!"),
            I18nUtil.tr("But you are Yelling in the wind!"),
            I18nUtil.tr("But your fantasy friends are gone!"),
            I18nUtil.tr("But you are Talking to your self!")
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
            String message = args[0];
            for (int ix = 1; ix < args.length; ix++) {
                message += " " + args[ix]; // Java 8 has String.join - not guaranteed here
            }
            String format = getFormat();
            format = FormatUtil.normalize(format);
            format = format.replaceAll("\\{DISPLAYNAME\\}", player.getDisplayName());
            message = format.replaceAll("\\{MESSAGE\\}", message);
            List<Player> onlineMembers = getRecipients(player, islandInfo);
            if (onlineMembers.size() <= 1) {
                player.sendMessage(I18nUtil.tr("\u00a7cSorry! {0}", "\u00a79" + ALONE.get(((int) Math.round(Math.random() * ALONE.size())) % ALONE.size())));
            } else {
                for (Player member : onlineMembers) {
                    member.sendMessage(message);
                }
            }
            return true;
        }
        return false;
    }

    protected abstract String getFormat();

    protected abstract List<Player> getRecipients(Player player, IslandInfo islandInfo);
}

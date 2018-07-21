package us.talabrek.ultimateskyblock.chat;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.BaseCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.api.event.IslandChatEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * The chat command for party messages
 */
public abstract class IslandChatCommand extends BaseCommandExecutor {
    private final uSkyBlock plugin;
    private final ChatLogic chatLogic;

    public IslandChatCommand(uSkyBlock plugin, ChatLogic chatLogic, String name, String permission, String description) {
        super(name, permission, "?message", description);
        this.plugin = plugin;
        this.chatLogic = chatLogic;
    }

    @Override
    public String getUsage() {
        return tr("Either send a message directly to your group, or toggle it on/off.");
    }

    @Override
    public boolean execute(CommandSender commandSender, String alias, Map<String, Object> data, String... args) {
        if (!plugin.isRequirementsMet(commandSender, this, args)) {
            return true;
        }
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            IslandChatEvent.Type type = this instanceof PartyTalkCommand ? IslandChatEvent.Type.PARTY : IslandChatEvent.Type.ISLAND;
            if (args == null || args.length == 0) {
                String chatType = type == IslandChatEvent.Type.PARTY
                        ? tr("party")
                        : tr("island");
                if (chatLogic.toggle(player, type)) {
                    player.sendMessage(tr("\u00a7cToggled chat to {0} \u00a7aON", chatType));
                    player.sendMessage(tr("\u00a7cRepeat \u00a79{0}\u00a7c to toggle it off", "/" + alias));
                } else {
                    player.sendMessage(tr("\u00a7aToggled chat \u00a7cOFF\u00a7a for {0}", chatType));
                }
                return true;
            } else if (args != null && args.length == 1 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))) {
                showUsage(commandSender, 1);
                return true;
            }
            String message = AbstractCommand.join(args);
            Bukkit.getServer().getPluginManager().callEvent(new IslandChatEvent(player, type, message));
        } else {
            commandSender.sendMessage(tr("\u00a7cCommand only available to players"));
        }
        return true;
    }
}

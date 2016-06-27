package us.talabrek.ultimateskyblock.command.challenge;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Complete Challenge Command
 */
public class ChallengeCompleteCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    public ChallengeCompleteCommand(uSkyBlock plugin) {
        super("complete|c", "usb.island.challenges", "challenge", tr("try to complete a challenge"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("\u00a7cCommand only available for players."));
            return false;
        }
        String challengeName = "";
        for (String arg : args) {
            challengeName += " " + arg;
        }
        plugin.getChallengeLogic().completeChallenge((Player) sender, challengeName.trim());
        return true;
    }
}

package us.talabrek.ultimateskyblock.command.completion;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

/**
 * Lists all registered challenges.
 */
public class ChallengeTabCompleter extends AbstractTabCompleter {
    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        return uSkyBlock.getInstance().getChallengeLogic().getAllChallengeNames();
    }
}

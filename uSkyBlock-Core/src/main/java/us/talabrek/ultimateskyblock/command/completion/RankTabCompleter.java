package us.talabrek.ultimateskyblock.command.completion;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.challenge.Rank;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;

/**
 * Rank name tab completer
 */
public class RankTabCompleter extends AbstractTabCompleter {
    private final uSkyBlock plugin;

    public RankTabCompleter(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        List<String> rankNames = new ArrayList<>();
        for (Rank rank : plugin.getChallengeLogic().getRanks()) {
            rankNames.add(stripFormatting(rank.getRankKey()));
        }
        return rankNames;
    }
}

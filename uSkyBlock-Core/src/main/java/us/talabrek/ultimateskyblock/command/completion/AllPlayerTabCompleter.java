package us.talabrek.ultimateskyblock.command.completion;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lists ALL players (both offline and online).
 */
public class AllPlayerTabCompleter extends AbstractTabCompleter {
    private final OnlinePlayerTabCompleter online;

    public AllPlayerTabCompleter(OnlinePlayerTabCompleter online) {
        this.online = online;
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        Set<String> allPlayers = new LinkedHashSet<>(online.getTabList(commandSender, term));
        // Fetching from player DB disabled -- see GH rlf/1211.
        //allPlayers.addAll(uSkyBlock.getInstance().getPlayerDB().getNames(term));
        return new ArrayList<>(allPlayers);
    }
}

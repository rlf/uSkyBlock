package us.talabrek.ultimateskyblock.command.completion;

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
    private static final long TIMEOUT = 1000*60*2; // Only generate list every 2 minutes...
    private final OnlinePlayerTabCompleter online;
    private List<String> islandPlayers;
    private long time;

    public AllPlayerTabCompleter(OnlinePlayerTabCompleter online) {
        this.online = online;
    }

    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        long now = System.currentTimeMillis();
        if (islandPlayers == null || now >= time + TIMEOUT) {
            islandPlayers = new ArrayList<>();
            for (String playerFile : uSkyBlock.getInstance().directoryPlayers.list()) {
                islandPlayers.add(playerFile.split("\\.")[0]);
            }
            time = now;
        }
        Set<String> allPlayers = new LinkedHashSet<>(online.getTabList(commandSender, term));
        allPlayers.addAll(islandPlayers);
        return new ArrayList<>(allPlayers);
    }
}

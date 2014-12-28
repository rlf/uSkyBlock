package us.talabrek.ultimateskyblock.command.completion;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists ALL players (both offline and online).
 */
public class AllPlayerTabCompleter extends AbstractTabCompleter {
    private static final long TIMEOUT = 1000*60*2; // Only generate list every 2 minutes...
    private List<String> playerList;
    private long time;
    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        long now = System.currentTimeMillis();
        if (playerList == null || now >= time + TIMEOUT) {
            playerList = new ArrayList<>();
            for (String playerFile : uSkyBlock.getInstance().directoryPlayers.list()) {
                playerList.add(playerFile.split("\\.")[0]);
            }
            time = now;
        }
        return playerList;
    }
}

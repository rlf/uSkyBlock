package us.talabrek.ultimateskyblock.island.task;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

/**
 * Recalculates the listed players island-score
 */
public class RecalculateTask implements IncrementalTask {
    private final uSkyBlock plugin;
    private final List<Player> players;
    private final int size;

    public RecalculateTask(uSkyBlock plugin, List<Player> players) {
        this.plugin = plugin;
        this.players = players;
        this.size = players.size();
    }

    @Override
    public boolean execute(Plugin bukkitPlugin, int offset, int length) {
        for (int i = 0; i < Math.min(players.size(), length); i++) {
            Player player = players.remove(0);
            PlayerInfo playerInfo = plugin.getPlayerInfo(player);
            if (playerInfo != null && playerInfo.getHasIsland() && plugin.playerIsOnIsland(player)) {
                plugin.recalculateScore(player, playerInfo);
            }
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return players.isEmpty();
    }
}

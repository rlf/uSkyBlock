package dk.lockfuglsang.minecraft.command.completion;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience implementation of a tab-completer for all online players
 */
public class OnlinePlayerTabCompleter extends AbstractTabCompleter {
    @Override
    protected List<String> getTabList(CommandSender commandSender, String term) {
        List<String> tabList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                tabList.add(player.getName());
            }
        }
        return tabList;
    }
}

package us.talabrek.ultimateskyblock.command.completion;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab completion for players.
 */
public class OnlinePlayerTabCompleter extends AbstractTabCompleter {
    @Override
    public List<String> getTabList(CommandSender commandSender, String term) {
        List<String> list = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            // Ignore hidden players:
            if (commandSender instanceof Player) {
                if (!((Player) commandSender).canSee(player)) {
                    continue;
                }
            }

            // Add player to tablist:
            list.add(player.getName());
        }
        return list;
    }
}

package us.talabrek.ultimateskyblock.command.completion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Common ancestor of the TabCompleters.
 * Uses the Template Pattern for sub-classes.
 */
public abstract class AbstractTabCompleter implements TabCompleter {

    abstract protected List<String> getTabList(CommandSender commandSender, String term);

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        String term = args.length > 0 ? args[args.length-1] : "";
        return filter(getTabList(commandSender, term), term);
    }

    public static List<String> filter(Collection<String> list, String prefix) {
        Set<String> filtered = new LinkedHashSet<>();
        if (list != null) {
            String lowerPrefix = prefix.toLowerCase();
            for (String test : list) {
                if (test.toLowerCase().startsWith(lowerPrefix)) {
                    filtered.add(test);
                }
            }
        }
        if (filtered.size() > 20) {
            return new ArrayList<>(filtered).subList(0, 20);
        }
        return new ArrayList<>(filtered);
    }
}

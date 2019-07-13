package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Sets data directly on the IslandInfo object
 */
public class GetIslandDataCommand extends AbstractIslandInfoCommand {
    private final TabCompleter tabCompleter;
    private final List<String> getterNames;

    public GetIslandDataCommand() {
        super("get", "usb.admin.get", marktr("advanced command for getting island-data"));
        getterNames = new ArrayList<>();
        for (Method m : IslandInfo.class.getDeclaredMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                String fieldName = m.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                getterNames.add(fieldName);
            }
        }
        tabCompleter = new ReflectionTabCompleter(getterNames);
    }

    @Override
    protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
        if (args.length == 1 && args[0].length() > 1) {
            String getName = "get" + args[0].substring(0,1).toUpperCase() + args[0].substring(1);
            try {
                Object value = IslandInfo.class.getMethod(getName).invoke(islandInfo);
                sender.sendMessage(tr("\u00a7eCurrent value for {0} is ''{1}''", args[0], value));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                sender.sendMessage(tr("\u00a7cUnable to get state for {0}", args[0]));
            }
        } else {
            sender.sendMessage(tr("\u00a7eValid fields are {0}", getterNames));
        }
    }

    @Override
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    private static class ReflectionTabCompleter extends AbstractTabCompleter {
        private final List<String> getterNames;

        public ReflectionTabCompleter(List<String> getterNames) {
            this.getterNames = getterNames;
        }

        @Override
        protected List<String> getTabList(CommandSender commandSender, String term) {
            return getterNames;
        }
    }
}

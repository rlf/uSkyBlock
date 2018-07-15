package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Sets data directly on the IslandInfo object
 */
public class SetIslandDataCommand extends AbstractIslandInfoCommand {
    private final TabCompleter tabCompleter;
    private final List<String> methodNames;

    public SetIslandDataCommand(uSkyBlock plugin) {
        super("set", "usb.admin.set", marktr("advanced command for setting island-data"));
        methodNames = new ArrayList<>();
        for (Method m : IslandInfo.class.getDeclaredMethods()) {
            if (m.getName().startsWith("set")
                    && !m.getName().startsWith("setup")
                    && m.getParameterTypes().length == 1
                    && (m.getParameterTypes()[0].isPrimitive()
                    || m.getParameterTypes()[0].isAssignableFrom(String.class)
                    || m.getParameterTypes()[0].isAssignableFrom(Double.class)
            )) {
                String fieldName = m.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                methodNames.add(fieldName);
            }
        }
        tabCompleter = new ReflectionTabCompleter(methodNames);
    }

    @Override
    protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
        if (args.length > 1 && args[0].length() > 1 && methodNames.contains(args[0])) {
            String methodName = "set" + args[0].substring(0, 1).toUpperCase() + args[0].substring(1);
            String stringValue = args.length > 1 ? args[1] : null;
            Method m = getMethod(methodName);
            if (m != null) {
                try {
                    Object value = getValue(m.getParameterTypes()[0], stringValue);
                    m.invoke(islandInfo, value);
                    sender.sendMessage(tr("\u00a7c{0} was set to ''{1}''", args[0], value));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    sender.sendMessage(tr("\u00a7cUnable to set field {0} to ''{1}''", args[0], stringValue));
                } catch (NumberFormatException e) {
                    sender.sendMessage(tr("\u00a7cUnable to set field {0} to ''{1}'', a number was expected", args[0], stringValue));
                }
            } else {
                sender.sendMessage(tr("\u00a7cInvalid field {0}", args[0]));
            }
        } else if (args.length == 1 && methodNames.contains(args[0])) {
            String methodName = "get" + args[0].substring(0, 1).toUpperCase() + args[0].substring(1);
            Method m = getMethod(methodName);
            if (m != null && m.getParameterTypes().length == 0) {
                try {
                    Object value = m.invoke(islandInfo);
                    sender.sendMessage(tr("\u00a7eCurrent value for {0} is ''{1}''", args[0], value));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    sender.sendMessage(tr("\u00a7cUnable to get state for {0}", args[0]));
                }
            } else {
                sender.sendMessage(tr("\u00a7cUnable to get state for {0}", args[0]));
            }
        } else {
            sender.sendMessage(tr("\u00a7eValid fields are {0}", methodNames));
        }
    }

    private Object getValue(Class<?> aClass, String stringValue) {
        if ("null".equalsIgnoreCase(stringValue)) {
            return null;
        }
        if (aClass.isAssignableFrom(String.class)) {
            return stringValue;
        } else if (aClass.isAssignableFrom(Double.class)) {
            return Double.parseDouble(stringValue);
        } else if (aClass.isPrimitive()) {
            if (aClass == Boolean.TYPE) {
                return Boolean.parseBoolean(stringValue);
            } else if (aClass == Integer.TYPE) {
                return Integer.parseInt(stringValue);
            }
        }
        return null;
    }

    private Method getMethod(String methodName) {
        for (Method m : IslandInfo.class.getDeclaredMethods()) {
            if (methodName.equalsIgnoreCase(m.getName()) ||
                    (methodName.startsWith("get") && (
                            ("is"+methodName.substring(3)).equalsIgnoreCase(m.getName()) ||
                            ("has"+methodName.substring(3)).equalsIgnoreCase(m.getName())
                    )))
            {
                return m;
            }
        }
        return null;
    }

    @Override
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    private static class ReflectionTabCompleter extends AbstractTabCompleter {
        private final List<String> list;

        public ReflectionTabCompleter(List<String> list) {
            this.list = list;
        }

        @Override
        protected List<String> getTabList(CommandSender commandSender, String term) {
            return list;
        }
    }
}

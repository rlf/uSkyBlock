package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command for showing the config-gui.
 */
public class ConfigGUICommand extends AbstractCommand {
    private final uSkyBlock plugin;
    public static final List<String> CONFIGS = Arrays.asList("config", "levelConfig", "challenges", "signs");

    public ConfigGUICommand(uSkyBlock plugin) {
        super("config|c", "usb.admin.config", "?config", tr("open GUI for config"));
        this.plugin = plugin;
    }

    @Override
    public TabCompleter getTabCompleter() {
        return new AbstractTabCompleter() {
            @Override
            protected List<String> getTabList(CommandSender commandSender, String term) {
                return CONFIGS;
            }
        };
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (sender instanceof Player) {
            String configName = "config";
            if (args.length > 0) {
                if (CONFIGS.contains(args[0])) {
                    configName = args[0];
                } else {
                    sender.sendMessage(tr("\u00a7eInvalid configuration name"));
                    return false;
                }
            }
            plugin.getConfigMenu().showMenu((Player) sender, configName + ".yml", 1);
            return true;
        }
        return false;
    }
}

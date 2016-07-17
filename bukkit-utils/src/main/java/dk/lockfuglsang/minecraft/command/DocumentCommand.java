package dk.lockfuglsang.minecraft.command;

import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command that traverses all the commands in a plugin, and generates documentation for them.
 */
public class DocumentCommand extends AbstractCommand {
    public static final List<String> FORMATS = Arrays.asList("text", "yml");
    private final JavaPlugin plugin;
    private TabCompleter tabCompleter;

    public DocumentCommand(JavaPlugin plugin, String name, String permission) {
        super(name, permission, "?format ?arg", tr("saves documentation of the commands to a file"));
        this.plugin = plugin;
        tabCompleter = new AbstractTabCompleter() {
            @Override
            protected List<String> getTabList(CommandSender commandSender, String term) {
                return FORMATS;
            }
        };
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("text")) {
            int lineWidth = args.length > 1 && args[1].matches("\\d+") ? Integer.parseInt(args[1], 10) : 130;
            return writeToFile(sender, new PlainTextCommandVisitor(lineWidth), getName() + ".txt");
        } else if (args[0].equalsIgnoreCase("yml")) {
            int groupDepth = args.length > 1 && args[1].matches("\\d+") ? Integer.parseInt(args[1], 10) : 2;
            return writeToFile(sender, new PluginYamlCommandVisitor(groupDepth), getName() + ".yml");
        }
        return false;
    }

    private boolean writeToFile(CommandSender sender, DocumentWriter visitor, String filename) {
        List<String> commands = new ArrayList<>(plugin.getDescription().getCommands().keySet());
        Collections.sort(commands);
        for (String cmd : commands) {
            PluginCommand pluginCommand = plugin.getCommand(cmd);
            if (pluginCommand.getExecutor() instanceof Command) {
                ((Command) pluginCommand.getExecutor()).accept(visitor);
            }
        }
        File docFile = new File(plugin.getDataFolder(), filename);
        try (FileOutputStream fos = new FileOutputStream(docFile);
             PrintStream ps = new PrintStream(fos, true, "UTF-8"))
        {
            visitor.writeTo(ps);
            sender.sendMessage(tr("Wrote documentation to {0}", docFile));
            return true;
        } catch (IOException e) {
            sender.sendMessage(tr("\u00a74Error writing documentation: {0}", e.getMessage()));
        }
        return false;
    }

    @Override
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }
}

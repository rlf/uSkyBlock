package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.PlainTextCommandVisitor;
import us.talabrek.ultimateskyblock.command.common.USBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 */
public class DocumentCommand extends AbstractUSBCommand {
    private static final List<String> cmds = Arrays.asList("island", "islandtalk", "partytalk", "usb");
    private final uSkyBlock plugin;

    public DocumentCommand(uSkyBlock plugin) {
        super("doc", "usb.admin.doc", "?format", tr("saves documentation of the commands to a file"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        File docFile = new File(plugin.getDataFolder(), "doc.txt");
        try (FileOutputStream fos = new FileOutputStream(docFile);PrintStream ps = new PrintStream(fos, true, "UTF-8")) {
            PlainTextCommandVisitor visitor = new PlainTextCommandVisitor();
            for (String cmd : cmds) {
                PluginCommand pluginCommand = plugin.getCommand(cmd);
                if (pluginCommand.getExecutor() instanceof USBCommand) {
                    ((USBCommand) pluginCommand.getExecutor()).accept(visitor);
                }
            }
            visitor.writeTo(ps);
            sender.sendMessage(tr("Wrote documentation to {0}", docFile));
            return true;
        } catch (IOException e) {
            sender.sendMessage(tr("\u00a74Error writing documentation: {0}", e.getMessage()));
        }
        return false;
    }
}

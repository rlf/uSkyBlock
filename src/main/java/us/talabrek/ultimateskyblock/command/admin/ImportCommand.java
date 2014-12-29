package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.imports.impl.ImportTabCompleter;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Command for importing external formats.
 */
public class ImportCommand extends AbstractUSBCommand {

    private final ImportTabCompleter completer;

    public ImportCommand() {
        super("import", "usb.admin.import", "format", "imports players and islands from other formats", null);
        completer = new ImportTabCompleter();
    }

    @Override
    public TabCompleter getTabCompleter() {
        return completer;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            uSkyBlock.getInstance().getPlayerImporter().importUSB(sender, args[args.length - 1]);
            return true;
        }
        return false;
    }
}

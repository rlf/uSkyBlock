package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.imports.ImportTabCompleter;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

/**
 * Command for importing external formats.
 */
public class ImportCommand extends AbstractCommand {

    private final ImportTabCompleter completer;

    public ImportCommand() {
        super("import", "usb.admin.import", "format", marktr("imports players and islands from other formats"));
        completer = new ImportTabCompleter();
    }

    @Override
    public TabCompleter getTabCompleter() {
        return completer;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            uSkyBlock.getInstance().getImporter().importUSB(sender, args[args.length - 1]);
            return true;
        }
        return false;
    }
}

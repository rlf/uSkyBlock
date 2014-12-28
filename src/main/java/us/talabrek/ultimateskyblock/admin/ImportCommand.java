package us.talabrek.ultimateskyblock.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.imports.impl.ImportTabCompleter;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Command for importing external formats.
 */
public class ImportCommand extends AbstractUSBCommand {
    public ImportCommand() {
        super("import", "usb.admin.import", "\u00a7a<format> \u00a77- imports players and islands from other formats", null, new ImportTabCompleter());
    }

    @Override
    public boolean execute(CommandSender sender, String... args) {
        if (args.length == 1) {
            uSkyBlock.getInstance().getPlayerImporter().importUSB(sender, args[args.length - 1]);
            return true;
        }
        return false;
    }
}

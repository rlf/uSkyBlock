package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

/**
 * Handles Orphans.
 */
public class OrphanCommand extends AbstractUSBCommand {
    public OrphanCommand() {
        super("orphan", "usb.mod.orphan", "", "manage orphans",
                "  \u00a7fcount \u00a77- count orphans\n"
                        + "  \u00a7bclear \u00a77- clear orphans\n"
                        + "  \u00a7bsave \u00a77- save orphans\n"
                        + "  \u00a7blist \u00a77- list orphans");
    }

    @Override
    public boolean execute(CommandSender sender, String... args) {
        if (args.length == 0 || args[0].trim().isEmpty()) {
            return false;
        }
        if (args[0].equalsIgnoreCase("count")) {
            sender.sendMessage("" + ChatColor.YELLOW + uSkyBlock.getInstance().orphanCount() + " old island locations will be used before new ones.");
        } else if (args[0].equalsIgnoreCase("clear")) {
            sender.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
            uSkyBlock.getInstance().clearOrphanedIsland();
        } else if (args[0].equalsIgnoreCase("save")) {
            sender.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
            uSkyBlock.getInstance().saveOrphans();
        } else if (args[0].equalsIgnoreCase("list")) {
            FileConfiguration config = uSkyBlock.getInstance().getOrphans();
            String list = config.getString("orphans.list", "");
            sender.sendMessage(ChatColor.YELLOW + "Orphans: " + list);
        } else {
            return false;
        }
        return true;
    }
}

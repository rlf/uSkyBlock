package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Handles Orphans.
 */
public class OrphanCommand extends CompositeUSBCommand {
    public OrphanCommand() {
        super("orphan", "usb.mod.orphan", "manage orphans");
        add(new AbstractUSBCommand("count", "count orphans") {
                @Override
                public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
                    sender.sendMessage("" + ChatColor.YELLOW + uSkyBlock.getInstance().orphanCount() + " old island locations will be used before new ones.");
                    return true;
                }
            });
        add(new AbstractUSBCommand("clear", "clear orphans") {
                @Override
                public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
                    sender.sendMessage(ChatColor.YELLOW + "Clearing all old (empty) island locations.");
                    uSkyBlock.getInstance().clearOrphanedIsland();
                    return true;
                }
            });
        add(new AbstractUSBCommand("save", "save orphans") {
                @Override
                public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
                    sender.sendMessage(ChatColor.YELLOW + "Saving the orphan list.");
                    uSkyBlock.getInstance().saveOrphans();
                    return true;
                }
            });
        add(new AbstractUSBCommand("list", "list orphans") {
                @Override
                public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
                    FileConfiguration config = uSkyBlock.getInstance().getOrphans();
                    String list = config.getString("orphans.list", "");
                    sender.sendMessage(ChatColor.YELLOW + "Orphans: " + list);
                    return true;
                }
            });
    }
}

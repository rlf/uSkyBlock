package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Handles Orphans.
 */
public class OrphanCommand extends CompositeUSBCommand {
    public OrphanCommand() {
        super("orphan", "usb.admin.orphan", "manage orphans");
        add(new AbstractUSBCommand("count", "count orphans") {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    sender.sendMessage("\u00a7e" + uSkyBlock.getInstance().orphanCount() + " old island locations will be used before new ones.");
                    return true;
                }
            });
        add(new AbstractUSBCommand("clear", "clear orphans") {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    sender.sendMessage("\u00a7eClearing all old (empty) island locations.");
                    uSkyBlock.getInstance().clearOrphanedIsland();
                    return true;
                }
            });
        add(new AbstractUSBCommand("save", "save orphans") {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    sender.sendMessage("\u00a7eSaving the orphan list.");
                    uSkyBlock.getInstance().saveOrphans();
                    return true;
                }
            });
        add(new AbstractUSBCommand("list", "list orphans") {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    FileConfiguration config = uSkyBlock.getInstance().getOrphans();
                    String list = config.getString("orphans.list", "");
                    sender.sendMessage("\u00a7eOrphans: " + list);
                    return true;
                }
            });
    }
}

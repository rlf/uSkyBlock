package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.island.OrphanLogic;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;
import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Handles Orphans.
 */
public class OrphanCommand extends CompositeUSBCommand {
    public OrphanCommand(final uSkyBlock plugin) {
        super("orphan", "usb.admin.orphan", tr("manage orphans"));
        add(new AbstractUSBCommand("count", tr("count orphans")) {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    sender.sendMessage(tr("\u00a7e{0} old island locations will be used before new ones.", plugin.getOrphanLogic().getOrphans().size()));
                    return true;
                }
            });
        add(new AbstractUSBCommand("clear", tr("clear orphans")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage(tr("\u00a7eClearing all old (empty) island locations."));
                plugin.getOrphanLogic().clear();
                return true;
            }
        });
        add(new AbstractUSBCommand("list", tr("list orphans")) {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    List<OrphanLogic.Orphan> orphans = plugin.getOrphanLogic().getOrphans();
                    sender.sendMessage(tr("\u00a7eOrphans: {0}",
                            orphans.toString()
                                    .replaceAll(", ", "\u00a77; \u00a75")
                                    .replaceAll("\\[", "\u00a75")
                                    .replaceAll("\\]", "")
                    ));
                    return true;
                }
            });
    }
}

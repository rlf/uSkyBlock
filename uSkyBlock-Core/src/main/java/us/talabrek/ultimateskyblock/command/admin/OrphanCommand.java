package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.island.OrphanLogic;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;
import java.util.Map;

/**
 * Handles Orphans.
 */
public class OrphanCommand extends CompositeCommand {
    public OrphanCommand(final uSkyBlock plugin) {
        super("orphan", "usb.admin.orphan", I18nUtil.tr("manage orphans"));
        add(new AbstractCommand("count", I18nUtil.tr("count orphans")) {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    sender.sendMessage(I18nUtil.tr("\u00a7e{0} old island locations will be used before new ones.", plugin.getOrphanLogic().getOrphans().size()));
                    return true;
                }
            });
        add(new AbstractCommand("clear", I18nUtil.tr("clear orphans")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage(I18nUtil.tr("\u00a7eClearing all old (empty) island locations."));
                plugin.getOrphanLogic().clear();
                return true;
            }
        });
        add(new AbstractCommand("list", I18nUtil.tr("list orphans")) {
                @Override
                public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                    List<OrphanLogic.Orphan> orphans = plugin.getOrphanLogic().getOrphans();
                    if (orphans.isEmpty()) {
                        sender.sendMessage(I18nUtil.tr("\u00a7eNo orphans currently registered."));
                    } else {
                        sender.sendMessage(I18nUtil.tr("\u00a7eOrphans: {0}",
                                orphans.toString()
                                        .replaceAll(", ", "\u00a77; \u00a75")
                                        .replaceAll("\\[", "\u00a75")
                                        .replaceAll("\\]", "")
                        ));
                    }
                    return true;
                }
            });
    }
}

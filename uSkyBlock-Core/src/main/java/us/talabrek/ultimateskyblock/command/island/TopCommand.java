package us.talabrek.ultimateskyblock.command.island;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class TopCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    public TopCommand(uSkyBlock plugin) {
        super("top", "usb.island.top", "?page", tr("display the top10 of islands"));
        this.plugin = plugin;
        addFeaturePermission("usb.admin.topten", tr("enables user to all-ways generate top-ten (no caching)"));
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        int page = 1;
        if (args.length == 1 && args[0].matches("\\d*")) {
            page = Integer.parseInt(args[0]);
        }
        plugin.getIslandLogic().showTopTen(sender, page);
        return true;
    }
}

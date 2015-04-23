package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class TopCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public TopCommand(uSkyBlock plugin) {
        super("top", "usb.island.topten", "?page", "display the top10 of islands");
        this.plugin = plugin;
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

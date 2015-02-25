package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Re-generates the topten.
 */
public class GenTopTenCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public GenTopTenCommand(uSkyBlock plugin) {
        super("topten", "usb.mod.topten", "manually update the top 10 list");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, String... args) {
        sender.sendMessage("\u00a7eGenerating the Top Ten list");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getIslandLogic().generateTopTen(sender);
                plugin.getIslandLogic().showTopTen(sender);
                sender.sendMessage("\u00a7eFinished generation of the Top Ten list");
            }
        });
        return true;
    }
}

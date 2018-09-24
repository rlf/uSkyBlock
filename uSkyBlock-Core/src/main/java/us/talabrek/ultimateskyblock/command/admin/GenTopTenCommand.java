package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Re-generates the topten.
 */
public class GenTopTenCommand extends AbstractCommand {
    private final uSkyBlock plugin;

    public GenTopTenCommand(uSkyBlock plugin) {
        super("topten", "usb.mod.topten", marktr("manually update the top 10 list"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, String... args) {
        sender.sendMessage(tr("\u00a7eGenerating the Top Ten list"));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getIslandLogic().generateTopTen(sender);
                plugin.getIslandLogic().showTopTen(sender, 1);
                sender.sendMessage(tr("\u00a7eFinished generation of the Top Ten list"));
            }
        });
        return true;
    }
}

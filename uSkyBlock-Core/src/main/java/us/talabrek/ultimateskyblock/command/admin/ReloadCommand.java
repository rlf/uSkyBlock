package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;

/**
 * Reloads the config-files for USB.
 */
public class ReloadCommand extends AbstractCommand {
    public ReloadCommand() {
        super("reload", "usb.admin.reload", marktr("reload configuration from file."));
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        uSkyBlock.getInstance().reloadConfig();
        sender.sendMessage(I18nUtil.tr("\u00a7eConfiguration reloaded from file."));
        return true;
    }
}

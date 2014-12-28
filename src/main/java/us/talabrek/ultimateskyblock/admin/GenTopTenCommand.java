package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Re-generates the topten.
 */
public class GenTopTenCommand extends AbstractUSBCommand {
    public GenTopTenCommand() {
        super("topten", "usb.mod.topten", "manually update the top 10 list");
    }

    @Override
    public boolean execute(CommandSender sender, Map<String,Object> data, String... args) {
        sender.sendMessage(ChatColor.YELLOW + "Generating the Top Ten list");
        // TODO: 27/12/2014 - R4zorax: Actually do the generation instead of just the showing.
        uSkyBlock.getInstance().getIslandLogic().showTopTen(sender);
        sender.sendMessage(ChatColor.YELLOW + "Finished generation of the Top Ten list");
        return true;
    }
}

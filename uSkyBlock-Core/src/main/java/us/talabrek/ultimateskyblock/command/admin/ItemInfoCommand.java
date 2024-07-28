package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command for querying items reg. NBT stuff
 */
public class ItemInfoCommand extends CompositeCommand {
    public ItemInfoCommand() {
        super("iteminfo", "usb.admin.iteminfo", marktr("advanced info about items"));
        add(new AbstractCommand("info|i", marktr("shows the component format for the currently held item")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player player) {
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    if (!itemStack.getType().isItem()) {
                        player.sendMessage(tr("\u00a7cNo item in hand!"));
                        return true;
                    }
                    String[] msgs = new String[]{
                        tr("\u00a7eInfo for \u00a79{0}", ItemStackUtil.asString(itemStack)),
                        tr("\u00a77 - name: \u00a79{0}", ItemStackUtil.getItemName(itemStack))
                    };
                    player.sendMessage(msgs);
                    return true;
                }
                sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                return false;
            }
        });
    }
}

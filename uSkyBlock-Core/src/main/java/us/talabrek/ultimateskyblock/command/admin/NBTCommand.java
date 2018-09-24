package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Command for querying items reg. NBT stuff
 */
public class NBTCommand extends CompositeCommand {
    public NBTCommand() {
        super("nbt", "usb.admin.nbt", marktr("advanced info about NBT stuff"));
        add(new AbstractCommand("info|i", marktr("shows the NBTTag for the currently held item")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    ItemStack itemStack = player.getInventory().getItemInHand();
                    if (itemStack != null) {
                        String[] msgs = new String[]{
                                tr("\u00a7eInfo for \u00a79{0}", ItemStackUtil.asString(itemStack)),
                                tr("\u00a77 - name: \u00a79{0}", VaultHandler.getItemName(itemStack)),
                                tr("\u00a77 - nbttag: \u00a79{0}", NBTUtil.getNBTTag(itemStack))
                        };
                        player.sendMessage(msgs);
                    } else {
                        player.sendMessage(tr("\u00a7cNo item in hand!"));
                    }
                    return true;
                }
                sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                return false;
            }
        });
        add(new AbstractCommand("set|s", null, "nbttag", marktr("sets the NBTTag on the currently held item")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    if (args.length > 0) {
                        Player player = (Player) sender;
                        ItemStack itemStack = player.getInventory().getItemInHand();
                        if (itemStack != null) {
                            String nbtTag = join(args);
                            itemStack = NBTUtil.setNBTTag(itemStack, nbtTag);
                            player.getInventory().setItemInHand(itemStack);
                            player.sendMessage(tr("\u00a7eSet \u00a79{0}\u00a7e to \u00a7c{1}", nbtTag, itemStack));
                        } else {
                            player.sendMessage(tr("\u00a7cNo item in hand!"));
                        }
                        return true;
                    }
                }
                sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                return false;
            }
        });
        add(new AbstractCommand("add|a", null, "nbttag", marktr("adds the NBTTag on the currently held item")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (sender instanceof Player) {
                    if (args.length > 0) {
                        Player player = (Player) sender;
                        ItemStack itemStack = player.getInventory().getItemInHand();
                        if (itemStack != null) {
                            String nbtTag = join(args);
                            itemStack = NBTUtil.addNBTTag(itemStack, nbtTag);
                            player.getInventory().setItemInHand(itemStack);
                            player.sendMessage(tr("\u00a7eAdded \u00a79{0}\u00a7e to \u00a7c{1}", nbtTag, itemStack));
                        } else {
                            player.sendMessage(tr("\u00a7cNo item in hand!"));
                        }
                        return true;
                    }
                }
                sender.sendMessage(tr("\u00a7eCan only be executed as a player"));
                return false;
            }
        });
    }
}

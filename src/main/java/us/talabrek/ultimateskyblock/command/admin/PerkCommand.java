package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.player.Perk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class PerkCommand extends CompositeUSBCommand {
    public PerkCommand(final uSkyBlock plugin) {
        super("perk", "usb.admin.perk", "shows perk-information");
        add(new AbstractUSBCommand("list", "lists all perks") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Perk> entry : plugin.getPerkLogic().getPerkMap().entrySet()) {
                    sb.append("\u00a79" + entry.getKey() + ":\n");
                    String value = (entry.getValue().toString().replaceAll("\n", "\n  ")).trim();
                    sb.append("  " + value + "\n");
                }
                sender.sendMessage(sb.toString().split("\n"));
                return true;
            }
        });
        add(new AbstractUSBCommand("player", "", "player", "shows a specific players perks") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1) {
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player != null) {
                        StringBuilder sb = new StringBuilder();
                        Perk perk = plugin.getPerkLogic().getPerk(player);
                        sb.append("\u00a79" + player.getName() + ":\n");
                        String value = (perk.toString().replaceAll("\n", "\n  ")).trim();
                        sb.append("  " + value + "\n");
                        sender.sendMessage(sb.toString().split("\n"));
                        return true;
                    } else {
                        sender.sendMessage(tr("\u00a74No player named {0} was found!", args[0]));
                    }
                }
                return false;
            }
        });
    }
}

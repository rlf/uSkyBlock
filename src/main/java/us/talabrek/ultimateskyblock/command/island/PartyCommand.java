package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.SkyBlockMenu;
import us.talabrek.ultimateskyblock.command.InviteHandler;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.*;

public class PartyCommand extends CompositeUSBCommand {
    private final uSkyBlock plugin;
    private final SkyBlockMenu menu;

    public PartyCommand(final uSkyBlock plugin, SkyBlockMenu menu, final InviteHandler inviteHandler) {
        super("party", "usb.island.create", "show party information");
        this.plugin = plugin;
        this.menu = menu;
        add(new AbstractUSBCommand("info", "shows information about your party") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage(plugin.getIslandInfo((Player) sender).toString());
                return true;
            }
        });
        add(new AbstractUSBCommand("invites", "show pending invites") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                IslandInfo islandInfo = plugin.getIslandInfo((Player) sender);
                Set<UUID> pendingInvites = inviteHandler.getPendingInvites(islandInfo);
                if (pendingInvites == null || pendingInvites.isEmpty()) {
                    sender.sendMessage("\u00a7eNo pending invites");
                } else {
                    List<String> invites = new ArrayList<>();
                    for (UUID uuid : pendingInvites) {
                        invites.add(plugin.getServer().getOfflinePlayer(uuid).getName());
                    }
                    sender.sendMessage("\u00a7ePending invites: " + invites);
                }
                return true;
            }
        });
        add(new AbstractUSBCommand("uninvite", null, "player", "withdraw an invite") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1) {
                    IslandInfo islandInfo = plugin.getIslandInfo((Player) sender);
                    if (!islandInfo.isLeader(sender.getName()) || !islandInfo.hasPerm(sender.getName(), "canInviteOthers")) {
                        sender.sendMessage("\u00a74You don't have permissions to unvite players.");
                        return true;
                    }
                    String playerName = args[0];
                    if (inviteHandler.uninvite(islandInfo, playerName)) {
                        sender.sendMessage("\u00a7eSuccessfully withdrew invite for " + playerName);
                    } else {
                        sender.sendMessage("\u00a74No pending invite found for " + playerName);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a74This command can only be executed by a player");
            return false;
        }
        Player player = (Player) sender;
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        if (playerInfo == null || !playerInfo.getHasIsland()) {
            player.sendMessage("\u00a74No Island. \u00a7eUse \u00a7b/is create\u00a7e to get one");
            return true;
        }
        if (args.length == 0) {
            player.openInventory(menu.displayPartyGUI(player));
            return true;
        }
        return super.execute(sender, alias, data, args);
    }
}

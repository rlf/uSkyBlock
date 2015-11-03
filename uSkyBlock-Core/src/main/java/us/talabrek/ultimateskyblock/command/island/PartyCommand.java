package us.talabrek.ultimateskyblock.command.island;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.InviteHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Collection;
import java.util.Map;

public class PartyCommand extends CompositeCommand {
    private final uSkyBlock plugin;
    private final SkyBlockMenu menu;

    public PartyCommand(final uSkyBlock plugin, SkyBlockMenu menu, final InviteHandler inviteHandler) {
        super("party", "usb.island.create", I18nUtil.tr("show party information"));
        this.plugin = plugin;
        this.menu = menu;
        add(new AbstractCommand("info", I18nUtil.tr("shows information about your party")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage(plugin.getIslandInfo((Player) sender).toString());
                return true;
            }
        });
        add(new AbstractCommand("invites", I18nUtil.tr("show pending invites")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                IslandInfo islandInfo = plugin.getIslandInfo((Player) sender);
                Collection<String> pendingInvitesAsNames = inviteHandler.getPendingInvitesAsNames(islandInfo);
                if (pendingInvitesAsNames == null || pendingInvitesAsNames.isEmpty()) {
                    sender.sendMessage(I18nUtil.tr("\u00a7eNo pending invites"));
                } else {
                    sender.sendMessage("\u00a7ePending invites: " + pendingInvitesAsNames);
                }
                return true;
            }
        });
        add(new AbstractCommand("uninvite", null, "player", I18nUtil.tr("withdraw an invite")) {
            @Override
            public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, final String... args) {
                if (args.length == 1) {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            IslandInfo islandInfo = plugin.getIslandInfo((Player) sender);
                            if (!islandInfo.isLeader(sender.getName()) || !islandInfo.hasPerm(sender.getName(), "canInviteOthers")) {
                                sender.sendMessage(I18nUtil.tr("\u00a74You don't have permissions to uninvite players."));
                                return;
                            }
                            String playerName = args[0];
                            if (inviteHandler.uninvite(islandInfo, playerName)) {
                                sender.sendMessage("\u00a7eSuccessfully withdrew invite for " + playerName);
                            } else {
                                sender.sendMessage("\u00a74No pending invite found for " + playerName);
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18nUtil.tr("\u00a74This command can only be executed by a player"));
            return false;
        }
        Player player = (Player) sender;
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        if (playerInfo == null || !playerInfo.getHasIsland()) {
            player.sendMessage(I18nUtil.tr("\u00a74No Island. \u00a7eUse \u00a7b/is create\u00a7e to get one"));
            return true;
        }
        if (args.length == 0) {
            player.openInventory(menu.displayPartyGUI(player));
            return true;
        }
        return super.execute(sender, alias, data, args);
    }
}

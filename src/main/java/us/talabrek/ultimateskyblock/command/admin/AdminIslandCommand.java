package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.admin.task.ProtectAllTask;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The island moderator command.
 */
public class AdminIslandCommand extends CompositeUSBCommand {
    private final uSkyBlock plugin;

    public AdminIslandCommand(final uSkyBlock plugin) {
        super("island", "", "manage islands");
        this.plugin = plugin;
        add(new AbstractIslandInfoCommand("protect", "usb.mod.protect", "protects the island") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                protectIsland(sender, islandInfo);
            }
        });
        add(new AbstractUSBCommand("delete", "usb.admin.delete", "?leader", "delete the island (removes the blocks)") {
            @Override
            public boolean execute(final CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1) {
                    PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
                    if (playerInfo == null) {
                        sender.sendMessage(String.format("\u00a74Could not locate an island for player %s!", args[0]));
                        return false;
                    }
                    deleteIsland(sender, playerInfo);
                    return true;
                } else if (args.length == 0 && sender instanceof Player) {
                    String islandName = WorldGuardHandler.getIslandNameAt(((Player) sender).getLocation());
                    if (islandName != null) {
                        if (plugin.deleteEmptyIsland(islandName, new Runnable() {
                            @Override
                            public void run() {
                                sender.sendMessage(tr("\u00a79Deleted abandoned island at your current location."));
                            }
                        })) {
                            return true;
                        } else {
                            sender.sendMessage(tr("\u00a74Island at this location has members!\n\u00a7eUse \u00a79/usb island delete <name>\u00a7e to delete it."));
                        }
                    }
                }
                return false;
            }
        });
        add(new AbstractIslandInfoCommand("remove", "usb.admin.remove", "removes the player from the island") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                removePlayerFromIsland(sender, playerInfo, islandInfo);
            }
        });
        add(new AbstractIslandInfoCommand("info", null, "print out info about the island") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                sender.sendMessage(islandInfo.toString());
            }
        });
        add(new AbstractUSBCommand("protectall", "usb.admin.protectall", "protects all islands (time consuming)") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                protectAll(plugin, sender);
                return true;
            }
        });
        add(new AbstractUSBCommand("setbiome", "usb.admin.setbiome", "?leader biome", "sets the biome of the island") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 2) {
                    PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
                    if (playerInfo == null || !playerInfo.getHasIsland()) {
                        sender.sendMessage(tr("\u00a74That player has no island."));
                        return false;
                    }
                    setBiome(sender, playerInfo, plugin.getIslandInfo(playerInfo), args[1]);
                    return true;
                } else if (args.length == 1 && sender instanceof Player) {
                    Biome biome = plugin.getBiome(args[0]);
                    String islandName = WorldGuardHandler.getIslandNameAt(((Player) sender).getLocation());
                    if (biome == null || islandName == null) {
                        return false;
                    }
                    IslandInfo islandInfo = plugin.getIslandInfo(islandName);
                    if (islandInfo == null) {
                        sender.sendMessage(tr("\u00a74No valid island at your location"));
                        return false;
                    }
                    setBiome(sender, islandInfo, biome.name());
                    return true;
                }
                return false;
            }
        });
        add(new MakeLeaderCommand(plugin));
        add(new RegisterIslandToPlayerCommand());
    }

    private void removePlayerFromIsland(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo) {
        if (playerInfo == null) {
            sender.sendMessage(tr("\u00a74No valid player-name supplied."));
            return;
        }
        sender.sendMessage(tr("Removing {0} from island", playerInfo.getPlayerName()));
        playerInfo.removeFromIsland();
        islandInfo.removeMember(playerInfo);
        playerInfo.save();
    }

    private void setBiome(CommandSender sender, IslandInfo islandInfo, String biome) {
        if (uSkyBlock.getInstance().setBiome(islandInfo.getIslandLocation(), biome)) {
            islandInfo.setBiome(biome);
            sender.sendMessage(tr("\u00a7eChanged biome of {0}s island to {1}.", islandInfo.getLeader(), biome));
        } else {
            islandInfo.setBiome("OCEAN");
            sender.sendMessage(tr("\u00a7eChanged biome of {0}s island to OCEAN.", islandInfo.getLeader()));
        }
        sender.sendMessage(tr("\u00a7aYou may need to go to spawn, or relog, to see the changes."));
    }

    private void setBiome(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String biome) {
        if (playerInfo == null || !playerInfo.getHasIsland()) {
            sender.sendMessage(tr("\u00a74That player has no island."));
            return;
        }
        if (uSkyBlock.getInstance().setBiome(playerInfo.getIslandLocation(), biome)) {
            islandInfo.setBiome(biome);
            sender.sendMessage(tr("\u00a7e{0} has had their biome changed to {1}.", playerInfo.getPlayerName(), biome));
        } else {
            islandInfo.setBiome("OCEAN");
            sender.sendMessage(tr("\u00a7e{0} has had their biome changed to OCEAN.", playerInfo.getPlayerName()));
        }
        sender.sendMessage(tr("\u00a7aYou may need to go to spawn, or relog, to see the changes."));
    }

    private void protectAll(uSkyBlock plugin, CommandSender sender) {
        synchronized (plugin) {
            if (plugin.isProtectAllActive()) {
                sender.sendMessage(tr("\u00a74Sorry!\u00a7e A protect-all is already running. Let it complete first."));
                return;
            }
            plugin.setProtectAllActive(true);
        }
        sender.sendMessage(tr("\u00a7eStarting a protect-all task. It will take a while."));
        new ProtectAllTask(plugin, sender).runTask(plugin);
    }

    private void deleteIsland(CommandSender sender, PlayerInfo playerInfo) {
        if (playerInfo != null && playerInfo.getIslandLocation() != null) {
            sender.sendMessage(tr("\u00a7eRemoving {0}'s island.", playerInfo.getPlayerName()));
            uSkyBlock.getInstance().deletePlayerIsland(playerInfo.getPlayerName(), null);
        } else {
            sender.sendMessage(tr("Error: That player does not have an island!"));
        }
    }

    private void protectIsland(CommandSender sender, IslandInfo islandInfo) {
        if (WorldGuardHandler.protectIsland(plugin, sender, islandInfo)) {
            sender.sendMessage(tr("\u00a7e{0}'s island at {1} has been protected", islandInfo.getLeader(), islandInfo.getName()));
        } else {
            sender.sendMessage(tr("\u00a74{0}'s island at {1} was already protected", islandInfo.getLeader(), islandInfo.getName()));
        }
    }
}

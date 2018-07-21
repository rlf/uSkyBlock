package us.talabrek.ultimateskyblock.command.admin;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.minecraft.command.CompositeCommand;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.ConfirmHandler;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * The island moderator command.
 */
public class AdminIslandCommand extends CompositeCommand {
    private final uSkyBlock plugin;

    public AdminIslandCommand(final uSkyBlock plugin, final ConfirmHandler confirmHandler) {
        super("island|is", "usb.admin", marktr("manage islands"));
        this.plugin = plugin;
        add(new AbstractIslandInfoCommand("protect", "usb.admin.protect", marktr("protects the island")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                protectIsland(sender, islandInfo);
            }
        });
        add(new AbstractCommand("delete", "usb.admin.delete", "?leader", marktr("delete the island (removes the blocks)")) {
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
        add(new AbstractIslandInfoCommand("remove", "usb.admin.remove", marktr("removes the player from the island")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                removePlayerFromIsland(sender, playerInfo, islandInfo);
            }
        });
        add(new AbstractCommand("addmember|add", "usb.admin.addmember", "player ?island", marktr("adds the player to the island")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                IslandInfo islandInfo = null;
                if (args.length == 2) {
                    islandInfo = plugin.getIslandInfo(Bukkit.getPlayer(args[1]));
                } else if (args.length == 1 && sender instanceof Player) {
                    String islandName = WorldGuardHandler.getIslandNameAt(((Player) sender).getLocation());
                    islandInfo = plugin.getIslandInfo(islandName);
                }
                if (islandInfo != null && args.length > 0) {
                    PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
                    if (playerInfo != null) {
                        islandInfo.addMember(playerInfo);
                        playerInfo.save();
                        islandInfo.sendMessageToIslandGroup(true, marktr("\u00a7b{0}\u00a7d has joined your island group."), playerInfo.getDisplayName());
                        return true;
                    } else {
                        sender.sendMessage(tr("\u00a74No player named {0} found!", args[0]));
                    }
                } else {
                    sender.sendMessage(tr("\u00a74No valid island provided, either stand within one, or provide an island name"));
                }
                return false;
            }
        });
        add(new AbstractIslandInfoCommand("info", "usb.admin.info", marktr("print out info about the island")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                sender.sendMessage(islandInfo.toString());
            }
        });
        add(new AbstractCommand("setbiome", "usb.admin.setbiome", "?leader biome", marktr("sets the biome of the island")) {
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
        add(new AbstractCommand("purge", "usb.admin.purge", "?leader", marktr("purges the island")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                String cmd = "/usb island purge";
                IslandInfo islandInfo = null;
                if (args.length == 0 && sender instanceof Player) {
                    Player player = (Player) sender;
                    String islandName = WorldGuardHandler.getIslandNameAt(player.getLocation());
                    islandInfo = plugin.getIslandInfo(islandName);
                } else if (args.length == 1) {
                    cmd += " " + args[0];
                    PlayerInfo playerInfo = plugin.getPlayerInfo(args[0]);
                    islandInfo = plugin.getIslandInfo(playerInfo);
                }
                if (islandInfo == null) {
                    sender.sendMessage(tr("\u00a74Error! \u00a79No valid island found for purging."));
                    return false;
                } else {
                    String islandName = islandInfo.getName();
                    if (sender instanceof Player && confirmHandler.checkCommand((Player) sender, cmd)) {
                        plugin.getIslandLogic().purge(islandName);
                        sender.sendMessage(tr("\u00a7cPURGE: \u00a79Purged island at {0}", islandName));
                    } else if (!(sender instanceof Player)) {
                        plugin.getIslandLogic().purge(islandName);
                        sender.sendMessage(tr("\u00a7cPURGE: \u00a79Purged island at {0}", islandName));
                    }
                }
                return true;
            }
        });
        add(new MakeLeaderCommand(plugin));
        add(new RegisterIslandToPlayerCommand());
        add(new AbstractIslandInfoCommand("ignore", "usb.admin.ignore", marktr("toggles the islands ignore status")) {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                if (islandInfo != null) {
                    islandInfo.setIgnore(!islandInfo.ignore());
                    if (islandInfo.ignore()) {
                        sender.sendMessage(tr("\u00a7cSet {0}s island to be ignored on top-ten and purge.", islandInfo.getLeader()));
                    } else {
                        sender.sendMessage(tr("\u00a7cRemoved ignore-flag of {0}s island, it will now show up on top-ten and purge.", islandInfo.getLeader()));
                    }
                }
            }
        });
        add(new SetIslandDataCommand(plugin));
        add(new GetIslandDataCommand());
    }

    private void removePlayerFromIsland(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo) {
        if (playerInfo == null) {
            sender.sendMessage(tr("\u00a74No valid player-name supplied."));
            return;
        }
        sender.sendMessage(tr("Removing {0} from island", playerInfo.getPlayerName()));
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

    private void deleteIsland(CommandSender sender, PlayerInfo playerInfo) {
        if (playerInfo != null && playerInfo.getIslandLocation() != null) {
            sender.sendMessage(tr("\u00a7eRemoving {0}''s island.", playerInfo.getPlayerName()));
            uSkyBlock.getInstance().deletePlayerIsland(playerInfo.getPlayerName(), null);
        } else {
            sender.sendMessage(tr("Error: That player does not have an island!"));
        }
    }

    private void protectIsland(CommandSender sender, IslandInfo islandInfo) {
        if (WorldGuardHandler.protectIsland(plugin, sender, islandInfo)) {
            sender.sendMessage(tr("\u00a7e{0}s island at {1} has been protected", islandInfo.getLeader(), islandInfo.getName()));
        } else {
            sender.sendMessage(tr("\u00a74{0}s island at {1} was already protected", islandInfo.getLeader(), islandInfo.getName()));
        }
    }
}

package us.talabrek.ultimateskyblock.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * The island moderator command.
 */
public class AdminIslandCommand extends CompositeUSBCommand {
    public AdminIslandCommand() {
        super("island", "usb.admin.island", "manage islands");
        add(new AbstractIslandInfoCommand("protect", "usb.mod.protect", "protects the island") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                protectIsland(sender, playerInfo, islandInfo);
            }
        });
        add(new AbstractIslandInfoCommand("delete", "usb.admin.delete", "delete the island (removes the blocks)") {
            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                deleteIsland(sender, playerInfo, islandInfo);
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
        add(new AbstractUSBCommand("protectall", "usb.mod.protectall", "protects all islands (time consuming)") {
            @Override
            public boolean execute(CommandSender sender, Map<String, Object> data, String... args) {
                protectAll(sender);
                return true;
            }
        });
        add(new AbstractIslandInfoCommand("setbiome", "usb.mod.biome", "sets the biome of the island") {
            private final String[] params = new String[]{"player", "biome"};
            @Override
            public String[] getParams() {
                return params;
            }

            @Override
            protected void doExecute(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String... args) {
                if (args.length > 0) {
                    setBiome(sender, playerInfo, islandInfo, args[0]);
                } else {
                    sender.sendMessage(ChatColor.RED + "No biome supplied!");
                }
            }
        });
    }

    private void removePlayerFromIsland(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo) {
        sender.sendMessage("Removing " + playerInfo.getPlayerName() + " from island");
        playerInfo.removeFromIsland();
        islandInfo.removeMember(playerInfo.getPlayerName());
        playerInfo.save();
    }

    private void setBiome(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo, String biome) {
        if (uSkyBlock.getInstance().setBiome(playerInfo.getIslandLocation(), biome)) {
            islandInfo.setBiome(biome);
            sender.sendMessage(ChatColor.YELLOW + playerInfo.getPlayerName() + " has had their biome changed to " + biome + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW + playerInfo.getPlayerName() + " has had their biome changed to OCEAN.");
        }
        sender.sendMessage(ChatColor.GREEN + "You may need to go to spawn, or relog, to see the changes.");
    }

    private void protectAll(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Oups, that was embarrassing - protectall is currently out of order");
    }

    private void deleteIsland(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo) {
        if (playerInfo.getIslandLocation() != null) {
            sender.sendMessage(ChatColor.YELLOW + "Removing " + playerInfo.getPlayerName() + "'s island.");
            uSkyBlock.getInstance().deletePlayerIsland(playerInfo.getPlayerName());
        } else {
            sender.sendMessage("Error: That player does not have an island!");
        }
    }

    private void protectIsland(CommandSender sender, PlayerInfo playerInfo, IslandInfo islandInfo) {
        if (WorldGuardHandler.protectIsland(sender, islandInfo.getLeader(), playerInfo)) {
            sender.sendMessage(ChatColor.YELLOW + playerInfo.getPlayerName() + "'s island at " + playerInfo.locationForParty() + " has been protected");
        } else {
            sender.sendMessage(ChatColor.RED + playerInfo.getPlayerName() + "'s island at " + playerInfo.locationForParty() + " was already protected");
        }
    }
}

package us.talabrek.ultimateskyblock.admin.island;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.command.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.completion.AbstractTabCompleter;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The island moderator command.
 */
public class AdminIslandCommand extends AbstractUSBCommand implements TabCompleter {
    private static final Map<String, String[]> cmds = new LinkedHashMap<>();
    static {
        cmds.put("protect", new String[]{"<player>", "protects the island"});
        cmds.put("delete", new String[]{"<player>", "delete the island (removes the blocks)"});
        cmds.put("remove", new String[]{"<player>", "removes the player from the island"});
        cmds.put("protectall", new String[]{null, "protects all known islands (time consuming)"});
        cmds.put("setbiome", new String[]{"<player> <biome>", "sets the biome of the island"});
    }

    public AdminIslandCommand() {
        super("island", "usb.admin.island", "\u00a77- manage islands");
    }

    @Override
    public boolean execute(CommandSender sender, String... args) {
        String cmdName = args.length > 0 ? args[0].toLowerCase() : "";
        if (!cmds.containsKey(cmdName)) {
            return false;
        }
        if (hasPlayerArg(cmdName) && args.length > 1) {
            String playerName = args[1];
            PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(playerName);
            IslandInfo islandInfo = uSkyBlock.getInstance().getIslandInfo(playerInfo);
            if (playerInfo != null && islandInfo != null) {
                if (cmdName.equalsIgnoreCase("protect")) {
                    protectIsland(sender, playerInfo, islandInfo);
                } else if (cmdName.equalsIgnoreCase("delete")) {
                    deleteIsland(sender, playerInfo, islandInfo);
                } else if (cmdName.equalsIgnoreCase("remove")) {
                    removePlayerFromIsland(sender, playerInfo, islandInfo);
                } else if (cmdName.equalsIgnoreCase("setbiome") && args.length > 2) {
                    setBiome(sender, playerInfo, islandInfo, args[2].toUpperCase());
                }
                return true;
            } else if (playerInfo == null) {
                sender.sendMessage(ChatColor.RED + "No valid player named " + playerName + " found.");
            } else if (islandInfo == null) {
                sender.sendMessage(ChatColor.RED + playerName + " has no island.");
            }
            return true;
        } else if (cmdName.matches("(iu)protectall")) {
            protectAll(sender);
        } else {
            return false;
        }
        return true;
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
        // TODO: 27/12/2014 - R4zorax: Threading and everything...
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

    private boolean hasPlayerArg(String cmdName) {
        return cmdName.matches("(?iu)protect|delete|remove|setbiome");
    }

    @Override
    public String getUsage() {
        String usage = super.getUsage();
        if (usage == null) {
            usage = "";
        }
        for (String cmd : cmds.keySet()) {
            String args = cmds.get(cmd)[0];
            usage += "  \u00a7b" + cmd;
            if (args != null) {
                usage += "\u00a7a " + args;
            }
            usage += "\u00a77 - " + cmds.get(cmd)[1] + "\n";
        }
        return usage;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String subCmd = args.length > 0 ? args[0] : "";
        if (args.length <= 1) {
            return AbstractTabCompleter.filter(cmds.keySet(), subCmd);
        } else if (hasPlayerArg(subCmd) && args.length == 2) {
            //return playerTabCompleter.onTabComplete(sender, command, alias, args);
        } else if (args.length == 3 && subCmd.equalsIgnoreCase("setbiome")) {
            //return biomeTabCompleter.onTabComplete(sender, command, alias, args);
        }
        return null;
    }

}

package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.VaultHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ChallengesCommand implements CommandExecutor, TabCompleter {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        final Player player = (Player)sender;
        if (!VaultHandler.checkPerk(player.getName(), "usb.island.challenges", player.getWorld())) {
            player.sendMessage(ChatColor.RED + "You don't have access to this command!");
            return true;
        }
        if (!player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            player.sendMessage(ChatColor.RED + "You can only submit challenges in the skyblock world!");
            return true;
        }
        PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(player);
        if (!playerInfo.getHasIsland()) {
            player.sendMessage(ChatColor.RED + "You can only submit challenges when you have an island!");
            return true;
        }
        ChallengeLogic challengeLogic = uSkyBlock.getInstance().getChallengeLogic();
        if (split.length == 0) {
            player.openInventory(uSkyBlock.getInstance().getMenu().displayChallengeGUI(player, 1));
        } else if (split.length == 1) {
            String arg = split[0].toLowerCase();
            Challenge challenge = challengeLogic.getChallenge(arg);
            if (arg.equals("help") || arg.equals("complete") || arg.equals("c")) {
                player.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
                player.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
                player.sendMessage(ChatColor.YELLOW + "Challenges will have different colors depending on if they are:");
                player.sendMessage(challengeLogic.defaults.challengeColor + "Incomplete " + challengeLogic.defaults.finishedColor + "Completed (not repeatable) " + challengeLogic.defaults.repeatableColor + "Completed(repeatable) ");
            } else if (challenge != null && challenge.getRank().isAvailable(playerInfo)) {
                player.sendMessage(ChatColor.YELLOW + "Challenge Name: " + ChatColor.WHITE + arg.toLowerCase());
                player.sendMessage(ChatColor.YELLOW + challenge.getDescription());
                if (challenge.getType() == Challenge.Type.PLAYER) {
                    if (challenge.isTakeItems()) {
                        player.sendMessage(ChatColor.RED + "You will lose all required items when you complete this challenge!");
                    }
                } else if (challenge.getType() == Challenge.Type.ISLAND) {
                    player.sendMessage(ChatColor.RED + "All required items must be placed on your island, within " + challenge.getRadius() + " blocks of you.");
                }
                if (challengeLogic.getRanks().size() > 1) {
                    player.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + challenge.getRank());
                }
                ChallengeCompletion completion = playerInfo.getChallenge(arg);
                if (completion.getTimesCompleted() > 0 && !challenge.isRepeatable()) {
                    player.sendMessage(ChatColor.RED + "This Challenge is not repeatable!");
                    return true;
                }
                ItemStack item = challenge.getDisplayItem(completion, challengeLogic.defaults.enableEconomyPlugin);
                for (String lore : item.getItemMeta().getLore()) {
                    if (lore != null && !lore.trim().isEmpty()) {
                        player.sendMessage(lore);
                    }
                }
                player.sendMessage(ChatColor.YELLOW + "To complete this challenge, use " + ChatColor.WHITE + "/c c " + arg.toLowerCase());
            } else {
                player.sendMessage(ChatColor.RED + "Invalid challenge name! Use /c help for more information");
            }
        } else if (split.length == 2 && (split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c"))) {
            challengeLogic.completeChallenge(player, split[1]);
        }
        return true;
    }

    private void filter(List<String> list, String search) {
        for (ListIterator<String> it = list.listIterator(); it.hasNext(); ) {
            String test = it.next();
            if (!test.startsWith(search)) {
                it.remove();
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<String> suggestions = new ArrayList<>();
            if (args.length == 1) {
                suggestions.add("help");
                suggestions.add("complete");
            }
            if (args.length >= 1) {
                PlayerInfo playerInfo = uSkyBlock.getInstance().getPlayerInfo(player);
                suggestions.addAll(uSkyBlock.getInstance().getChallengeLogic().getAvailableChallengeNames(playerInfo));
                filter(suggestions, args[args.length - 1]);
            }
            Collections.sort(suggestions);
            return suggestions;
        }
        return null;
    }
}

package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class ChallengesCommand implements CommandExecutor, TabCompleter {
    private final uSkyBlock plugin;

    public ChallengesCommand(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        if (!plugin.isRequirementsMet(sender)) {
            return false;
        }
        if (!(sender instanceof Player)) {
            return false;
        }
        if (!plugin.getChallengeLogic().isEnabled()) {
            sender.sendMessage(tr("\u00a7eChallenges has been disabled. Contact an administrator."));
            return false;
        }
        final Player player = (Player)sender;
        if (!VaultHandler.checkPerk(player.getName(), "usb.island.challenges", player.getWorld())) {
            player.sendMessage(tr("\u00a74You don't have access to this command!"));
            return true;
        }
        if (!player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
            player.sendMessage(tr("\u00a74You can only submit challenges in the skyblock world!"));
            return true;
        }
        PlayerInfo playerInfo = plugin.getPlayerInfo(player);
        if (!playerInfo.getHasIsland()) {
            player.sendMessage(tr("\u00a74You can only submit challenges when you have an island!"));
            return true;
        }
        ChallengeLogic challengeLogic = plugin.getChallengeLogic();
        if (split.length == 0) {
            player.openInventory(plugin.getMenu().displayChallengeGUI(player, 1));
        } else if (split.length == 1) {
            String arg = split[0].toLowerCase();
            Challenge challenge = challengeLogic.getChallenge(arg);
            if (arg.equals("help") || arg.equals("complete") || arg.equals("c")) {
                player.sendMessage(tr("\u00a7eUse /c <name> to view information about a challenge."));
                player.sendMessage(tr("\u00a7eUse /c complete <name> to attempt to complete that challenge."));
                player.sendMessage(tr("\u00a7eChallenges will have different colors depending on if they are:"));
                player.sendMessage(challengeLogic.defaults.challengeColor + tr("Incomplete") + " "
                        + challengeLogic.defaults.finishedColor + tr("Completed (not repeatable)") + " "
                        + challengeLogic.defaults.repeatableColor + tr("Completed(repeatable)"));
            } else if (challenge != null && challenge.getRank().isAvailable(playerInfo)) {
                player.sendMessage("\u00a7eChallenge Name: " + ChatColor.WHITE + arg.toLowerCase());
                player.sendMessage("\u00a7e" + challenge.getDescription());
                if (challenge.getType() == Challenge.Type.PLAYER) {
                    if (challenge.isTakeItems()) {
                        player.sendMessage(tr("\u00a74You will lose all required items when you complete this challenge!"));
                    }
                } else if (challenge.getType() == Challenge.Type.ISLAND) {
                    player.sendMessage(tr("\u00a74All required items must be placed on your island, within {0} blocks of you.", challenge.getRadius()));
                }
                if (challengeLogic.getRanks().size() > 1) {
                    player.sendMessage(tr("\u00a7eRank: ") + ChatColor.WHITE + challenge.getRank());
                }
                ChallengeCompletion completion = playerInfo.getChallenge(arg);
                if (completion.getTimesCompleted() > 0 && !challenge.isRepeatable()) {
                    player.sendMessage(tr("\u00a74This Challenge is not repeatable!"));
                    return true;
                }
                ItemStack item = challenge.getDisplayItem(completion, challengeLogic.defaults.enableEconomyPlugin);
                for (String lore : item.getItemMeta().getLore()) {
                    if (lore != null && !lore.trim().isEmpty()) {
                        player.sendMessage(lore);
                    }
                }
                player.sendMessage(tr("\u00a7eTo complete this challenge, use \u00a7f/c c {0}", arg.toLowerCase()));
            } else {
                player.sendMessage(tr("\u00a74Invalid challenge name! Use /c help for more information"));
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
                PlayerInfo playerInfo = plugin.getPlayerInfo(player);
                suggestions.addAll(plugin.getChallengeLogic().getAvailableChallengeNames(playerInfo));
                filter(suggestions, args[args.length - 1]);
            }
            Collections.sort(suggestions);
            return suggestions;
        }
        return null;
    }
}

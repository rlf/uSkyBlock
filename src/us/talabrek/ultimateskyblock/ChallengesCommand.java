package us.talabrek.ultimateskyblock;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ChallengesCommand implements CommandExecutor, TabCompleter {
	private ArrayList<String> challengeNames = new ArrayList<String>(Settings.challenges_challengeList.size());

	public ChallengesCommand() {
		challengeNames.addAll(Settings.challenges_challengeList);
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String args[]) 
	{
		ArrayList<String> challenges = new ArrayList<String>();

		String find = args[0].toLowerCase();

		if (find.equals("c") && args.length == 2)
			find = args[1].toLowerCase();

		for (String name : challengeNames) {
			if (name.startsWith(find))
				challenges.add(name);
		}

		return challenges;
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) 
	{
		if (!(sender instanceof Player))
			return false;

		PlayerInfo pi = uSkyBlock.getInstance().getPlayer(sender.getName());
		Player player = (Player)sender;
		
		if (!Settings.challenges_allowChallenges)
		{
			sender.sendMessage(ChatColor.RED + "Challenges are not enabled");
			return true;
		}
		
		if (!VaultHandler.hasPerm(sender, "usb.island.challenges")) 
		{
			sender.sendMessage(ChatColor.RED + "You don't have access to this command!");
			return true;
		}
		if (!uSkyBlock.isSkyBlockWorld(((Player)sender).getWorld())) 
		{
			sender.sendMessage(ChatColor.RED + "You can only submit challenges in the skyblock world!");
			return true;
		}

		if (split.length == 0) 
		{
			int rankComplete = 0;
			sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[0] + ": " + uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[0]));
			for (int i = 1; i < Settings.challenges_ranks.length; i++) 
			{
				rankComplete = uSkyBlock.getInstance().checkRankCompletion(player, Settings.challenges_ranks[i - 1]);
				if (rankComplete <= 0)
					sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ": " + uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[i]));
				else
					sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ChatColor.GRAY + ": Complete " + rankComplete + " more " + Settings.challenges_ranks[i - 1] + " challenges to unlock this rank!");
			}
			sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
			sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
		} 
		else if (split.length == 1) 
		{
			if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c")) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
				sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
				sender.sendMessage(ChatColor.YELLOW + "Challenges will have different colors depending on if they are:");
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Settings.challenges_challengeColor + "Incomplete " + Settings.challenges_finishedColor + "Completed(not repeatable) " + Settings.challenges_repeatableColor + "Completed(repeatable) "));
			} 
			else if (uSkyBlock.getInstance().isRankAvailable(player, uSkyBlock.getInstance().getChallengeConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rankLevel"))) 
			{
				sender.sendMessage(ChatColor.YELLOW + "Challenge Name: " + ChatColor.WHITE + split[0].toLowerCase());
				sender.sendMessage(ChatColor.YELLOW + uSkyBlock.getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".description").toString()));
				if (uSkyBlock.getInstance().getChallengeConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onPlayer")) 
				{
					if (uSkyBlock.getInstance().getChallengeConfig().getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".takeItems")) 
						sender.sendMessage(ChatColor.RED + "You will lose all required items when you complete this challenge!");
				} 
				else if (uSkyBlock.getInstance().getChallengeConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onIsland")) 
					sender.sendMessage(ChatColor.RED + "All required items must be placed on your island!");

				if (Settings.challenges_ranks.length > 1)
					sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".rankLevel").toString()));
				
				if (pi.checkChallenge(split[0].toLowerCase()) && (!uSkyBlock.getInstance().getChallengeConfig().getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onPlayer") || !uSkyBlock.getInstance().getChallengeConfig().getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatable"))) 
				{
					sender.sendMessage(ChatColor.RED + "This Challenge is not repeatable!");
					return true;
				}
				if (Settings.challenges_enableEconomyPlugin && VaultHandler.econ != null) 
				{
					if (pi.checkChallenge(split[0].toLowerCase())) 
					{
						sender.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', uSkyBlock.getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".repeatRewardText").toString())));
						sender.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".repeatXpReward").toString()));
						sender.sendMessage(ChatColor.YELLOW + "Repeat currency reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".repeatCurrencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
					} 
					else 
					{
						sender.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', uSkyBlock.getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".rewardText").toString())));
						sender.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".xpReward").toString()));
						sender.sendMessage(ChatColor.YELLOW + "Currency reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".currencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
					}

				} 
				else if (pi.checkChallenge(split[0].toLowerCase())) 
				{
					sender.sendMessage(ChatColor.YELLOW + "Repeat reward(s): " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', uSkyBlock.getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".repeatRewardText").toString())));
					sender.sendMessage(ChatColor.YELLOW + "Repeat exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".repeatXpReward").toString()));
				} 
				else 
				{
					sender.sendMessage(ChatColor.YELLOW + "Reward(s): " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', uSkyBlock.getInstance().getChallengeConfig().getString(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".rewardText").toString())));
					sender.sendMessage(ChatColor.YELLOW + "Exp reward: " + ChatColor.WHITE + uSkyBlock.getInstance().getChallengeConfig().getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase()).append(".xpReward").toString()));
				}

				sender.sendMessage(ChatColor.YELLOW + "To complete this challenge, use " + ChatColor.WHITE + "/c c " + split[0].toLowerCase());
			} 
			else
				sender.sendMessage(ChatColor.RED + "Invalid challenge name! Use /c help for more information");
		} 
		else if (split.length == 2) 
		{
			if (split[0].equalsIgnoreCase("complete") || split[0].equalsIgnoreCase("c")) 
			{
				if (uSkyBlock.getInstance().checkIfCanCompleteChallenge(player, split[1].toLowerCase())) 
					uSkyBlock.getInstance().giveReward(player, split[1].toLowerCase());
			}
		}
		return true;
	}
}
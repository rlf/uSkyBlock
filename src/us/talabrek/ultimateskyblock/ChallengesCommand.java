package us.talabrek.ultimateskyblock;

import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ChallengesCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		/*  17 */if (!(sender instanceof Player)) {
			/*  18 */return false;
		}

		/*  21 */Player player = sender.getServer().getPlayer(sender.getName());
		/*  22 */if (!Settings.challenges_allowChallenges) {
			/*  24 */return true;
		}
		/*  26 */if ((!VaultHandler.checkPerk(player.getName(), "usb.island.challenges", player.getWorld())) && (!player.isOp())) {
			/*  27 */player.sendMessage(ChatColor.RED + "You don't have access to this command!");
			/*  28 */return true;
		}
		/*  30 */if (!player.getWorld().getName().equalsIgnoreCase(Settings.general_worldName)) {
			/*  31 */player.sendMessage(ChatColor.RED + "You can only submit challenges in the skyblock world!");
			/*  32 */return true;
		}

		/*  35 */if (split.length == 0) {
			/*  36 */int rankComplete = 0;
			/*  37 */sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[0] + ": "
					+ uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[0]));
			/*  38 */for (int i = 1; i < Settings.challenges_ranks.length; i++) {
				/*  40 */rankComplete = uSkyBlock.getInstance().checkRankCompletion(player, Settings.challenges_ranks[(i - 1)]);
				/*  41 */if (rankComplete <= 0) {
					/*  43 */sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ": "
							+ uSkyBlock.getInstance().getChallengesFromRank(player, Settings.challenges_ranks[i]));
				} else {
					/*  46 */sender.sendMessage(ChatColor.GOLD + Settings.challenges_ranks[i] + ChatColor.GRAY + ": Complete "
							+ rankComplete + " more " + Settings.challenges_ranks[(i - 1)] + " challenges to unlock this rank!");
				}
			}
			/*  49 */sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
			/*  50 */sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
			/*  51 */} else if (split.length == 1) {
			/*  53 */if ((split[0].equalsIgnoreCase("help")) || (split[0].equalsIgnoreCase("complete"))
					|| (split[0].equalsIgnoreCase("c"))) {
				/*  55 */sender.sendMessage(ChatColor.YELLOW + "Use /c <name> to view information about a challenge.");
				/*  56 */sender.sendMessage(ChatColor.YELLOW + "Use /c complete <name> to attempt to complete that challenge.");
				/*  57 */sender.sendMessage(ChatColor.YELLOW + "Challenges will have different colors depending on if they are:");
				/*  58 */sender.sendMessage(Settings.challenges_challengeColor.replace('&', '§') + "Incomplete "
						+ Settings.challenges_finishedColor.replace('&', '§') + "Completed(not repeatable) "
						+ Settings.challenges_repeatableColor.replace('&', '§') + "Completed(repeatable) ");
				/*  59 */} else if (uSkyBlock.getInstance().isRankAvailable(
					player,
					uSkyBlock.getInstance().getConfig()
							.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".rankLevel"))) {
				/*  61 */sender.sendMessage(ChatColor.YELLOW + "Challenge Name: " + ChatColor.WHITE + split[0].toLowerCase());
				/*  62 */sender.sendMessage(ChatColor.YELLOW
						+ uSkyBlock
								.getInstance()
								.getConfig()
								.getString(
										new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".description").toString()));
				/*  63 */if (uSkyBlock.getInstance().getConfig()
						.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onPlayer")) {
					/*  65 */if (uSkyBlock.getInstance().getConfig()
							.getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".takeItems")) {
						/*  67 */sender.sendMessage(ChatColor.RED + "You will lose all required items when you complete this challenge!");
					}
					/*  69 */} else if (uSkyBlock.getInstance().getConfig()
						.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type").equalsIgnoreCase("onIsland")) {
					/*  71 */sender.sendMessage(ChatColor.RED + "All required items must be placed on your island!");
				}

				/*  74 */if (Settings.challenges_ranks.length > 1) {
					/*  76 */sender.sendMessage(ChatColor.YELLOW
							+ "Rank: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
													.append(".rankLevel").toString()));
				}
				/*  78 */if ((((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).checkChallenge(split[0]
						.toLowerCase()))
						&& ((!uSkyBlock.getInstance().getConfig()
								.getString("options.challenges.challengeList." + split[0].toLowerCase() + ".type")
								.equalsIgnoreCase("onPlayer")) || (!uSkyBlock.getInstance().getConfig()
								.getBoolean("options.challenges.challengeList." + split[0].toLowerCase() + ".repeatable")))) {
					/*  80 */sender.sendMessage(ChatColor.RED + "This Challenge is not repeatable!");
					/*  81 */return true;
				}
				/*  83 */if ((Settings.challenges_enableEconomyPlugin) && (VaultHandler.econ != null)) {
					/*  85 */if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).checkChallenge(split[0]
							.toLowerCase())) {
						/*  87 */sender.sendMessage(ChatColor.YELLOW
								+ "Repeat reward(s): "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getString(
												new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
														.append(".repeatRewardText").toString()).replace('&', '§'));
						/*  88 */player.sendMessage(ChatColor.YELLOW
								+ "Repeat exp reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".repeatXpReward").toString()));
						/*  89 */sender
								.sendMessage(ChatColor.YELLOW
										+ "Repeat currency reward: "
										+ ChatColor.WHITE
										+ uSkyBlock
												.getInstance()
												.getConfig()
												.getInt(new StringBuilder("options.challenges.challengeList.")
														.append(split[0].toLowerCase()).append(".repeatCurrencyReward").toString()) + " "
										+ VaultHandler.econ.currencyNamePlural());
					} else {
						/*  92 */sender.sendMessage(ChatColor.YELLOW
								+ "Reward(s): "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getString(
												new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
														.append(".rewardText").toString()).replace('&', '§'));
						/*  93 */player.sendMessage(ChatColor.YELLOW
								+ "Exp reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".xpReward").toString()));
						/*  94 */sender.sendMessage(ChatColor.YELLOW
								+ "Currency reward: "
								+ ChatColor.WHITE
								+ uSkyBlock
										.getInstance()
										.getConfig()
										.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
												.append(".currencyReward").toString()) + " " + VaultHandler.econ.currencyNamePlural());
					}

				}
				/*  98 */else if (((PlayerInfo) uSkyBlock.getInstance().getActivePlayers().get(player.getName())).checkChallenge(split[0]
						.toLowerCase())) {
					/* 100 */sender.sendMessage(ChatColor.YELLOW
							+ "Repeat reward(s): "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
													.append(".repeatRewardText").toString()).replace('&', '§'));
					/* 101 */player.sendMessage(ChatColor.YELLOW
							+ "Repeat exp reward: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
											.append(".repeatXpReward").toString()));
				} else {
					/* 104 */sender.sendMessage(ChatColor.YELLOW
							+ "Reward(s): "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getString(
											new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
													.append(".rewardText").toString()).replace('&', '§'));
					/* 105 */player.sendMessage(ChatColor.YELLOW
							+ "Exp reward: "
							+ ChatColor.WHITE
							+ uSkyBlock
									.getInstance()
									.getConfig()
									.getInt(new StringBuilder("options.challenges.challengeList.").append(split[0].toLowerCase())
											.append(".xpReward").toString()));
				}

				/* 108 */sender.sendMessage(ChatColor.YELLOW + "To complete this challenge, use " + ChatColor.WHITE + "/c c "
						+ split[0].toLowerCase());
			} else {
				/* 111 */sender.sendMessage(ChatColor.RED + "Invalid challenge name! Use /c help for more information");
			}
			/* 113 */} else if (split.length == 2) {
			/* 115 */if ((split[0].equalsIgnoreCase("complete")) || (split[0].equalsIgnoreCase("c"))) {
				/* 117 */if (uSkyBlock.getInstance().checkIfCanCompleteChallenge(player, split[1].toLowerCase())) {
					/* 119 */uSkyBlock.getInstance().giveReward(player, split[1].toLowerCase());
				}
			}
		}
		/* 123 */return true;
	}
}
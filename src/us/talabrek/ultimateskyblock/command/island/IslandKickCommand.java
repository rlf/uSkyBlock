package us.talabrek.ultimateskyblock.command.island;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.ICommand;
import us.talabrek.ultimateskyblock.Misc;
import us.talabrek.ultimateskyblock.PlayerInfo;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class IslandKickCommand implements ICommand {

	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "remove" };
	}

	@Override
	public String getPermission() {
		return "usb.party.kick";
	}

	@Override
	public String getUsageString(String label, CommandSender sender) {
		return label + ChatColor.GOLD + " <player>|all";
	}

	@Override
	public String getDescription() {
		return "Removes a player from your island.";
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	private void removePlayer(PlayerInfo player, PlayerInfo owner, CommandSender sender) {
		Player onlinePlayer = player.getPlayer();

		if (onlinePlayer != null) {
			onlinePlayer.sendMessage(ChatColor.RED + "You have been kicked from " + owner.getPlayerName() + "'s skyblock.");
			if (uSkyBlock.isSkyBlockWorld(onlinePlayer.getWorld())) {

				if (Settings.extras_sendToSpawn)
					Misc.safeTeleport(onlinePlayer, Bukkit.getWorld("spawnworld").getSpawnLocation());
				else
					Misc.safeTeleport(onlinePlayer, uSkyBlock.getSkyBlockWorld().getSpawnLocation());
			}
		}

		sender.sendMessage(ChatColor.GREEN + player.getPlayerName() + " has been removed from the island.");

		player.setLeaveParty();
		player.setHomeLocation(null);

		owner.getMembers().remove(player.getPlayerName());

		if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
			WorldGuardHandler.removePlayerFromRegion(owner.getPlayerName(), player.getPlayerName());

		player.save();
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length != 1)
			return false;

		PlayerInfo info = uSkyBlock.getInstance().getPlayer(sender.getName());

		if (info == null) {
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
			return true;
		}

		if (!info.getHasParty()) {
			sender.sendMessage(ChatColor.RED + "You are not part of a party.");
			return true;
		}

		if (!info.getPartyLeader().equals(sender.getName())) {
			sender.sendMessage(ChatColor.RED + "You are not the party leader.");
			return true;
		}

		boolean all = false;

		PlayerInfo other = null;

		if (args[0].equalsIgnoreCase("all"))
			all = true;
		else {
			other = Misc.getPlayerInfo(args[0]);
			if (other == null) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[0]);
				return true;
			}

			if (other.getPlayerName().equals(sender.getName())) {
				sender.sendMessage(ChatColor.RED + "You cannot kick yourself.");
				return true;
			}
		}

		if (all) {
			ArrayList<String> members = new ArrayList<String>(info.getMembers());
			for (String member : members) {
				if (member.equals(sender.getName()))
					continue;

				other = uSkyBlock.getInstance().getPlayer(member);

				removePlayer(other, info, sender);
			}
		} else
			removePlayer(other, info, sender);

		if (info.getMembers().isEmpty() || (info.getMembers().size() == 1 && info.getMembers().contains(info.getPlayerName())))
			info.setLeaveParty();

		info.save();

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			ArrayList<String> players = new ArrayList<String>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					if (sender instanceof Player && ((Player) sender).canSee(player))
						players.add(player.getName());
				}
			}

			if ("all".startsWith(args[0].toLowerCase()))
				players.add("all");

			return players;
		}
		// TODO Auto-generated method stub
		return null;
	}

}

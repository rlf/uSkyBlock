package us.talabrek.ultimateskyblock.command.island;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.*;
import us.talabrek.ultimateskyblock.command.ICommand;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.model.Settings;
import us.talabrek.ultimateskyblock.model.UUIDPlayerInfo;

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

	private void removePlayer(UUIDPlayerInfo player, UUIDPlayerInfo owner, CommandSender sender) {
		OfflinePlayer offlinePlayer = player.getPlayer();

		if (offlinePlayer instanceof Player) {
            Player onlinePlayer = (Player)offlinePlayer;
            onlinePlayer.sendMessage(ChatColor.RED + "You have been kicked from " + owner.getPlayer().getName() + "'s skyblock.");
			if (uSkyBlock.isSkyBlockWorld(onlinePlayer.getWorld())) {

				if (Settings.extras_sendToSpawn)
					Misc.safeTeleport(onlinePlayer, Bukkit.getWorld("spawnworld").getSpawnLocation());
				else
					Misc.safeTeleport(onlinePlayer, uSkyBlock.getSkyBlockWorld().getSpawnLocation());
			}
		}

		sender.sendMessage(ChatColor.GREEN + player.getPlayer().getName() + " has been removed from the island.");

		player.setLeaveParty();
		player.setHomeLocation(null);

		owner.getMembers().remove(player.getPlayerUUID());

		if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
			WorldGuardHandler.removePlayerFromRegion(owner.getPlayer().getName(), player.getPlayer().getName());

		player.save();
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length != 1)
			return false;

		UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(((Player) sender).getUniqueId());

		if (info == null) {
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
			return true;
		}

		if (!info.getHasParty()) {
			sender.sendMessage(ChatColor.RED + "You are not part of a party.");
			return true;
		}

		boolean all = false;

		UUIDPlayerInfo other = null;

		if (args[0].equalsIgnoreCase("all"))
			all = true;
		else {
			other = Misc.getPlayerInfo(args[0]);
			if (other == null) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[0]);
				return true;
			}

			if (other.getPlayerUUID().equals(((Player)sender).getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "You cannot kick yourself.");
				return true;
			}

            if (other.getPlayer().getPlayer() != null &&
                    uSkyBlock.isSkyBlockWorld(other.getPlayer().getPlayer().getWorld()) &&
                    other.getPlayer().getPlayer().getLocation().distance(info.getIslandLocation()) < Settings.island_distance) {
                // if player, regardless of party, is on the owner's island, send to spawn
                if (Settings.extras_sendToSpawn)
                    Misc.safeTeleport(other.getPlayer().getPlayer(), Bukkit.getWorld("spawnworld").getSpawnLocation());
                else
                    Misc.safeTeleport(other.getPlayer().getPlayer(), uSkyBlock.getSkyBlockWorld().getSpawnLocation());
            }

			// todo: check if other party leader exists
			if (!other.getPartyLeader().equals(((Player)sender).getUniqueId())){
				sender.sendMessage(ChatColor.RED + args[0] + " is not a member of your party!");
				return true;
			}

		}

        if (!info.getPartyLeader().equals(((Player) sender).getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You are not the party leader.");
            return true;
        }

		if (all) {
			ArrayList<UUID> members = new ArrayList<UUID>(info.getMembers());
			for (UUID member : members) {
				if (member.equals(((Player)sender).getUniqueId()))
					continue;

				other = uSkyBlock.getInstance().getPlayer(member);

				removePlayer(other, info, sender);
			}
		} else
			removePlayer(other, info, sender);

		if (info.getMembers().isEmpty() || (info.getMembers().size() == 1 && info.getMembers().contains(info.getPlayerUUID())))
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

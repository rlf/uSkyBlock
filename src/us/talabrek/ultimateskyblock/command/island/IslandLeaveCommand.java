package us.talabrek.ultimateskyblock.command.island;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.*;

public class IslandLeaveCommand implements ICommand {

	@Override
	public String getName() {
		return "leave";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return "usb.party.create";
	}

	@Override
	public String getUsageString(String label, CommandSender sender) {
		return label;
	}

	@Override
	public String getDescription() {
		return "Removes you from whatever party you are part of";
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length != 0)
			return false;

		UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(((Player)sender).getUniqueId());

		if (info == null) {
			sender.sendMessage(ChatColor.RED + "You have not started skyblock. Please use " + ChatColor.YELLOW + "/island" + ChatColor.RED + " to begin");
			return true;
		}

		if (!info.getHasParty()) {
			sender.sendMessage(ChatColor.RED + "You are not part of a party. Use " + ChatColor.YELLOW + "/island restart" + ChatColor.RED + " to restart your island.");
			return true;
		}

		if (info.getPartyLeader().equals(sender.getName())) {
			sender.sendMessage(ChatColor.RED + "You are the leader of this party. Use " + ChatColor.YELLOW + "/island transfer <player>" + ChatColor.RED + " to transfer to someone else first.");
			return true;
		}

		Player player = (Player) sender;

		if (!uSkyBlock.isSkyBlockWorld(player.getWorld())) {
			sender.sendMessage(ChatColor.RED + "You need to be in skyblock to leave your party.");
			return true;
		}

		UUIDPlayerInfo leader = uSkyBlock.getInstance().getPlayer(info.getPartyLeader());

		info.setLeaveParty();
		info.setHomeLocation(null);

		leader.getMembers().remove(info.getPlayerUUID());

		if (Settings.extras_sendToSpawn)
			Misc.safeTeleport(player, Bukkit.getWorlds().get(0).getSpawnLocation());
		else
			Misc.safeTeleport(player, uSkyBlock.getSkyBlockWorld().getSpawnLocation());

		sender.sendMessage(ChatColor.YELLOW + "You have left that party.");

		if (leader.getPlayer() instanceof Player)
            ((Player)leader.getPlayer()).sendMessage(ChatColor.YELLOW + player.getName() + " has left your party.");

		if (leader.getMembers().isEmpty() || (leader.getMembers().size() == 1 && leader.getMembers().contains(leader.getPlayerUUID())))
			leader.setLeaveParty();

		if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
			WorldGuardHandler.removePlayerFromRegion(leader.getPlayer().getName(), player.getName());

		info.save();
		leader.save();

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		return null;
	}
}

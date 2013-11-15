package us.talabrek.ultimateskyblock;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler {
	public static Economy econ = null;
	public static Permission perms = null;

	public static void addGroup(final Player player, final String perk) {
		perms.playerAddGroup((String) null, player.getName(), perk);
	}

	public static void addPerk(final Player player, final String perk) {
		perms.playerAdd((String) null, player.getName(), perk);
	}
	
	public static boolean hasPerm(CommandSender sender, String perm)
	{
		if(sender instanceof Player)
			return checkPerk(sender.getName(), perm, ((Player)sender).getWorld());
		
		return sender.hasPermission(perm);
	}

	public static boolean checkPerk(final String player, final String perk, final World world) {
		if (perms.has((String) null, player, perk)) { return true; }
		if (perms.has(world, player, perk)) { return true; }
		return false;
	}

	public static void removePerk(final Player player, final String perk) {
		perms.playerRemove((String) null, player.getName(), perk);
	}

	public static boolean setupEconomy() {
		if (uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) { return false; }
		final RegisteredServiceProvider<Economy> rsp = uSkyBlock.getInstance().getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (rsp == null) { return false; }
		econ = rsp.getProvider();
		return econ != null;
	}

	public static boolean setupPermissions() {
		final RegisteredServiceProvider<Permission> rsp = uSkyBlock.getInstance().getServer().getServicesManager()
				.getRegistration(Permission.class);
		if (rsp.getProvider() != null) {
			perms = rsp.getProvider();
		}
		return perms != null;
	}
}
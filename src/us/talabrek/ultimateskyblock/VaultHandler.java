package us.talabrek.ultimateskyblock;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class VaultHandler
{
  public static Permission perms = null;
  public static Economy econ = null;
  
  public VaultHandler() {}
  
  public static void addPerk(Player player, String perk)
  {
    perms.playerAdd(null, player.getName(), perk);
  }
  
  public static void removePerk(Player player, String perk)
  {
    perms.playerRemove(null, player.getName(), perk);
  }
  
  public static void addGroup(Player player, String perk)
  {
    perms.playerAddGroup(null, player.getName(), perk);
  }
  
  public static boolean checkPerk(String player, String perk, World world)
  {
    if (perms.has(null, player, perk)) {
      return true;
    }
    if (perms.has(world, player, perk)) {
      return true;
    }
    return false;
  }
  
  public static boolean setupPermissions()
  {
    RegisteredServiceProvider<Permission> rsp = uSkyBlock.getInstance().getServer().getServicesManager().getRegistration(Permission.class);
    if (rsp.getProvider() != null) {
      perms = (Permission)rsp.getProvider();
    }
    return perms != null;
  }
  
  public static boolean setupEconomy()
  {
    if (uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = uSkyBlock.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = (Economy)rsp.getProvider();
    return econ != null;
  }
}

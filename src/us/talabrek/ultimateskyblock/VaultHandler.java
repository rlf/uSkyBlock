package us.talabrek.ultimateskyblock;

import net.milkbowl.vault.permission.*;
import net.milkbowl.vault.economy.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.plugin.*;

public class VaultHandler
{
    public static Permission perms;
    public static Economy econ;
    
    static {
        VaultHandler.perms = null;
        VaultHandler.econ = null;
    }
    
    public static void addPerk(final Player player, final String perk) {
        VaultHandler.perms.playerAdd((String)null, player.getName(), perk);
    }
    
    public static void removePerk(final Player player, final String perk) {
        VaultHandler.perms.playerRemove((String)null, player.getName(), perk);
    }
    
    public static void addGroup(final Player player, final String perk) {
        VaultHandler.perms.playerAddGroup((String)null, player.getName(), perk);
    }
    
    public static boolean checkPerk(final String player, final String perk, final World world) {
        return VaultHandler.perms.has((String)null, player, perk) || VaultHandler.perms.has(world, player, perk);
    }
    
    public static boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>)uSkyBlock.getInstance().getServer().getServicesManager().getRegistration((Class)Permission.class);
        if (rsp.getProvider() != null) {
            VaultHandler.perms = (Permission)rsp.getProvider();
        }
        return VaultHandler.perms != null;
    }
    
    public static boolean setupEconomy() {
        if (uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)uSkyBlock.getInstance().getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (rsp == null) {
            return false;
        }
        VaultHandler.econ = (Economy)rsp.getProvider();
        return VaultHandler.econ != null;
    }
}

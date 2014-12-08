package us.talabrek.ultimateskyblock.handler;

import net.milkbowl.vault.permission.*;
import net.milkbowl.vault.economy.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import us.talabrek.ultimateskyblock.uSkyBlock;

public enum VaultHandler {;
    private static Permission perms;
    private static Economy econ;

    static {
        perms = null;
        econ = null;
    }

    public static void init(Permission perms, Economy econ) {
        VaultHandler.perms = perms;
        VaultHandler.econ = econ;
    }

    public static void addPerk(final Player player, final String perk) {
        perms.playerAdd((String) null, player.getName(), perk);
    }

    public static void removePerk(final Player player, final String perk) {
        perms.playerRemove((String) null, player.getName(), perk);
    }

    public static void addGroup(final Player player, final String perk) {
        perms.playerAddGroup((String) null, player.getName(), perk);
    }

    public static boolean checkPerk(final String player, final String perk, final World world) {
        return perms.has((String) null, player, perk) || perms.has(world, player, perk);
    }

    public static boolean checkPerm(final Player player, final String perm, final World world) {
        if (perm == null || perm.trim().isEmpty()) {
            return true;
        }
        // TODO: UUID aware
        return perms.has(player, perm) || perms.has(world, player.getName(), perm);
    }

    public static boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>) uSkyBlock.getInstance().getServer().getServicesManager().getRegistration((Class) Permission.class);
        if (rsp.getProvider() != null) {
            perms = rsp.getProvider();
        }
        return perms != null;
    }

    public static boolean setupEconomy() {
        if (uSkyBlock.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>) uSkyBlock.getInstance().getServer().getServicesManager().getRegistration((Class) Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static boolean hasEcon() {
        return econ != null;
    }

    // TODO: UUID aware
    public static void depositPlayer(String name, double v) {
        econ.depositPlayer(name, v);
    }

    public static Economy getEcon() {
        return econ;
    }
}

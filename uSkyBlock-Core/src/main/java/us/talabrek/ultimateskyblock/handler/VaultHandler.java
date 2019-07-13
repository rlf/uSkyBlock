package us.talabrek.ultimateskyblock.handler;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import us.talabrek.ultimateskyblock.uSkyBlock;

public enum VaultHandler {;
    private static Permission perms;
    private static Economy econ;

    static {
        perms = null;
        econ = null;
    }

    public static void addPermission(final Player player, final String perk) {
        perms.playerAdd(player, perk);
    }

    public static void removePermission(final Player player, final String perk) {
        perms.playerRemove(player, perk);
    }

    public static boolean hasPermission(final Player player, final String perk) {
        return perms.playerHas(player, perk);
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

    public static String getItemName(ItemStack stack) {
        return ItemStackUtil.getItemName(stack);
    }

    public static boolean hasEcon() {
        return econ != null;
    }

    public static void depositPlayer(Player player, double v) {
        econ.depositPlayer(player, v);
    }

    public static Economy getEcon() {
        return econ;
    }

}

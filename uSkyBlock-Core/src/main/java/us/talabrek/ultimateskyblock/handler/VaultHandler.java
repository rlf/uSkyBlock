package us.talabrek.ultimateskyblock.handler;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import us.talabrek.ultimateskyblock.uSkyBlock;

public enum VaultHandler {;
    private static Permission perms;

    static {
        perms = null;
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

    public static String getItemName(ItemStack stack) {
        return ItemStackUtil.getItemName(stack);
    }

}

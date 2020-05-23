package us.talabrek.ultimateskyblock.hook.permissions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.hook.PluginHook;
import us.talabrek.ultimateskyblock.uSkyBlock;

public abstract class PermissionsHook extends PluginHook {
    public PermissionsHook(@NotNull uSkyBlock plugin, @NotNull String implementing) {
        super(plugin, "Permissions", implementing);
    }

    /**
     * Add permission to a player ONLY for the world the player is currently on. This is a world-specific operation.
     * @param player Player Object
     * @param perk Permission node
     * @return Success or Failure
     */
    public abstract boolean addPermission(@NotNull Player player, @NotNull String perk);

    /**
     * Remove permission from a player. This is a world-specific operation.
     * @param player Player Object
     * @param perk Permission node
     * @return Success or Failure
     */
    public abstract boolean removePermission(@NotNull Player player, @NotNull String perk);

    /**
     * Gets the value of the specified permission, if set. If a permission override is not set on this object,
     * the default value of the permission will be returned.
     * @param perk Name of the permission
     * @return Value of the permission
     */
    public boolean hasPermission(@NotNull Player player, @NotNull String perk) {
        return player.hasPermission(perk);
    }
}

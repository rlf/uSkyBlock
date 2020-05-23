package us.talabrek.ultimateskyblock.hook.permissions;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Optional;

public class VaultPermissions extends PermissionsHook {
    private Permission permission;

    public VaultPermissions(@NotNull uSkyBlock plugin) {
        super(plugin, "Vault");
        setupPermission().ifPresent((permission) -> this.permission = permission);
    }

    private Optional<Permission> setupPermission() {
        RegisteredServiceProvider<Permission> rsp =
            plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            permission = rsp.getProvider();
            return Optional.of(permission);
        }

        return Optional.empty();
    }

    @Override
    public boolean addPermission(@NotNull Player player, @NotNull String perk) {
        return permission.playerAdd(player, perk);
    }

    @Override
    public boolean removePermission(@NotNull Player player, @NotNull String perk) {
        return permission.playerRemove(player, perk);
    }
}

package us.talabrek.ultimateskyblock.hook;

import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.hook.economy.EconomyHook;
import us.talabrek.ultimateskyblock.hook.economy.VaultEconomy;
import us.talabrek.ultimateskyblock.hook.permissions.PermissionsHook;
import us.talabrek.ultimateskyblock.hook.permissions.VaultPermissions;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HookManager {
    private final uSkyBlock plugin;
    private final Map<String, PluginHook> hooks = new ConcurrentHashMap<>();

    public HookManager(@NotNull uSkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns an {@link Optional} containing the requested {@link PluginHook}, or null if the hook is not available.
     * @param hook Name of the requested hook.
     * @return Optional containing the requested PluginHook, or null if unavailable.
     */
    public Optional<PluginHook> getHook(String hook) {
        return Optional.ofNullable(hooks.get(hook));
    }

    /**
     * Short method for {@link #getHook(String)} to get the optional {@link EconomyHook}.
     * @return optional of EconomyHook.
     */
    public Optional<EconomyHook> getEconomyHook() {
        return Optional.ofNullable((EconomyHook) getHook("Economy").orElse(null));
    }

    /**
     * Short method for {@link #getHook(String)} to get the optional {@link PermissionsHook}.
     * @return optional of PermissionsHook.
     */
    public Optional<PermissionsHook> getPermissionsHook() {
        return Optional.ofNullable((PermissionsHook) getHook("Permissions").orElse(null));
    }

    /**
     * Tries to enable the hook in the given {@link PluginHook}. Adds the plugin hook to the list of enabled hooks
     * if successfull. Throws a {@link HookFailedException} otherwise.
     * @param hook Hook to enable and register.
     * @throws HookFailedException if hooking into the plugin failes.
     */
    public void registerHook(PluginHook hook) throws HookFailedException {
        hook.onHook();
        hooks.put(hook.getHookName(), hook);
    }

    /**
     * Checks and hooks if there are compatible Economy plugins available.
     * @return True if a compatible Economy plugin has been found and hooking succeeded, false otherwise.
     */
    public boolean setupEconomyHook() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                VaultEconomy vault = new VaultEconomy(plugin);
                registerHook(vault);
                plugin.getLogger().info("Hooked into Vault economy");
                return true;
            }
        } catch (HookFailedException ex) {
            plugin.getLogger().warning("Failed to hook into Vault economy.");
            ex.printStackTrace();
        }

        plugin.getLogger().info("Failed to find a compatible economy system. Economy rewards will be disabled.");
        return false;
    }

    /**
     * Checks and hooks if there are compatible Permissions plugins available.
     * @return True if a compatible Permissions plugin has geen found and hooking succeeded, false otherwise.
     */
    public boolean setupPermissionsHook() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                VaultPermissions vault = new VaultPermissions(plugin);
                registerHook(vault);
                plugin.getLogger().info("Hooked into Vault permissions.");
                return true;
            }
        } catch (HookFailedException ex) {
            plugin.getLogger().warning("Failed to hook into Vault permissions plugin.");
            ex.printStackTrace();
        }

        plugin.getLogger().warning("Failed to find a compatible permissions system. Permission rewards " +
            "will be disabled.");
        return false;
    }
}

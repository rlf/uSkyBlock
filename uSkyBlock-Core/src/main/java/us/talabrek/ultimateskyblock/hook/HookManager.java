package us.talabrek.ultimateskyblock.hook;

import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HookManager {
    private final uSkyBlock plugin;
    private Map<String, PluginHook> hooks = new ConcurrentHashMap<>();

    public HookManager(@NotNull uSkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets a {@link List} of all enabled hooks.
     * @return List of all enabled hooks.
     */
    public @NotNull List<String> getEnabledHooks() {
        return new ArrayList<>(hooks.keySet());
    }

    /**
     * Returns an {@link Optional} containing the requested {@link PluginHook}, or null if the hook is not available.
     * @param hook Name of the requested hook.
     * @return Optional containing the requested PluginHook, or null if unavailable.
     */
    public Optional<? extends PluginHook> getHook(String hook) {
        return Optional.ofNullable(hooks.get(hook));
    }

    /**
     * Returns the requested {@link PluginHook}. Throws a {@link NullPointerException} if the hook is not available.
     * This method is designed to be used for hooks that we check on load, e.g. WorldEdit.
     * @param hook Name of the requested hook.
     * @return Requested PluginHook
     * @throws NullPointerException If the requested hook is unavailable.
     */
    public @NotNull PluginHook getRequiredHook(String hook) throws NullPointerException {
        PluginHook foundHook = hooks.get(hook);
        if (foundHook == null) {
            throw new NullPointerException("No required hook found for: " + hook);
        }
        return foundHook;
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
}

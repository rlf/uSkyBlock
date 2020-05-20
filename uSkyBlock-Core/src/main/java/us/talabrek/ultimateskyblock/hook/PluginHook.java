package us.talabrek.ultimateskyblock.hook;

import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.uSkyBlock;

public abstract class PluginHook {
    protected final uSkyBlock plugin;
    private final String hookName;
    private final String implementing;

    /**
     * PluginHook constructor.
     * @param plugin uSkyBlock instance
     * @param hookName The name of this plugin hook.
     * @param implementing The name of the actual plugin that this hook will implement.
     */
    public PluginHook(@NotNull uSkyBlock plugin, @NotNull String hookName, @NotNull String implementing) {
        this.plugin = plugin;
        this.hookName = hookName;
        this.implementing = implementing;
    }

    /**
     * Called when the hook is enabled. Throws {@link HookFailedException} if the hooking fails.
     * @throws HookFailedException if hooking fails.
     */
    public void onHook() throws HookFailedException {

    }

    /**
     * Called when the hook is disabled. Throws {@link HookFailedException} if the unhooking fails.
     * @throws HookFailedException if unhooking fails.
     */
    public void onUnhook() throws HookFailedException {

    }

    /**
     * Gets the configured name of this hook.
     * @return Hook name
     */
    public @NotNull String getHookName() {
        return hookName;
    }

    /**
     * Gets the name of the plugin that this hook implements.
     * @return Name of the plugin that this hook implements.
     */
    public @NotNull String getImplementing() {
        return implementing;
    }
}

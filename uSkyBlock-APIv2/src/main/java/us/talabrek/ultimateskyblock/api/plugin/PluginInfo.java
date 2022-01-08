package us.talabrek.ultimateskyblock.api.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Provides general information about the Ultimate Skyblock plugin instance running.
 */
public interface PluginInfo {
    /**
     * Gets the plugin version running on the server.
     * @return Plugin version running.
     */
    @NotNull String getPluginVersion();

    /**
     * Gets the {@link UpdateChecker}, which provides various information about the current and latest
     * Ultimate Skyblock releases.
     * @return Update checker for Ultimate Skyblock.
     */
    @NotNull UpdateChecker getUpdateChecker();
}

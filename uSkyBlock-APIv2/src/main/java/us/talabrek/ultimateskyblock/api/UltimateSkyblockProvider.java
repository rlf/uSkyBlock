package us.talabrek.ultimateskyblock.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class UltimateSkyblockProvider {
    private static UltimateSkyblock instance = null;

    /**
     * Gets the API instance of {@link UltimateSkyblock}, will throw {@link IllegalStateException} when the
     * API isn't loaded yet.
     *
     * Convenience method, using Bukkit's {@link org.bukkit.plugin.ServicesManager} is the preferred way to get
     * an API instance.
     * @return UltimateSkyblock API instance.
     * @throws IllegalStateException when the API isn't loaded yet.
     */
    public static @NotNull UltimateSkyblock getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UltimateSkyblock isn't loaded yet!");
        }
        return instance;
    }

    /**
     * Internal method - Registers the uSkyBlock plugin instance with the API provider.
     * @param instance uSkyBlock plugin instance
     */
    @ApiStatus.Internal
    public static void registerPlugin(UltimateSkyblock instance) {
        UltimateSkyblockProvider.instance = instance;
    }

    /**
     * Internal method - Deregisters the uSkyBlock plugin instance with the API provider.
     */
    @ApiStatus.Internal
    public static void deregisterPlugin() {
        UltimateSkyblockProvider.instance = null;
    }

    /**
     * No instance of this class should exist.
     */
    private UltimateSkyblockProvider() {}
}

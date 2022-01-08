package us.talabrek.ultimateskyblock.api.impl;

import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.api.UltimateSkyblock;
import us.talabrek.ultimateskyblock.api.plugin.PluginInfo;
import us.talabrek.ultimateskyblock.api.plugin.UpdateChecker;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class UltimateSkyblockApi implements UltimateSkyblock, PluginInfo {
    private final uSkyBlock plugin;

    public UltimateSkyblockApi(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull PluginInfo getPluginInfo() {
        return this;
    }

    /* PluginInfo impl */

    @Override
    public @NotNull String getPluginVersion() {
        return plugin.getUpdateChecker().getCurrentVersion();
    }

    @Override
    public @NotNull UpdateChecker getUpdateChecker() {
        return plugin.getUpdateChecker();
    }
}

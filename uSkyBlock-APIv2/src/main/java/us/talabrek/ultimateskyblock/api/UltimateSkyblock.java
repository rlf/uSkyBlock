package us.talabrek.ultimateskyblock.api;

import org.jetbrains.annotations.NotNull;
import us.talabrek.ultimateskyblock.api.plugin.PluginInfo;

public interface UltimateSkyblock {
    /**
     * Gets the {@link PluginInfo}, providing general information about this Ultimate Skyblock instance.
     * @return General plugin information.
     */
    @NotNull PluginInfo getPluginInfo();
}

package us.talabrek.ultimateskyblock;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import java.util.logging.Level;

public class MetricsManager {
    // Pushing to two accounts at the moment to track both legacy and current installations.
    private static final int BSTATS_MUSPAH_ID = 7525;
    private static final int BSTATS_RLF_ID = 2801;

    private final uSkyBlock plugin;

    public MetricsManager(uSkyBlock plugin) {
        this.plugin = plugin;

        try {
            setupMetrics(BSTATS_MUSPAH_ID);
            setupMetrics(BSTATS_RLF_ID);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to setup bStats metrics:", ex);
        }
    }

    private void setupMetrics(int pluginId) {
        Metrics bStats = new Metrics(plugin, pluginId);
        bStats.addCustomChart(new SimplePie("language",
            () -> plugin.getConfig().getString("language", "en")));
        bStats.addCustomChart(new SimplePie("radius_and_distance",
            () -> String.format("(%d,%d)", Settings.island_radius, Settings.island_distance)));

        // Temp. chart to measure storage usage for (legacy) uuid.PlayerDB.
        bStats.addCustomChart(new SimplePie("playerdb_type",
            () -> plugin.getConfig().getString("options.advanced.playerdb.storage", "yml")));
    }
}

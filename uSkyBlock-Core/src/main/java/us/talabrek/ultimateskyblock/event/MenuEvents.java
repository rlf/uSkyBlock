package us.talabrek.ultimateskyblock.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Menu events.
 */
public class MenuEvents implements Listener {
    private final uSkyBlock plugin;

    public MenuEvents(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void guiClick(final InventoryClickEvent event) {
        if (stripFormatting(event.getInventory().getTitle()).startsWith(stripFormatting(tr("Config:")))) {
            plugin.getConfigMenu().onClick(event);
        } else {
            plugin.getMenu().onClick(event);
        }
    }
}

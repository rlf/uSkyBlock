package us.talabrek.ultimateskyblock.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static dk.lockfuglsang.minecraft.util.FormatUtil.stripFormatting;

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
        String prefix = stripFormatting(tr("Config:"));
        if (!prefix.isEmpty() && stripFormatting(plugin.getMenu().getInventoryManager().getInventoryName((Player) event.getWhoClicked())).startsWith(prefix)) {
            plugin.getConfigMenu().onClick(event);
        } else {
            plugin.getMenu().onClick(event);
        }
    }
}

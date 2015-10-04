package us.talabrek.ultimateskyblock.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Common interface for menus.
 */
public interface EditMenu {
    /**
     * Returns true if the action belonged to the menu.
     * @param e A click on an item, not sure if it belongs on this menu.
     * @return <code>true</code> if no further processing is needed.
     */
    boolean onClick(InventoryClickEvent e);

    /**
     * Creates an edit menu for this type.
     * @param configName Configuration name (filename)
     * @param path       Path to node
     * @param page       The page to show (for multi-page editors)
     * @return           An editor or <code>null</code> if this is not the right edit-menu.
     */
    Inventory createEditMenu(String configName, String path, int page);
}

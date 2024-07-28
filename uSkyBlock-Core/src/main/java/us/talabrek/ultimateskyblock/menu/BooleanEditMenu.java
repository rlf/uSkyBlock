package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Simple menu-less editor for boolean values.
 */
public class BooleanEditMenu extends AbstractConfigMenu implements EditMenu {
    public BooleanEditMenu(FileConfiguration menuConfig) {
        super(menuConfig);
    }

    @Override
    public boolean onClick(InventoryClickEvent e) {
        // The boolean menu is never active
        return false;
    }

    @Override
    public Inventory createEditMenu(String configName, String path, int page) {
        FileConfiguration config = FileUtil.getYmlConfiguration(configName);
        if (config.isBoolean(path)) {
            boolean value = config.getBoolean(path);
            config.set(path, !value);
            config.set("dirty", true);
        }
        // never returns an editor...
        return null;
    }
}

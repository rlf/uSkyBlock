package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * A GUI for managing the uSkyBlock config-files
 */
public class ConfigMenu {

    private final YmlConfiguration menuConfig;
    private final MenuItemFactory factory;
    private final List<EditMenu> editMenus = new ArrayList<>();
    private final MainConfigMenu mainMenu;

    public ConfigMenu(uSkyBlock plugin) {
        menuConfig = new YmlConfiguration();
        FileUtil.readConfig(menuConfig, getClass().getClassLoader().getResourceAsStream("configmenu.yml"));
        factory = new MenuItemFactory();
        mainMenu = new MainConfigMenu(plugin, menuConfig, factory, editMenus);
        editMenus.add(new IntegerEditMenu(menuConfig, factory, mainMenu));
        editMenus.add(new BooleanEditMenu(menuConfig));
        editMenus.add(new StringEditMenu(menuConfig, mainMenu));
        editMenus.add(mainMenu); // mainMenu goes last (catch all)
    }

    public void showMenu(Player player, String configName, int page) {
        player.openInventory(mainMenu.createEditMenu(configName, null, page));
    }

    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        for (EditMenu editMenu : editMenus) {
            if (editMenu.onClick(event)) {
                break;
            }
        }
    }
}

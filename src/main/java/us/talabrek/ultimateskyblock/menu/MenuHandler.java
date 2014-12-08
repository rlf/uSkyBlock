package us.talabrek.ultimateskyblock.menu;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.provider.ConfigProvider;
import us.talabrek.ultimateskyblock.provider.MenuProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Responsible for tracking various menu-interactions for the user.
 */
public class MenuHandler implements Listener, MenuProvider {
    private static final Logger logger = Logger.getLogger(MenuHandler.class.getName());
    private final ConfigProvider configProvider;
    private FileConfiguration currentConfig;
    private final MenuProvider menuProvider;
    private final EvaluatorProvider provider;

    private Map<String, Menu> menus = new HashMap<>();
    private Map<UUID, String> currentMenus = new HashMap<>();

    public MenuHandler(ConfigProvider configProvider, MenuProvider menuProvider, EvaluatorProvider provider) {
        this.configProvider = configProvider;
        this.menuProvider = menuProvider;
        menuProvider.setParent(this);
        this.provider = provider;
        reloadConfig();
    }

    private void reloadConfig() {
        // Reference comparison on purpose
        FileConfiguration config = configProvider.getConfig();
        if (config != currentConfig) {
            currentConfig = config;
            menus.clear();
            for (Menu menu : ConfigMenuReader.readMenus(currentConfig.getConfigurationSection("menus"))) {
                menus.put(menu.getTitle(), menu);
            }
        }
    }

    public void showMenu(Player player, String menuName) {
        reloadConfig();
        Menu menu = getMenu(player, menuName);
        if (menu != null) {
            Inventory inventory = createInventory(player, menu);
            player.openInventory(inventory);
            currentMenus.put(player.getUniqueId(), menu.getTitle());
        } else {
            logger.warning("No menu named " + menuName + " configured!");
        }
    }

    private Inventory createInventory(Player player, Menu menu) {
        ParameterEvaluator evaluator = provider.getEvaluator(player);
        InventoryCreatorVisitor visitor = new InventoryCreatorVisitor(player, evaluator);
        menu.accept(visitor);
        return visitor.getInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        if (player != null) {
            currentMenus.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (event != null && human instanceof Player && event.getCurrentItem() != null) {
            reloadConfig();
            Player player = (Player) human;
            String title = event.getInventory().getTitle();
            if (menus.containsKey(title) || currentMenus.values().contains(title)){
                event.setCancelled(true);
            }
            ParameterEvaluator evaluator = provider.getEvaluator(player);
            Menu menu = getCurrentMenu(player);
            if (menu != null) {
                MenuItem menuItem = getMenuItem(menu, event.getCurrentItem());
                if (menuItem != null) {
                    event.setCancelled(true);
                    player.closeInventory();
                    if (menuProvider.onClick(player, menu, menuItem)) {
                        // Do something?
                    } else {
                        for (String cmd : menuItem.getCommands()) {
                            if (cmd != null && !cmd.trim().isEmpty()) {
                                String cmdEval = evaluator.eval(cmd);
                                player.performCommand(cmdEval);
                            }
                        }
                        if (menuItem.getSubMenu() != null) {
                            showMenu(player, menuItem.getSubMenu());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(InventoryInteractEvent event) {
        cancelMenuDrops(event.getInventory(), event);
    }

    private void cancelMenuDrops(Inventory inventory, Cancellable event) {
        if (inventory != null) {
            if (menus.containsKey(inventory.getTitle()) || currentMenus.values().contains(inventory.getTitle())) {
                event.setCancelled(true);
            }
        }
    }

    private MenuItem getMenuItem(Menu menu, ItemStack currentItem) {
        if (menu == null || currentItem == null || currentItem.getItemMeta() == null) {
            return null;
        }
        return menu.findItem(currentItem.getType().name(), currentItem.getItemMeta().getDisplayName());
    }

    private Menu getCurrentMenu(Player player) {
        if (player != null && currentMenus.containsKey(player.getUniqueId())) {
            String menuName = currentMenus.get(player.getUniqueId());
            return getMenu(player, menuName);
        }
        return null;
    }

    @Override
    public Menu getMenu(Player player, String menuName) {
        Menu menu = menuProvider.getMenu(player, menuName);
        if (menu == null) {
            menu = menus.get(menuName);
        }
        return menu;
    }

    @Override
    public boolean onClick(Player player, Menu menu, MenuItem clickedItem) {
        // Clicks propagate down, not up
        return false;
    }

    @Override
    public void setParent(MenuProvider parentProvider) {
        // Not used (we are the root provider)
    }
}

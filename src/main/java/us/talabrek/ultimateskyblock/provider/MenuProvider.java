package us.talabrek.ultimateskyblock.provider;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.menu.Menu;
import us.talabrek.ultimateskyblock.menu.MenuItem;

/**
 * Programmatically created menus
 */
public interface MenuProvider {
    Menu getMenu(Player player, String menuName);
    boolean onClick(Player player, Menu menu, MenuItem clickedItem);
    void setParent(MenuProvider parentProvider);
}

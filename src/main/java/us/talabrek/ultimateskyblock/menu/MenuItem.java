package us.talabrek.ultimateskyblock.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * An Immutable object representing a menu-item.
 */
public class MenuItem {
    private final String icon;
    private final String title;
    private final List<String> lore;
    private final String subMenu;
    private final List<String> commands;
    private final int index;
    private final String enabled;

    MenuItem(String icon, String title, List<String> lore, String subMenu, List<String> commands, int index, String enabled) {
        this.icon = icon;
        this.title = title;
        this.lore = lore;
        this.subMenu = subMenu;
        this.commands = commands;
        this.index = index;
        this.enabled = enabled;
    }

    public ItemStack getIconItemStack() {
        if (icon != null) {
            String[] split = icon.split(":");
            String matName = split[0];
            int subItem = 0;
            if (split.length == 2) {
                subItem = Integer.parseInt(split[1]);
            }
            Material type = Material.matchMaterial(matName);
            if (type != null) {
                return new ItemStack(type, 1, (short) subItem);
            } else {
                ItemStack itemStack = new ItemStack(Material.SKULL, 1);
                ((SkullMeta)itemStack.getItemMeta()).setOwner(matName);
            }
        }
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getSubMenu() {
        return subMenu;
    }

    public List<String> getCommands() {
        return commands;
    }

    public int getIndex() {
        return index;
    }

    public String getEnabled() {
        return enabled;
    }

    public void accept(MenuVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "icon=" + icon +
                ", title='" + title + '\'' +
                '}';
    }
}

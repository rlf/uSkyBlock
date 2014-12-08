package us.talabrek.ultimateskyblock.menu;

import java.util.List;

/**
 * An Immutable representation of a menu.
 */
public class Menu {
    private final String title;
    private final int size;
    private final List<MenuItem> items;

    public Menu(String title, int size, List<MenuItem> items) {
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void accept(MenuVisitor visitor) {
        visitor.visit(this);
        for (MenuItem item : items) {
            item.accept(visitor);
        }
    }

    public MenuItem findItem(String icon, String title) {
        for (MenuItem item : items) {
            if ((icon == null || icon.equals(item.getIconItemStack().getType().name())) && title.equals(item.getTitle())) {
                return item;
            }
        }
        return null;
    }
}

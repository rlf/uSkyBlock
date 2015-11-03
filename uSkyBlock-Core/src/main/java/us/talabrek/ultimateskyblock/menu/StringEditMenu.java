package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;

/**
 * Complex editor for string values (only simple keyboard, no extended).
 */
public class StringEditMenu extends AbstractConfigMenu implements EditMenu {
    private final EditMenu parent;
    private ArrayList<ItemStack> keyboard = new ArrayList<>();
    private ArrayList<ItemStack> capslockOverlay = new ArrayList<>();

    private ItemStack capsOn;
    private ItemStack capsOff;

    private final int capsIndex;
    private final int backspaceIndex;
    private final int returnIndex;

    public StringEditMenu(YmlConfiguration menuConfig, EditMenu parent) {
        super(menuConfig);
        this.parent = parent;
        List<?> characterMap = menuConfig.getList("keyboard.en");
        ensureCapacity(keyboard, 9*6);
        ensureCapacity(capslockOverlay, 9*6);
        int row = 0;
        for (Object o : characterMap) {
            int col = 0;
            if (o instanceof List) {
                for (String item : (List<String>)o) {
                    ItemStack character = createItem(item);
                    if (character != null) {
                        keyboard.set(getIndex(row, col), character);
                    }
                    col++;
                }
            }
            row++;
        }
        ConfigurationSection overlay = menuConfig.getConfigurationSection("keyboard.capslock.overlay");
        if (overlay != null) {
            for (String key : overlay.getKeys(false)) {
                int index = Integer.parseInt(key, 10);
                ItemStack item = createItem(overlay.getString(key));
                if (item != null) {
                    capslockOverlay.set(index, item);
                }
            }
        }
        capsOn = createItem(menuConfig.getString("keyboard.capslock.true"));
        capsOff = createItem(menuConfig.getString("keyboard.capslock.false"));
        keyboard.set(getIndex(0,0), createItem(Material.WOOD_DOOR, "\u00a79" + tr("Return"), null));
        capsIndex = getIndex(5, 0);
        keyboard.set(capsIndex, capsOff);
        backspaceIndex = getIndex(0, 5);
        returnIndex = 0;
    }

    @Override
    public boolean onClick(InventoryClickEvent e) {
        if (!stripFormatting(e.getInventory().getTitle()).equals(stripFormatting(getTitle()))) {
            return false;
        }
        Player player = (Player) e.getWhoClicked();
        ItemStack returnItem = e.getInventory().getItem(0);
        String configName = returnItem.getItemMeta().getLore().get(0);
        String path = returnItem.getItemMeta().getLore().get(1);
        int page = getPage(returnItem.getItemMeta().getLore().get(2));
        ItemStack currentItem = e.getCurrentItem();
        boolean isCaps = e.getInventory().getItem(capsIndex).getItemMeta().getDisplayName().equals(tr("Caps On"));
        if (currentItem != null) {
            YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
            String value = config.getString(path);
            if (e.getSlot() == capsIndex) {
                // Toggle caps
                ItemStack capsItem = isCaps ? capsOff.clone() : capsOn.clone();
                ItemMeta meta = capsItem.getItemMeta();
                meta.setLore(Arrays.asList(value));
                capsItem.setItemMeta(meta);
                e.getInventory().setItem(capsIndex, capsItem);
                isCaps = !isCaps;
            } else if (e.getSlot() == backspaceIndex) {
                if (value.length() > 0) {
                    value = value.substring(0, value.length() - 1);
                    config.set(path, value);
                    config.set("dirty", true);
                }
            } else if (e.getSlot() == returnIndex) {
                player.openInventory(parent.createEditMenu(configName, path, page));
                return true;
            } else if (currentItem.getType() == Material.SKULL_ITEM) {
                String character = stripFormatting(currentItem.getItemMeta().getDisplayName());
                if (character.isEmpty()) {
                    character = " ";
                }
                value += isCaps ? character.toUpperCase() : character.toLowerCase();
                config.set(path, value);
                config.set("dirty", true);
            }
            // re-load the ui (refresh)
            player.openInventory(createEditMenuInternal(configName, path, page, isCaps));
        }
        return true;
    }

    @Override
    public Inventory createEditMenu(String configName, String path, int page) {
        return createEditMenuInternal(configName, path, page, false);
    }

    private Inventory createEditMenuInternal(String configName, String path, int page, boolean isCaps) {
        YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
        if (!config.isString(path)) {
            return null;
        }
        String value = config.getString(path, "");
        Inventory menu = Bukkit.createInventory(null, 9 * 6, getTitle());
        setCharacters(menu, value, keyboard);
        if (isCaps) {
            setCharacters(menu, value, capslockOverlay);
        }
        ItemStack returnItem = menu.getItem(0).clone();
        ItemMeta meta = returnItem.getItemMeta();
        meta.setLore(Arrays.asList(configName, path, tr("Page {0,number,integer}", page)));
        returnItem.setItemMeta(meta);
        menu.setItem(0, returnItem);

        ItemStack capsItem = isCaps ? capsOn.clone() : capsOff.clone();
        ItemMeta itemMeta = capsItem.getItemMeta();
        itemMeta.setLore(Arrays.asList(value));
        capsItem.setItemMeta(itemMeta);
        menu.setItem(capsIndex, capsItem);

        return menu;
    }

    private void setCharacters(Inventory menu, String value, ArrayList<ItemStack> itemList) {
        int index = 0;
        for (ItemStack item : itemList) {
            if (item != null) {
                ItemStack clone = item.clone();
                ItemMeta itemMeta = clone.getItemMeta();
                itemMeta.setLore(Arrays.asList(value));
                clone.setItemMeta(itemMeta);
                menu.setItem(index, clone);
            }
            index++;
        }
    }

    private String getTitle() {
        return tr("Config:") + " " + tr("\u00a79Text Editor");
    }
}

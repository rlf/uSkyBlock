package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.file.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.talabrek.ultimateskyblock.player.UltimateHolder;
import us.talabrek.ultimateskyblock.player.UltimateHolder.MenuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static dk.lockfuglsang.minecraft.util.FormatUtil.stripFormatting;

/**
 * Complex editor for string values (only simple keyboard, no extended).
 */
public class StringEditMenu extends AbstractConfigMenu implements EditMenu {
    private final EditMenu parent;
    private final List<ItemStack> keyboard = new ArrayList<>();
    private final List<ItemStack> capslockOverlay = new ArrayList<>();

    private final ItemStack capsOn;
    private final ItemStack capsOff;

    private final int capsIndex;
    private final int backspaceIndex;
    private final int returnIndex;

    public StringEditMenu(FileConfiguration menuConfig, EditMenu parent) {
        super(menuConfig);
        this.parent = parent;
        List<?> characterMap = menuConfig.getList("keyboard.en");
        ensureCapacity(keyboard, 9 * 6);
        ensureCapacity(capslockOverlay, 9 * 6);
        int row = 0;
        for (Object o : characterMap) {
            int col = 0;
            if (o instanceof List) {
                for (String item : (List<String>) o) {
                    ItemStack character = createItemFromComponentString(item);
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
                ItemStack item = createItemFromComponentString(overlay.getString(key));
                if (item != null) {
                    capslockOverlay.set(index, item);
                }
            }
        }
        capsOn = createItemFromComponentString(menuConfig.getString("keyboard.capslock.true"));
        capsOff = createItemFromComponentString(menuConfig.getString("keyboard.capslock.false"));
        keyboard.set(getIndex(0, 0), createItem(Material.OAK_DOOR, "\u00a79" + tr("Return"), null));
        capsIndex = getIndex(5, 0);
        keyboard.set(capsIndex, capsOff);
        backspaceIndex = getIndex(0, 5);
        returnIndex = 0;
    }

    @Override
    public boolean onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof UltimateHolder) ||
            !stripFormatting(((UltimateHolder) e.getInventory().getHolder()).getTitle()).equals(stripFormatting(getTitle()))) {
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
            FileConfiguration config = FileUtil.getYmlConfiguration(configName);
            String value = config.getString(path);
            if (e.getSlot() == capsIndex) {
                // Toggle caps
                ItemStack capsItem = isCaps ? capsOff.clone() : capsOn.clone();
                ItemMeta meta = capsItem.getItemMeta();
                meta.setLore(Collections.singletonList(value));
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
            } else if (currentItem.getType() == Material.PLAYER_HEAD) {
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
        FileConfiguration config = FileUtil.getYmlConfiguration(configName);
        if (!config.isString(path)) {
            return null;
        }
        String value = config.getString(path, "");
        Inventory menu = Bukkit.createInventory(new UltimateHolder(null, getTitle(), MenuType.DEFAULT), 9 * 6, getTitle());
        setCharacters(menu, value, keyboard);
        if (isCaps) {
            setCharacters(menu, value, capslockOverlay);
        }
        ItemStack returnItem = menu.getItem(0).clone();
        ItemMeta meta = returnItem.getItemMeta();
        meta.setLore(Arrays.asList(configName, path, tr("\u00a77Page {0}", page)));
        returnItem.setItemMeta(meta);
        menu.setItem(0, returnItem);

        ItemStack capsItem = isCaps ? capsOn.clone() : capsOff.clone();
        ItemMeta itemMeta = capsItem.getItemMeta();
        itemMeta.setLore(List.of(value));
        capsItem.setItemMeta(itemMeta);
        menu.setItem(capsIndex, capsItem);

        return menu;
    }

    private void setCharacters(Inventory menu, String value, List<ItemStack> itemList) {
        int index = 0;
        for (ItemStack item : itemList) {
            if (item != null) {
                ItemStack clone = item.clone();
                ItemMeta itemMeta = clone.getItemMeta();
                itemMeta.setLore(Collections.singletonList(value));
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

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
import us.talabrek.ultimateskyblock.player.UltimateHolder;
import us.talabrek.ultimateskyblock.player.UltimateHolder.MenuType;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.pre;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.menu.MenuItemFactory.READONLY;
import static dk.lockfuglsang.minecraft.util.FormatUtil.stripFormatting;
import static dk.lockfuglsang.minecraft.util.FormatUtil.wordWrap;
import static dk.lockfuglsang.minecraft.util.ItemStackUtil.builder;

/**
 * The primary config menu.
 */
public class MainConfigMenu extends AbstractConfigMenu implements EditMenu {
    private final uSkyBlock plugin;
    private final MenuItemFactory factory;
    private final List<EditMenu> editMenus;

    public MainConfigMenu(uSkyBlock plugin, YmlConfiguration menuConfig, MenuItemFactory factory, List<EditMenu> editMenus) {
        super(menuConfig);
        this.plugin = plugin;
        this.factory = factory;
        this.editMenus = editMenus;
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof UltimateHolder))
            return false;
        String title = stripFormatting(((UltimateHolder) event.getInventory().getHolder()).getTitle());
        final Player player = (Player) event.getWhoClicked();
        if (!title.contains(".yml")) {
            return false;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return true;
        }
        if(event.getInventory().equals(event.getView().getBottomInventory())) {
            return true; //we clicked on the players inventory, lets not try to do anyting
        }
        if (event.getSlot() % 9 == 0 && (item.getType() == Material.BOOK || item.getType() == Material.WRITABLE_BOOK)) {
            String configName = getConfigName(item);
            int page = getConfigPage(item);
            if (event.isShiftClick()) {
                if (event.getSlot() == getIndex(1,0) && page > 10) {
                    page -= 10;
                } else if (event.getSlot() == getIndex(3,0)) {
                    page += 10;
                }
            }
            Inventory menu = createFileMenu(configName, page);
            player.openInventory(menu);
        } else if (event.getSlot() == getIndex(5, 0) && item.getType() == Material.ENDER_CHEST) {
            ItemStack currentMenu = getCurrentMenu(event);
            String configName = getConfigName(currentMenu);
            int page = getConfigPage(currentMenu);
            player.closeInventory();
            saveConfig(player, configName, page);
        } else {
            ItemStack currentMenu = getCurrentMenu(event);
            String configName = getConfigName(currentMenu);
            int page = getConfigPage(currentMenu);
            String path = findPath(event.getInventory(), event.getSlot());
            if (path != null && !isReadonly(configName, path)) {
                Inventory editor = null;
                for (EditMenu editMenu : editMenus) {
                    if (editMenu != this) {
                        editor = editMenu.createEditMenu(configName, path, page);
                    }
                    if (editor != null) {
                        break;
                    }
                }
                if (editor == null) {
                    player.openInventory(createFileMenu(configName, page));
                } else {
                    player.openInventory(editor);
                }
            }
        }
        return true;
    }

    private String findPath(Inventory inventory, int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(stripFormatting(item.getItemMeta().getDisplayName()));
        int row = slot / 9;
        int col = slot % 9;
        while (col >= 1) {
            ItemStack parent = inventory.getItem(getIndex(row, col));
            if(parent != null && parent.getType() != Material.PAPER) {
                col--;
                parent = inventory.getItem(getIndex(row, col));
            }
            else if(parent == null || parent.getType() != Material.PAPER) {
                row--;
                parent = inventory.getItem(getIndex(row, col));
            }
            else if (parent != null && parent.getType() == Material.PAPER) {
                sb.insert(0, stripFormatting(parent.getItemMeta().getDisplayName()) + ".");
                col--;
            }
        }
        return sb.toString();
    }

    private void saveConfig(final Player player, final String configName, final int page) {
        plugin.async(new Runnable() {
            @Override
            public void run() {
                try {
                    YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
                    config.set("dirty", null);
                    config.save(new File(plugin.getDataFolder(), configName));
                    plugin.sync(new Runnable() {
                        @Override
                        public void run() {
                            plugin.reloadConfig();
                            player.sendMessage(tr("\u00a7eConfiguration saved and reloaded."));
                            player.openInventory(createEditMenu(configName, null, page));
                        }
                    });
                } catch (IOException e) {
                    player.sendMessage(tr("\u00a7cError! \u00a79Unable to save config file!"));
                }
            }
        });

    }

    private Inventory createFileMenu(String filename, int page) {
        YmlConfiguration config = FileUtil.getYmlConfiguration(filename);
        int row = 0;
        int col = 1;
        ArrayList<ItemStack> menuList = new ArrayList<>(54);
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                row = addSection(menuList, config.getConfigurationSection(key), row, col, config, filename);
            }
        }
        int maxPages = (int) Math.ceil(menuList.size() / 54d);
        if (page < 1) {
            page = 1;
        }
        if (page > maxPages) {
            page = maxPages;
        }
        String title = tr("Config:") + " " + pre("{0} ({1}/{2})", filename, page, maxPages);
        Inventory menu = Bukkit.createInventory(new UltimateHolder(null, title, MenuType.CONFIG), 6 * 9, title);
        menu.setMaxStackSize(MenuItemFactory.MAX_INT_VALUE);
        int startOffset = (page-1)*54;
        // Add section markers on top line
        for (int i = 1; i <= 8; i++) {
            if (menuList.get(startOffset+i) != null) {
                break; // Already an item here... we are done
            }
            int offset = 9;
            while (menuList.get(startOffset+i) == null) {
                // find on "higher (hidden) row"
                if (menuList.get(startOffset+i-offset) != null) {
                    menuList.set(startOffset+i, menuList.get(startOffset+i-offset).clone());
                    break;
                }
                offset += 9;
            }
        }
        for (int i = startOffset; i < (page*54); i++) {
            if (i >= menuList.size()) {
                break;
            }
            ItemStack itemStack = menuList.get(i);
            if (itemStack != null) {
                menu.setItem(i-startOffset, itemStack);
            }
        }
        menu.setItem(0, builder(new ItemStack(page == 1 ? Material.WRITABLE_BOOK : Material.BOOK, 1))
                .displayName(filename)
                .lore(tr("\u00a77Page {0}", 1))
                .lore(tr("\u00a73First Page"))
                .build());
        int offset = 2;
        if (page > 3) {
            offset = page-1;
        }
        if (offset > maxPages-3 && maxPages > 5) {
            offset = maxPages-3;
        }
        for (int i = offset; maxPages > offset && i <= Math.min(offset+2, maxPages-1); i++) {
            menu.setItem((1 + i - offset) * 9, builder(new ItemStack(page == i ? Material.WRITABLE_BOOK : Material.BOOK, i))
                    .displayName(filename)
                    .lore(tr("\u00a77Page {0}", i))
                    .build());
        }
        menu.setItem(getIndex(4,0), builder(new ItemStack(page == maxPages ? Material.WRITABLE_BOOK : Material.BOOK, maxPages))
                .displayName(filename)
                .lore(tr("\u00a77Page {0}", maxPages))
                .lore(tr("\u00a73Last Page"))
                .build());
        ItemStack itemStack = builder(new ItemStack(Material.ENDER_CHEST, 1))
                .displayName(tr("\u00a7cSave & Reload config"))
                .lore(Arrays.asList(tr("\u00a77Saves the settings to\n\u00a77file & reloads again.\n\u00a7cNote: \u00a77Use with care!").split("\n")))
                .select(config.getBoolean("dirty", false))
                .build();
        menu.setItem(getIndex(5, 0), itemStack);
        return menu;
    }

    private String getConfigName(ItemStack currentMenu) {
        if (currentMenu != null && currentMenu.getItemMeta().getDisplayName() != null) {
            return stripFormatting(currentMenu.getItemMeta().getDisplayName());
        }
        return "config.yml";
    }

    private int getConfigPage(ItemStack currentMenu) {
        if (currentMenu != null && currentMenu.getItemMeta() != null
                && currentMenu.getItemMeta().getLore() != null
                && !currentMenu.getItemMeta().getLore().isEmpty()) {
            try {
                Object[] parts = new MessageFormat(tr("\u00a77Page {0}")).parse(currentMenu.getItemMeta().getLore().get(0));
                if (parts != null && parts.length == 1) {
                    return Integer.parseInt("" + parts[0]);
                }
            } catch (ParseException e) {
                // Ignore
            }
        }
        return 1;
    }
    private ItemStack getCurrentMenu(InventoryClickEvent event) {
        int index = 0;
        ItemStack currentMenu = event.getInventory().getItem(index);
        while (currentMenu != null && currentMenu.getType() != Material.WRITABLE_BOOK) {
            index += 9;
            currentMenu = event.getInventory().getItem(index);
        }
        return currentMenu;
    }

    @Override
    public Inventory createEditMenu(String configName, String path, int page) {
        return createFileMenu(configName, page);
    }

    private int addSection(ArrayList<ItemStack> menuList, ConfigurationSection sec, int row, int col, YmlConfiguration config, String filename) {
        if (isBlackListed(filename, sec.getCurrentPath())) {
            return row;
        }
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00a77\u00a7o" + sec.getName());
        String comment = config.getComment(sec.getCurrentPath());
        if (comment != null) {
            meta.setLore(wordWrap(comment.replaceAll("\n", " "), 20, 20));
        }
        item.setItemMeta(meta);
        int index = getIndex(row, col);
        ensureCapacity(menuList, index);
        menuList.set(index, item);
        int colbase = ++col;
        boolean lastWasSection = true;
        for (String key : sec.getKeys(false)) {
            index = getIndex(row, col);
            ensureCapacity(menuList, index);
            if (sec.isConfigurationSection(key)) {
                if (!lastWasSection && col != colbase) {
                    row++;
                    col = colbase;
                }
                row = addSection(menuList, sec.getConfigurationSection(key), row, col, config, filename);
                col = colbase;
                lastWasSection = true;
            } else {
                String path = sec.getCurrentPath() + "." + key;
                if (isBlackListed(filename, path)) {
                    continue; // Skip
                }
                boolean readonly = isReadonly(filename, path);
                item = null;
                if (sec.isBoolean(key)) {
                    item = factory.createBooleanItem(sec.getBoolean(key), path, config, readonly);
                } else if (sec.isInt(key)) {
                    item = factory.createIntegerItem(sec.getInt(key), path, config, readonly);
                } else {
                    item = factory.createStringItem(sec.getString(key, ""), path, config, readonly);
                }
                if (item != null) {
                    if (readonly) {
                        ItemMeta itemMeta = item.getItemMeta();
                        List<String> lore = itemMeta.getLore();
                        lore.set(0, READONLY + lore.get(0) + tr("\u00a77 (readonly)"));
                        itemMeta.setLore(lore);
                        item.setItemMeta(itemMeta);
                    }
                    menuList.set(index, item);
                    col++;
                    lastWasSection = false;
                }
            }
            if (col >= 9) {
                row++;
                col = colbase;
            }
        }
        return col != colbase ? row+1 : row;
    }
}

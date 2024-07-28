package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.player.UltimateHolder;
import us.talabrek.ultimateskyblock.player.UltimateHolder.MenuType;
import us.talabrek.ultimateskyblock.util.GuiItemUtil;

import java.util.*;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static dk.lockfuglsang.minecraft.util.FormatUtil.stripFormatting;

/**
 * Editor for integer nodes.
 */
public class IntegerEditMenu extends AbstractConfigMenu implements EditMenu {
    private static final Material DEFAULT_NUMBER_ICON = Material.BLUE_STAINED_GLASS_PANE;
    private final FileConfiguration menuConfig;
    private final MenuItemFactory factory;
    private final EditMenu parent;

    private final Map<String, ItemStack> increments = new LinkedHashMap<>();

    public IntegerEditMenu(FileConfiguration menuConfig, MenuItemFactory factory, EditMenu parent) {
        super(menuConfig);
        this.menuConfig = menuConfig;
        this.factory = factory;
        this.parent = parent;
        ConfigurationSection incSection = menuConfig.getConfigurationSection("integer-menu.increment");
        if (incSection != null) {
            for (String inc : incSection.getKeys(false)) {
                int incValue = Integer.parseInt(inc, 10);
                String itemType = incSection.getString(inc, Material.IRON_INGOT.name());
                String displayName = incValue < 0 ? tr("\u00a7c{0,number,#}", incValue) : tr("\u00a7a+{0,number,#}", incValue);
                ItemStack displayItem = GuiItemUtil.createGuiDisplayItem(itemType, displayName);
                increments.put(inc, displayItem);
            }
        }
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof UltimateHolder) ||
            ((UltimateHolder) event.getInventory().getHolder()).getTitle() == null ||
            !stripFormatting(((UltimateHolder) event.getInventory().getHolder()).getTitle()).contains(stripFormatting(getTitle()))) {
            return false;
        }
        if (event.getSlotType() != InventoryType.SlotType.CONTAINER) {
            return true;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory menu = event.getInventory();
        ItemStack returnItem = menu.getItem(getIndex(5, 0));
        String configName = returnItem.getItemMeta().getLore().get(0);
        String path = returnItem.getItemMeta().getLore().get(1);
        int page = getPage(returnItem.getItemMeta().getLore().get(2));
        int slot = event.getSlot();
        int row = slot / 9;
        int col = slot % 9;
        ItemStack clickedItem = event.getCurrentItem();
        if (slot >= getIndex(3, 0) && slot <= getIndex(3, 8)) {
            // increment buttons
            FileConfiguration config = FileUtil.getYmlConfiguration(configName);
            int value = config.getInt(path, 0);
            int increment = getDisplayNameAsInt(clickedItem);
            if (event.getClick() == ClickType.LEFT) {
                value += increment;
            } else if (event.getClick() == ClickType.RIGHT) {
                value = increment;
            }
            config.set(path, value);
            config.set("dirty", true);
        }
        if (slot != getIndex(5, 0)) {
            player.openInventory(createEditMenu(configName, path, page));
        } else {
            player.openInventory(parent.createEditMenu(configName, path, page));
        }
        return true;
    }

    private int getDisplayNameAsInt(ItemStack clickedItem) {
        int number = 0;
        try {
            number = Integer.parseInt(stripFormatting(clickedItem.getItemMeta().getDisplayName()).replaceAll("[^0-9\\-]+", ""), 10);
        } catch (NumberFormatException ex) {
        }
        return number;
    }

    /**
     * An editor for integers.
     * <pre>
     *     Config: Integer Editor
     *     +---+---+---+---+---+---+---+---+---+
     *  0  |   |   |   |   |   |   |   | G |   |
     *     +---+---+---+---+---+---+---+---+---+
     *  1  | R | R | R |   | I |   | G | G | G |
     *     +---+---+---+---+---+---+---+---+---+
     *  2  |   |   |   |   |   |   |   | G |   |
     *     +---+---+---+---+---+---+---+---+---+
     *  3  |-1k|100|-10|-1 | 0 |+1 |+10|100|+1k|
     *     +---+---+---+---+---+---+---+---+---+
     *  4  |   |   |   |   |   |   | ^ |   |   |
     *     +---+---+---+---+---+---+---+---+---+
     *  5  |SAV|   |   |   |   |   |   |   |   |
     *     +---+---+---+---+---+---+---+---+---+
     *       0   1   2   3   4   5   6   7   8
     * </pre>
     */
    @Override
    public Inventory createEditMenu(String configName, String path, int page) {
        FileConfiguration config = FileUtil.getYmlConfiguration(configName);
        if (!config.isInt(path)) {
            return null;
        }
        int value = config.getInt(path, 0);
        Inventory menu = Bukkit.createInventory(new UltimateHolder(null, getTitle(), MenuType.CONFIG), 6 * 9, getTitle());
        menu.setMaxStackSize(MenuItemFactory.MAX_INT_VALUE);
        ItemStack frame = createItem(Material.BLACK_STAINED_GLASS_PANE, null, null);
        for (int i = 0; i < 27; i++) {
            menu.setItem(i, frame);
        }
        int nvalue = Math.abs(value);
        int col = 7;
        do {
            int tenValue = nvalue % 10;
            String itemType = menuConfig.getString("integer-menu.number-items." + tenValue, DEFAULT_NUMBER_ICON.name());
            String displayName = value < 0 ? tr("\u00a7c{0,number,#}", value) : tr("\u00a7a{0,number,#}", value);
            ItemStack displayItem = GuiItemUtil.createGuiDisplayItem(itemType, displayName);
            menu.setItem(getIndex(1, col), displayItem);
            nvalue = (nvalue - tenValue) / 10;
            col--;
        } while (nvalue != 0 && col > 0);
        if (value < 0) {
            menu.setItem(getIndex(1, col), createItem(Material.RED_CARPET, MenuItemFactory.INT + value, null));
        }
        ItemStack valueItem = factory.createIntegerItem(value, path, config, false);
        List<String> lore = valueItem.getItemMeta().getLore();
        menu.setItem(getIndex(3, 4), valueItem);
        col = 0;
        for (ItemStack inc : increments.values()) {
            if (col == 4) { // Skip center
                col++;
            }
            int incValue = getDisplayNameAsInt(inc);
            ItemStack icon = ItemStackUtil.builder(inc)
                .lore(tr("&aLeft:&7 Increment with {0}", inc.getItemMeta().getDisplayName()))
                .lore(tr("&cRight-Click:&7 Set to {0}", incValue))
                .lore(lore)
                .build();
            menu.setItem(getIndex(3, col), icon);
            col++;
        }
        menu.setItem(getIndex(5, 0), createItem(Material.OAK_DOOR, "\u00a79" + tr("Return"),
            Arrays.asList(configName, path, tr("\u00a77Page {0}", page))));
        return menu;
    }

    private String getTitle() {
        return tr("Config:") + " " + tr("\u00a79Integer Editor");
    }
}

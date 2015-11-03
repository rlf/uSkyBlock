package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.file.FileUtil;
import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;

/**
 * Editor for integer nodes.
 */
public class IntegerEditMenu extends AbstractConfigMenu implements EditMenu {
    private final YmlConfiguration menuConfig;
    private final MenuItemFactory factory;
    private final EditMenu parent;

    public IntegerEditMenu(YmlConfiguration menuConfig, MenuItemFactory factory, EditMenu parent) {
        super(menuConfig);
        this.menuConfig = menuConfig;
        this.factory = factory;
        this.parent = parent;
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (event.getInventory() == null || event.getInventory().getTitle() == null ||
                !stripFormatting(event.getInventory().getTitle()).contains(stripFormatting(getTitle()))) {
            return false;
        }
        Player player = (Player) event.getWhoClicked();
        Inventory menu = event.getInventory();
        ItemStack returnItem = menu.getItem(getIndex(5, 0));
        String configName = returnItem.getItemMeta().getLore().get(0);
        String path = returnItem.getItemMeta().getLore().get(1);
        int page = getPage(returnItem.getItemMeta().getLore().get(2));
        int slot = event.getSlot();
        if (slot >= getIndex(3, 0) && slot <= getIndex(3, 8)) {
            // increment buttons
            int col = slot - getIndex(3, 0);
            YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
            int value = config.getInt(path, 0);
            int increment = (int) Math.round((Math.signum(col - 4) * Math.pow(10, Math.abs(col - 4)))) / 10;
            value += increment;
            config.set(path, value);
            config.set("dirty", true);
        } else if (slot >= getIndex(4,0) && slot <= getIndex(4,8)) {
            // direct slider
            int col = slot - getIndex(4, 0);
            YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
            int increment = (int) Math.round((Math.signum(col - 4) * Math.pow(10, Math.abs(col - 4)))) / 10;
            config.set(path, increment);
            config.set("dirty", true);
        }
        if (slot != getIndex(5,0)) {
            player.openInventory(createEditMenu(configName, path, page));
        } else {
            player.openInventory(parent.createEditMenu(configName, path, page));
        }
        return true;
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
        YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
        if (!config.isInt(path)) {
            return null;
        }
        int value = config.getInt(path, 0);
        Inventory menu = Bukkit.createInventory(null, 6 * 9, getTitle());
        menu.setMaxStackSize(MenuItemFactory.MAX_INT_VALUE);
        ItemStack redWool = createItem(Material.WOOL, (short) 14, tr("\u00a7c-"), null);
        menu.setItem(getIndex(1, 0), redWool);
        menu.setItem(getIndex(1, 1), redWool);
        menu.setItem(getIndex(1, 2), redWool);
        ItemStack greenWool = createItem(Material.WOOL, (short) 5, tr("\u00a7a+"), null);
        menu.setItem(getIndex(0, 7), greenWool);
        menu.setItem(getIndex(1, 7), greenWool);
        menu.setItem(getIndex(2, 7), greenWool);
        menu.setItem(getIndex(1, 6), greenWool);
        menu.setItem(getIndex(1, 8), greenWool);

        ItemStack frameItem = createItem(Material.STAINED_GLASS_PANE, (short) 8, "\u00a77" + configName, null);
        for (int r = 0; r <= 2; r++) {
            for (int c = 3; c <= 5; c++) {
                menu.setItem(getIndex(r, c), frameItem);
            }
        }
        ItemStack valueItem = factory.createIntegerItem(value, path, config, false);
        menu.setItem(getIndex(1, 4), valueItem);
        menu.setItem(getIndex(3, 0), createItem(Material.EMERALD, tr("\u00a7c-1000"), valueItem.getItemMeta().getLore()));
        menu.setItem(getIndex(3, 1), createItem(Material.DIAMOND, tr("\u00a7c-100"), valueItem.getItemMeta().getLore()));
        menu.setItem(getIndex(3, 2), createItem(Material.GOLD_INGOT, tr("\u00a7c-10"), valueItem.getItemMeta().getLore()));
        menu.setItem(getIndex(3, 3), createItem(Material.IRON_INGOT, tr("\u00a7c-1"), valueItem.getItemMeta().getLore()));

        menu.setItem(getIndex(3, 5), createItem(Material.IRON_INGOT, tr("\u00a7a+1"), valueItem.getItemMeta().getLore()));
        menu.setItem(getIndex(3, 6), createItem(Material.GOLD_INGOT, tr("\u00a7a+10"), valueItem.getItemMeta().getLore()));
        menu.setItem(getIndex(3, 7), createItem(Material.DIAMOND, tr("\u00a7a+100"), valueItem.getItemMeta().getLore()));
        menu.setItem(getIndex(3, 8), createItem(Material.EMERALD, tr("\u00a7a+1000"), valueItem.getItemMeta().getLore()));
        int slot = value <= -1000 ? 0
                : value <= -100 ? 1
                : value <= -10 ? 2
                : value < 0 ? 3
                : value == 0 ? 4
                : value < 10 ? 5
                : value < 100 ? 6
                : value < 1000 ? 7
                : 8;
        double ratio = Math.signum(slot - 4) * Math.pow(10, Math.abs(slot - 4));
        double score = value / ratio; // 0-1
        short subType = (short) (score <= 0.25 ? 0 // white
                : score <= 0.50 ? 8 // light gray
                : score <= 0.75 ? 7 // gray
                : 15); // black
        ItemStack pointer = createItem(Material.CARPET, subType, value < 0 ? "\u00a7c" : "\u00a7a" + value, valueItem.getItemMeta().getLore());
        menu.setItem(getIndex(4, slot), pointer);
        menu.setItem(getIndex(5, 0), createItem(Material.WOOD_DOOR, "\u00a79" + tr("Return"),
                Arrays.asList(configName, path, tr("Page {0,number,integer}", page))));
        return menu;
    }

    private String getTitle() {
        return tr("Config:") + " " + tr("\u00a79Integer Editor");
    }
}

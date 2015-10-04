package us.talabrek.ultimateskyblock.menu;

import dk.lockfuglsang.minecraft.yml.YmlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.util.FileUtil;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.util.Arrays;
import java.util.List;

import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.ItemStackUtil.builder;
import static us.talabrek.ultimateskyblock.util.ItemStackUtil.isValidInventoryItem;

/**
 *
 */
public class ItemStackEditMenu extends AbstractConfigMenu implements EditMenu {
    private static final String ARROW_UP = "{display:{Name:\"Arrow Up Quartz\"},SkullOwner:{Id:\"a8a8e02f-30f7-4b54-ad11-f1fa8f7830ea\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE4ZWY5MmVmYTE1NjY5ZGY0ODMzMmQxMThhMmY3MDU3NzJhMmEzZmNkMGZiODJhNjFmMjc3Yjg0OWEyYmQ2In19fQ==\"}]}}}";
    private static final String ARROW_DOWN = "{display:{Name:\"Arrow Down Quartz\"},SkullOwner:{Id:\"c3c1de76-4631-4f3b-af21-1ce1494f59f4\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ZhZWE5NzdhZWViYTFjODM3NjY5NDEzYjg4Yzk1YzI3ZDA4ZmI0MjlmM2RmZmI0MzFhOGZhYjM2MWE5ZiJ9fX0=\"}]}}}";
    private static final String PLUS = "{display:{Name:\"Stone Plus\"},SkullOwner:{Id:\"80e287ca-69a7-4a03-8811-324a469079b9\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWUzOTVlNzVlMmViOGM0YjcyNDRkY2RiNzVjNTdhNmQ3YWRmMzc1NTNmMjFkNDRlZmM3YmQ3MTQxODQzNDVkIn19fQ==\"}]}}}";
    private final EditMenu parent;
    private static final int incBaseIndex = getIndex(3, 6);
    private static final int decBaseIndex = getIndex(5, 6);
    private static final int baseIndex = getIndex(4, 6);
    private static final int incSubIndex = getIndex(3, 7);
    private static final int subIndex = getIndex(4, 7);
    private static final int incAmountIndex = getIndex(3, 8);
    private static final int decSubIndex = getIndex(5, 7);
    private static final int amountIndex = getIndex(4, 8);
    private static final int decAmountIndex = getIndex(5, 8);
    private static final int returnIndex = getIndex(5, 0);
    private static final int addIndex = getIndex(3,5);
    private static final int confirmIndex = getIndex(4,5);
    private static final int deleteIndex = getIndex(5,5);
    private static final List<Integer> controlPanelIndices = Arrays.asList(
            incBaseIndex, incSubIndex, incAmountIndex,
            baseIndex, subIndex, amountIndex,
            decBaseIndex, decSubIndex, decAmountIndex,
            deleteIndex, addIndex, confirmIndex);

    public ItemStackEditMenu(YmlConfiguration menuConfig, EditMenu parent) {
        super(menuConfig);
        this.parent = parent;
    }

    @Override
    public boolean onClick(InventoryClickEvent e) {
        Inventory menu = e.getInventory();
        if (!stripFormatting(menu.getTitle()).equals(stripFormatting(getTitle()))) {
            return false;
        }
        ItemStack returnItem = menu.getItem(getIndex(5, 0));
        String configName = returnItem != null ? returnItem.getItemMeta().getLore().get(0) : "config.yml";
        String path = returnItem != null ? returnItem.getItemMeta().getLore().get(1) : null;
        int page = returnItem != null ? getPage(returnItem.getItemMeta().getLore().get(2)) : 1;
        ItemStack currentItem = e.getCurrentItem();
        if (e.getSlot() == returnIndex) {
            saveStateToConfig(configName, path, page, menu);
            e.getWhoClicked().openInventory(parent.createEditMenu(configName, path, page));
        } else if (isControlPanel(e.getSlot()) && getSelected(menu) != -1) {
            handleControlPanelClick(e);
            return true;
        } else if (currentItem != null && currentItem.getItemMeta() != null) {
            setSelected(menu, currentItem, e.getSlot());
        }
        return true;
    }

    private void saveStateToConfig(String configName, String path, int page, Inventory menu) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < addIndex && menu.getItem(i) != null) {
            sb.append(ItemStackUtil.asString(menu.getItem(i++))).append(" ");
        }
        YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
        config.set(path, sb.toString().trim());
        config.set("dirty", true);
    }

    private void handleControlPanelClick(InventoryClickEvent e) {
        Inventory menu = e.getInventory();
        int selected = getSelected(menu);
        if (selected == -1) {
            return;
        }
        int clicked = e.getSlot();
        ItemStack selectedItem = menu.getItem(selected);
        ItemStack backupCopy = selectedItem.clone();
        int type = selectedItem.getType().getId();
        short subType = selectedItem.getDurability();
        int amount = selectedItem.getAmount();
        int inc = e.getClick() == ClickType.RIGHT ? 10 : e.getClick() == ClickType.SHIFT_RIGHT ? 100 : 1;
        int maxSub = Math.max(15, selectedItem.getType().getMaxDurability());
        if (clicked == incAmountIndex) {
            if (amount < (selectedItem.getMaxStackSize()-inc)) {
                amount += inc;
            } else if (amount < selectedItem.getMaxStackSize()) {
                amount++;
            }
            selectedItem.setAmount(amount);
        } else if (clicked == decAmountIndex) {
            if (amount > inc) {
                amount -= inc;
            } else if (amount > 1) {
                amount++;
            }
            selectedItem.setAmount(amount);
        } else if (clicked == incSubIndex || clicked == decSubIndex) {
            short newSub = (short) (subType + (clicked == incSubIndex ? inc : -inc));
            if (newSub >= 0 && newSub <= maxSub && isValidInventoryItem(builder(selectedItem).subType(newSub).build())) {
                selectedItem.setDurability(newSub);
            }
        } else if (clicked == incBaseIndex) {
            int newType = type + inc;
            while (newType < 2500 && !isValidInventoryItem(builder(selectedItem).type(newType).build())) {
                newType++;
            }
            if (newType < 2500 && isValidInventoryItem(builder(selectedItem).type(newType).build())) {
                selectedItem.setTypeId(newType);
                selectedItem.setDurability((short) 0);
            }
        } else if (clicked == decBaseIndex) {
            int newType = type - inc;
            while (newType > 0 && !isValidInventoryItem(builder(selectedItem).type(newType).build())) {
                newType--;
            }
            if (newType > 0 && isValidInventoryItem(builder(selectedItem).type(newType).build())) {
                selectedItem.setTypeId(newType);
                selectedItem.setDurability((short) 0);
            }
        } else if (clicked == deleteIndex) {
            selected = deleteSelected(menu, selected);
            selectedItem = setSelected(menu, null, selected);
        } else if (clicked == addIndex) {
            selected = addNewSelected(menu, new ItemStack(Material.DIRT));
            selectedItem = setSelected(menu, null, selected);
        }
        menu.setItem(selected, selectedItem);
        if (menu.getItem(selected) == null) {
            menu.setItem(selected, backupCopy);
        }
        renderSelected(menu, selected);
    }

    private int addNewSelected(Inventory menu, ItemStack itemStack) {
        int slot = 0;
        while (menu.getItem(slot) != null) {
            slot++;
        }
        if (menu.getItem(slot) == null) {
            setSelected(menu, itemStack, slot);
        }
        return slot;
    }

    private int deleteSelected(Inventory menu, int slot) {
        menu.setItem(slot, null);
        int i = slot + 1;
        while (i < getIndex(3,4) && menu.getItem(i) != null) {
            menu.setItem(i-1, menu.getItem(i));
            menu.setItem(i, null);
            i++;
        }
        if (menu.getItem(slot) != null) {
            return slot;
        } else if (slot > 0) {
            return slot-1;
        }
        return 0;
    }

    private void renderSelected(Inventory menu, int selected) {
        ItemStack selectedItem = builder(menu.getItem(selected)).deselect().build();

        ItemStack baseItem = builder(selectedItem.clone())
                .lore("" + selectedItem.getTypeId())
                .amount(1)
                .subType(0)
                .build();

        ItemStack subItem = builder(selectedItem.clone())
                .lore("" + selectedItem.getDurability())
                .amount(1)
                .build();

        menu.setItem(baseIndex, baseItem);
        menu.setItem(subIndex, subItem);
        menu.setItem(amountIndex, selectedItem);

        // arrows
        ItemStack up = builder(createItem(ARROW_UP))
                .displayName(VaultHandler.getItemName(selectedItem))
                .build();
        ItemStack down = builder(createItem(ARROW_DOWN))
                .displayName(VaultHandler.getItemName(selectedItem))
                .build();
        menu.setItem(incBaseIndex, builder(up).lore("" + baseItem.getTypeId()).build());
        menu.setItem(incSubIndex, builder(up).lore("" + subItem.getDurability()).build());
        menu.setItem(incAmountIndex, builder(up).lore("" + selectedItem.getAmount()).build());
        menu.setItem(decBaseIndex, builder(down).lore("" + baseItem.getTypeId()).build());
        menu.setItem(decSubIndex, builder(down).lore("" + subItem.getDurability()).build());
        menu.setItem(decAmountIndex, builder(down).lore("" + selectedItem.getAmount()).build());
        menu.setItem(deleteIndex, new ItemStack(Material.BARRIER));
        menu.setItem(addIndex, createItem(PLUS));
    }

    private boolean isControlPanel(int slot) {
        return controlPanelIndices.contains(slot);
    }

    private int getSelected(Inventory menu) {
        for (int i = 0; i < menu.getSize(); i++) {
            ItemStack item = menu.getItem(i);
            if (item != null) {
                if (item.getItemMeta() != null && item.getItemMeta().getEnchants() != null && !item.getItemMeta().getEnchants().isEmpty()) {
                    return i;
                }
            } else {
                break;
            }
        }
        return -1;
    }

    private ItemStack setSelected(Inventory menu, ItemStack currentItem, int index) {
        clearSelection(menu);
        ItemStack item = builder(currentItem !=  null ? currentItem : menu.getItem(index)).select().build();
        menu.setItem(index, item);
        renderSelected(menu, index);
        return item;
    }

    private void clearSelection(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                inventory.setItem(i, builder(item).deselect().build());
            } else {
                break;
            }
        }
    }

    @Override
    public Inventory createEditMenu(String configName, String path, int page) {
        YmlConfiguration config = FileUtil.getYmlConfiguration(configName);
        if (!config.isString(path)) {
            return null;
        }
        String items = config.getString(path);
        List<ItemStack> itemList = null;
        try {
            itemList = ItemStackUtil.createItemList(items);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        if (itemList == null || itemList.isEmpty()) {
            return null;
        }
        return createEditMenuInternal(configName, path, page, config, itemList);
    }

    /**
     * Design
     * <pre>
     *
     * </pre>
     *
     * @param configName
     * @param path
     * @param page
     * @param config
     * @param itemList
     * @return
     */
    private Inventory createEditMenuInternal(String configName, String path, int page, YmlConfiguration config, List<ItemStack> itemList) {
        Inventory menu = Bukkit.createInventory(null, 6 * 9, getTitle());
        int index = 0;
        for (int i = 0; i < itemList.size(); i++) {
            ItemStack item = itemList.get(i).clone();
            menu.setItem(index, builder(item)
                    .lore("\u00a79" + ItemStackUtil.asString(item))
                    .build());
            index++;
        }
        menu.setItem(getIndex(5, 0), createItem(Material.WOOD_DOOR, "\u00a79" + tr("Return"),
                Arrays.asList(configName, path, tr("Page {0,number,integer}", page))));
        return menu;
    }

    private String getTitle() {
        return tr("Config:") + " " + tr("\u00a79Item List Editor");
    }
}

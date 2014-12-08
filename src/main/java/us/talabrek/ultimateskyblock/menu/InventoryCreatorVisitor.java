package us.talabrek.ultimateskyblock.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Visitor responsible for building a menu-inventory.
 */
public class InventoryCreatorVisitor implements MenuVisitor {
    private static final Logger log = Logger.getLogger(InventoryCreatorVisitor.class.getName());
    private final Player player;
    private final ParameterEvaluator evaluator;
    private Inventory inventory;

    public InventoryCreatorVisitor(Player player, ParameterEvaluator evaluator) {
        this.player = player;
        this.evaluator = evaluator;
    }

    @Override
    public void visit(MenuItem menuItem) {
        try {
            if (menuItem.getEnabled() != null) {
                String eval = evaluator.eval(menuItem.getEnabled());
                if (!(eval.equals("true") || eval.equals("!false"))) {
                    return;
                }
            }
            ItemStack stack = menuItem.getIconItemStack();
            if (stack == null || stack.getType() == null) {
                log.warning("\u00a79[uSkyBlock]\u00a7r Misconfigured icon: " + menuItem.getIcon());
            }
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(evaluator.eval(menuItem.getTitle()));
            List<String> lores = new ArrayList<>();
            for (String lore : menuItem.getLore()) {
                String eval = evaluator.eval(lore);
                if (eval != null && !eval.trim().isEmpty()) {
                    lores.add(eval);
                }
            }
            meta.setLore(lores);
            stack.setItemMeta(meta);
            if (menuItem.getIndex() != -1) {
                inventory.setItem(menuItem.getIndex(), stack);
            } else {
                inventory.addItem(stack);
            }
        } catch (Exception e) {
            log.warning("\u00a79[uSkyBlock]\u00a7r Misconfigured menu-item: " + menuItem);
        }
    }

    @Override
    public void visit(Menu menu) {
        inventory = Bukkit.createInventory(null, menu.getSize(), menu.getTitle() != null ? menu.getTitle() : ParameterEvaluator.UNKNOWN);
    }

    public Inventory getInventory() {
        return inventory;
    }
}

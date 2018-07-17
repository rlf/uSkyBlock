package us.talabrek.ultimateskyblock.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.handler.VaultHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class BlockCollection {
    Map<Material, Map<Byte, Integer>> blockCount;

    public BlockCollection() {
        this.blockCount = new HashMap<>();
    }

    public synchronized void add(Block block) {
        Map<Byte, Integer> countMap = blockCount.getOrDefault(block.getType(), new HashMap<>());
        int currentValue = countMap.getOrDefault(block.getData(), 0);
        countMap.put(block.getData(), currentValue + 1);
        blockCount.put(block.getType(), countMap);
    }

    /**
     * Returns <code>null</code> if all the items are in the BlockCollection, a String describing the missing items if it's not
     * @param itemStacks
     * @return
     */
    public synchronized String diff(Collection<ItemStack> itemStacks) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack item : itemStacks) {
            int diff = 0;
            if (item.getDurability() != 0) {
                diff = item.getAmount() - count(item.getType(), (byte) (item.getDurability() & 0xff));
            } else {
                diff = item.getAmount() - count(item.getType());
            }
            if (diff > 0) {
                sb.append(tr(" \u00a7f{0}x \u00a7{1}", diff, VaultHandler.getItemName(item)));
            }
        }
        if (sb.toString().trim().isEmpty()) {
            return null;
        }
        return tr("\u00a7eStill the following blocks short: {0}", sb.toString());
    }

    private int count(Material type) {
        Map<Byte, Integer> countMap = blockCount.getOrDefault(type, Collections.emptyMap());
        return countMap.values().stream().mapToInt(i -> i).sum();
    }

    public int count(Material type, byte data) {
        Map<Byte, Integer> countMap = blockCount.getOrDefault(type, Collections.emptyMap());
        return countMap.getOrDefault(data, 0);
    }
}

package us.talabrek.ultimateskyblock.handler;

import dk.lockfuglsang.minecraft.util.ItemStackUtil;
import org.bukkit.inventory.ItemStack;

public enum VaultHandler {;
    public static String getItemName(ItemStack stack) {
        return ItemStackUtil.getItemName(stack);
    }
}

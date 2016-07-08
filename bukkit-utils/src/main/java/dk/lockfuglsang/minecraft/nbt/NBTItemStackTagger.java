package dk.lockfuglsang.minecraft.nbt;

import org.bukkit.inventory.ItemStack;

/**
 * Interface for allowing depencency injection for testing (and other platforms than CraftBukkit).
 */
public interface NBTItemStackTagger {
    /**
     * Returns the NBTTag of the <code>itemStack</code> as a string, or the empty-string if none was found.
     * @param itemStack A Bukkit ItemStack
     * @return the NBTTag
     * @since 1.7.2
     */
    String getNBTTag(ItemStack itemStack);
    /**
     * Returns a copy of the <code>itemStack</code> with the supplied <code>nbtTagString</code> applied.
     * @param itemStack     A Bukkit ItemStack
     * @param tag  A valid NBTTag string
     * @return a copy of the <code>itemStack</code>
     * @since 1.7.2
     */
    ItemStack setNBTTag(ItemStack itemStack, String tag);

    /**
     * Returns a copy of the <code>itemStack</code> with the supplied <code>nbtTagString</code> applied.
     * @param itemStack     A Bukkit ItemStack
     * @param tag  A valid NBTTag string
     * @return a copy of the <code>itemStack</code>
     * @since 1.7.2
     */
    ItemStack addNBTTag(ItemStack itemStack, String tag);
}

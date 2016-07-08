package dk.lockfuglsang.minecraft.nbt;

import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

/**
 * Utility for setting NBTTag data on Bukkit items without NMS (using reflection).
 * @since 1.7
 */
public enum NBTUtil { ;
    private static final Logger log = Logger.getLogger(NBTUtil.class.getName());

    private static NBTItemStackTagger tagger = new CraftBukkitNBTTagger();

    /**
     * Returns the NBTTag of the <code>itemStack</code> as a string, or the empty-string if none was found.
     * @param itemStack A Bukkit ItemStack
     * @return the NBTTag
     * @since 1.7
     */
    public static String getNBTTag(ItemStack itemStack) {
        if (itemStack == null) {
            return "";
        }
        return tagger.getNBTTag(itemStack);
    }

    /**
     * Returns a copy of the <code>itemStack</code> with the supplied <code>nbtTagString</code> applied.
     * @param itemStack     A Bukkit ItemStack
     * @param nbtTagString  A valid NBTTag string
     * @return a copy of the <code>itemStack</code>
     * @since 1.7
     */
    public static ItemStack setNBTTag(ItemStack itemStack, String nbtTagString) {
        if (itemStack == null || nbtTagString == null || nbtTagString.isEmpty()) {
            return itemStack;
        }
        return tagger.setNBTTag(itemStack, nbtTagString);
    }

    /**
     * Returns a copy of the <code>itemStack</code> with the supplied <code>nbtTagString</code> applied.
     * @param itemStack     A Bukkit ItemStack
     * @param nbtTagString  A valid NBTTag string
     * @return a copy of the <code>itemStack</code>
     * @since 1.7
     */
    public static ItemStack addNBTTag(ItemStack itemStack, String nbtTagString) {
        if (itemStack == null || nbtTagString == null || nbtTagString.isEmpty()) {
            return itemStack;
        }
        return tagger.addNBTTag(itemStack, nbtTagString);
    }

    public static void setNBTItemStackTagger(NBTItemStackTagger tagger) {
        if (tagger == null) {
            throw new IllegalArgumentException("tagger cannot be null");
        }
        NBTUtil.tagger = tagger;
    }
}

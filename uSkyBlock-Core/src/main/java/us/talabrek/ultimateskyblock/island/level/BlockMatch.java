package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Holds the identification of a unit to be matched against a block
 */
public class BlockMatch implements Comparable<BlockMatch> {
    private final Material type;

    public BlockMatch(Material type) {
        this.type = type;
    }

    public Material getType() {
        return type;
    }

    public ItemStack asItemStack() {
        return new ItemStack(type, 1);
    }

    public boolean matches(Material material) {
        return this.type == material;
    }

    public void accept(BlockMatchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return type.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockMatch that = (BlockMatch) o;
        return o.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(BlockMatch o) {
        return toString().compareTo(o.toString());
    }
}

package us.talabrek.ultimateskyblock.island.level;

import com.google.common.primitives.Bytes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds the identification of a unit to be matched against a block
 */
public class BlockMatch implements Comparable<BlockMatch> {
    private Material type;
    private Set<Byte> dataValues;

    public BlockMatch(Material type) {
        this(type, new byte[0]);
    }

    public BlockMatch(Material type, byte dataValue) {
        this(type, new byte[]{dataValue});
    }

    public BlockMatch(Material type, byte[] dataValues) {
        this.type = type;
        this.dataValues = dataValues != null && dataValues.length > 0 ? new TreeSet<>(Bytes.asList(dataValues)) : Collections.emptySet();
    }

    public Material getType() {
        return type;
    }

    public Set<Byte> getDataValues() {
        return dataValues;
    }

    public ItemStack asItemStack() {
        if (dataValues.size() == 1) {
            return new ItemStack(type, 1, dataValues.iterator().next());
        }
        return new ItemStack(type, 1);
    }

    public boolean matches(Material material, byte dataValue) {
        if (this.type != material) {
            return false;
        }
        if (!dataValues.isEmpty()) {
            return dataValues.contains(dataValue);
        }
        return true;
    }

    public void accept(BlockMatchVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String dvString = "";
        if (dataValues.size() > 1) {
            dvString = String.format("/%d-%d", dataValues.stream().min(Byte::compareTo).get(), dataValues.stream().max(Byte::compareTo).get());
        } else if (!dataValues.isEmpty()) {
            dvString = String.format("/%d", dataValues.iterator().next());
        }
        if (dvString.equals("/0-15")) {
            dvString = "";
        }
        return type.name() + dvString;
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

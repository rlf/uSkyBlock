package us.talabrek.ultimateskyblock.island.level;

import com.google.common.primitives.Bytes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Holds the identification of a unit to be matched against a block
 */
public class BlockMatch {
    private Material type;
    private List<Byte> dataValues;

    public BlockMatch(Material type) {
        this(type, (byte) 0);
    }

    public BlockMatch(Material type, byte dataValue) {
        this(type, new byte[]{dataValue});
    }

    public BlockMatch(Material type, byte[] dataValues) {
        this.type = type;
        this.dataValues = dataValues != null && dataValues.length > 0 ? Bytes.asList(dataValues) : Collections.emptyList();
    }

    public Material getType() {
        return type;
    }

    public List<Byte> getDataValues() {
        return dataValues;
    }

    public ItemStack asItemStack() {
        if (dataValues.size() == 1) {
            return new ItemStack(type, 1, dataValues.get(0));
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
        return "BlockMatch{" +
                "type=" + type +
                ", dataValues=" + dataValues +
                '}';
    }
}

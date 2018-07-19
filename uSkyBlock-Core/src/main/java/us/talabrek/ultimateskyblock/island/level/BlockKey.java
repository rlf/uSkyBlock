package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;

import java.util.Objects;

public class BlockKey {
    private Material type;
    private byte dataValue;

    public BlockKey(Material type, byte dataValue) {
        this.type = type;
        this.dataValue = dataValue;
    }

    public Material getType() {
        return type;
    }

    public byte getDataValue() {
        return dataValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockKey blockKey = (BlockKey) o;
        return dataValue == blockKey.dataValue &&
                type == blockKey.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, dataValue);
    }

    @Override
    public String toString() {
        return type.name() + ":" + dataValue;
    }
}

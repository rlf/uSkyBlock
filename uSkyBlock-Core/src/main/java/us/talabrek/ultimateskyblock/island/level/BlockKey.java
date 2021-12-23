package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;

import java.util.Objects;

public class BlockKey implements Comparable<BlockKey> {
    private final Material type;

    public BlockKey(Material type) {
        this.type = type;
    }

    public Material getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockKey blockKey = (BlockKey) o;
        return type == blockKey.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return type.name();
    }

    @Override
    public int compareTo(BlockKey o) {
        return toString().compareTo(o.toString());
    }
}

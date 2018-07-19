package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class BlockLevelConfigMap extends ConcurrentHashMap<BlockKey, BlockLevelConfig> {
    private final BlockLevelConfigBuilder defaultBuilder;

    public BlockLevelConfigMap(Collection<BlockLevelConfig> configCollection, BlockLevelConfigBuilder defaultBuilder) {
        this.defaultBuilder = defaultBuilder;
        configCollection.stream().forEach(m -> m.accept(new ExplodeMapVisitor(m)));
    }

    public synchronized BlockLevelConfig get(BlockKey key) {
        if (containsKey(key)) {
            return super.get(key);
        }
        BlockLevelConfig newConfig = defaultBuilder.copy().base(new BlockMatch(key.getType(), key.getDataValue())).build();
        put(key, newConfig);
        return newConfig;
    }

    public synchronized BlockLevelConfig get(Material type) {
        return get(createKey(type));
    }

    public synchronized BlockLevelConfig get(Material type, byte dataValue) {
        return get(createKey(type, dataValue));
    }

    private BlockKey createKey(Material material, byte dataValue) {
        return new BlockKey(material, dataValue);
    }

    private BlockKey createKey(Material material) {
        return createKey(material, (byte)0);
    }

    private class ExplodeMapVisitor implements BlockMatchVisitor {
        private BlockLevelConfig config;

        public ExplodeMapVisitor(BlockLevelConfig config) {
            this.config = config;
        }

        @Override
        public void visit(BlockMatch node) {
            if (node.getDataValues().isEmpty()) {
                put(createKey(node.getType()), config);
            } else {
                node.getDataValues().stream().forEach(b -> put(createKey(node.getType(), b), config));
            }
        }
    }
}

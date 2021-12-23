package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockLevelConfigMap {
    private final BlockLevelConfigBuilder defaultBuilder;
    private final Map<Material, Set<BlockLevelConfig>> searchMap = new HashMap<>();

    public BlockLevelConfigMap(Collection<BlockLevelConfig> configCollection, BlockLevelConfigBuilder defaultBuilder) {
        this.defaultBuilder = defaultBuilder;
        configCollection.stream().forEach(m -> m.accept(new ExplodeMapVisitor(m)));
    }

    public synchronized BlockLevelConfig get(BlockMatch blockMatch) {
        return get(asBlockKey(blockMatch));
    }

    private BlockKey asBlockKey(BlockMatch blockMatch) {
        return new BlockKey(blockMatch.getType());
    }

    public synchronized BlockLevelConfig get(BlockKey key) {
        // search map
        Set<BlockLevelConfig> searchSet = searchMap.getOrDefault(key.getType(), new HashSet<>());
        BlockLevelConfig existing = search(searchSet, key);
        if (existing != null) {
            return existing;
        }
        BlockLevelConfig newConfig = defaultBuilder.copy().base(new BlockMatch(key.getType())).build();
        searchSet.add(newConfig);
        searchMap.put(key.getType(), searchSet);
        return newConfig;
    }

    private BlockLevelConfig search(Set<BlockLevelConfig> searchSet, BlockKey key) {
        List<BlockLevelConfig> match = searchSet.stream()
                .filter(p -> p.matches(key.getType()))
                .distinct()
                .sorted((a,b) -> -a.getKey().compareTo(b.getKey())) // best match = longest string = desc ordering
                .collect(Collectors.toList());
        if (!match.isEmpty()) {
            return match.get(0);
        }
        return null;
    }

    public synchronized BlockLevelConfig get(Material type) {
        return get(createKey(type));
    }

    private BlockKey createKey(Material material) {
        return new BlockKey(material);
    }

    public BlockLevelConfig getDefault() {
        return defaultBuilder.copy().base(new BlockMatch(Material.AIR)).build();
    }

    public List<BlockLevelConfig> values() {
        return searchMap.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(BlockLevelConfig::getKey))
                .collect(Collectors.toList());
    }

    private class ExplodeMapVisitor implements BlockMatchVisitor {
        private BlockLevelConfig config;

        public ExplodeMapVisitor(BlockLevelConfig config) {
            this.config = config;
        }

        @Override
        public void visit(BlockMatch node) {
            Set<BlockLevelConfig> searchSet = searchMap.getOrDefault(node.getType(), new HashSet<>());
            searchSet.add(config);
            searchMap.put(node.getType(), searchSet);
        }
    }
}

package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockLevelConfigBuilder {
    private BlockMatch baseBlock;
    private Set<BlockMatch> additionalBlocks = new HashSet<>();
    private double scorePerBlock = 10;
    private int limit = -1;
    private int diminishingReturns = -1;
    private int negativeReturns = -1;

    public BlockLevelConfigBuilder() {
    }

    public BlockLevelConfigBuilder base(Material baseBlock) {
        this.baseBlock = new BlockMatch(baseBlock);
        return this;
    }

    public BlockLevelConfigBuilder base(Material baseBlock, byte dataValue) {
        this.baseBlock = new BlockMatch(baseBlock, dataValue);
        return this;
    }

    public BlockLevelConfigBuilder base(BlockMatch baseBlock) {
        this.baseBlock = baseBlock;
        return this;
    }

    public BlockLevelConfigBuilder additionalBlocks(BlockMatch... blocks) {
        this.additionalBlocks.addAll(Arrays.asList(blocks));
        return this;
    }

    public BlockLevelConfigBuilder scorePerBlock(double scorePerBlock) {
        this.scorePerBlock = scorePerBlock;
        return this;
    }

    public BlockLevelConfigBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public BlockLevelConfigBuilder diminishingReturns(int diminishingReturns) {
        this.diminishingReturns = diminishingReturns;
        return this;
    }

    public BlockLevelConfigBuilder negativeReturns(int negativeReturns) {
        this.negativeReturns = negativeReturns;
        return this;
    }

    public BlockLevelConfigBuilder copy() {
        return new BlockLevelConfigBuilder()
                .base(baseBlock)
                .additionalBlocks(additionalBlocks.toArray(new BlockMatch[0]))
                .scorePerBlock(scorePerBlock)
                .limit(limit)
                .diminishingReturns(diminishingReturns)
                .negativeReturns(negativeReturns);
    }

    public BlockLevelConfig build() {
        if (baseBlock == null) {
            throw new IllegalArgumentException("No base has been set for BlockLevelConfigBuilder");
        }
        // merge any additionalBlocks of the same type as baseBlock into the baseblock
        additionalBlocks.forEach(c -> {
            if (baseBlock.getType() == c.getType()) {
                baseBlock.getDataValues().addAll(c.getDataValues());
            }
        });
        additionalBlocks = additionalBlocks.stream().filter(f -> f.getType() != baseBlock.getType()).collect(Collectors.toSet());
        return new BlockLevelConfig(baseBlock, additionalBlocks, scorePerBlock, limit, diminishingReturns, negativeReturns);
    }
}

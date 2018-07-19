package us.talabrek.ultimateskyblock.island.level;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockLevelConfigBuilder {
    private BlockMatch baseBlock;
    private Set<BlockMatch> additionalBlocks = new HashSet<>();
    private double scorePerBlock = 10;
    private int limit = -1;
    private int diminishingReturns = 0;
    private int negativeReturns = 0;

    public BlockLevelConfigBuilder() {
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
        return new BlockLevelConfig(baseBlock, additionalBlocks, scorePerBlock, limit, diminishingReturns, negativeReturns);
    }
}

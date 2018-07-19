package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Material;
import us.talabrek.ultimateskyblock.api.model.BlockScore;

import java.util.Set;

public class BlockLevelConfig {
    private final BlockKey key;
    private final BlockMatch baseBlock;
    private final Set<BlockMatch> additionalBlocks;

    /**
     * The base-score you will get per block of this type.
     */
    private final double scorePerBlock;

    /**
     * A hard limit (-1 disables it), any blocks above this limit will yield 0 score.
     */
    private final int limit;

    /**
     * Linear score until this limit, then a diminishing return per block.
     */
    private final int diminishingReturns;

    /**
     * Linear score until this limit, then each subsequent block will deduct score.
     * I.e. having 2 x negativeReturns blocks will yield 0 in score, any more will give negative score.
     */
    private final int negativeReturns;

    public BlockLevelConfig(BlockMatch baseBlock, Set<BlockMatch> additionalBlocks, double scorePerBlock, int limit, int diminishingReturns, int negativeReturns) {
        this.baseBlock = baseBlock;
        this.additionalBlocks = additionalBlocks;
        this.scorePerBlock = scorePerBlock;
        this.limit = limit;
        this.diminishingReturns = diminishingReturns;
        this.negativeReturns = negativeReturns;
        this.key = new BlockKey(baseBlock.getType(), baseBlock.getDataValues().isEmpty() ? (byte) 0 : baseBlock.getDataValues().get(0));
    }

    public boolean matches(Material material, byte dataValue) {
        return baseBlock.matches(material, dataValue) || additionalBlocks.stream().anyMatch(b -> b.matches(material, dataValue));
    }

    public BlockScore calculateScore(int count) {
        return calculateScore(count, 1);
    }

    public BlockScore calculateScore(int count, double pointsPerLevel) {
        BlockScore.State state = BlockScore.State.NORMAL;
        double adjustedCount = count;
        if (negativeReturns > 0 && adjustedCount > negativeReturns) {
            state = BlockScore.State.NEGATIVE;
            adjustedCount = 2 * negativeReturns - adjustedCount;
        }
        if (adjustedCount > limit && limit != -1) {
            adjustedCount = limit;
            state = BlockScore.State.LIMIT;
        }
        if (diminishingReturns > 0 && adjustedCount > diminishingReturns) {
            state = BlockScore.State.DIMINISHING;
            adjustedCount = dReturns(adjustedCount, diminishingReturns);
        }
        double blockScore = adjustedCount * scorePerBlock;
        return new BlockScoreImpl(baseBlock.asItemStack(), count, blockScore/pointsPerLevel, state);
    }

    private double dReturns(final double val, final double scale) {
        if (val < 0.0) {
            return -this.dReturns(-val, scale);
        }
        final double mult = val / scale;
        final double trinum = (Math.sqrt(8.0 * mult + 1.0) - 1.0) / 2.0;
        return trinum * scale;
    }

    public void accept(BlockMatchVisitor visitor) {
        baseBlock.accept(visitor);
        additionalBlocks.forEach(ch -> ch.accept(visitor));
    }

    public BlockKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "BlockLevelConfig{" +
                "baseBlock=" + baseBlock +
                ", additionalBlocks=" + additionalBlocks +
                ", scorePerBlock=" + scorePerBlock +
                ", limit=" + limit +
                ", diminishingReturns=" + diminishingReturns +
                ", negativeReturns=" + negativeReturns +
                '}';
    }
}

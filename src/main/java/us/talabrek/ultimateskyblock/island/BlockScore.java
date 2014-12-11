package us.talabrek.ultimateskyblock.island;

/**
 * How much of your score is calculated based on a specific blockId.
 */
public class BlockScore {
    private final int blockId;
    private final int count;
    private final double score;

    public BlockScore(int blockId, int count, double score) {
        this.blockId = blockId;
        this.count = count;
        this.score = score;
    }

    @Override
    public String toString() {
        return "BlockScore{" +
                "blockId=" + blockId +
                ", count=" + count +
                ", score=" + score +
                '}';
    }

    public int getBlockId() {
        return blockId;
    }

    public int getCount() {
        return count;
    }

    public double getScore() {
        return score;
    }
}

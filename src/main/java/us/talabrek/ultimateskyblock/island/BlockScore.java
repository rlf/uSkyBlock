package us.talabrek.ultimateskyblock.island;

import org.bukkit.ChatColor;

/**
 * How much of your score is calculated based on a specific blockId.
 */
public class BlockScore {
    public enum State { NORMAL(ChatColor.AQUA), GOOD(ChatColor.GREEN), LIMIT(ChatColor.RED), DIMINISHING(ChatColor.YELLOW);
        private final ChatColor color;
        State(ChatColor color) {
            this.color = color;
        }
        public ChatColor getColor() {
            return color;
        }
    }

    private final int blockId;
    private final int count;
    private final double score;
    private final State state;

    public BlockScore(int blockId, int count, double score, State state) {
        this.blockId = blockId;
        this.count = count;
        this.score = score;
        this.state = state;
    }

    @Override
    public String toString() {
        return "BlockScore{" +
                "blockId=" + blockId +
                ", count=" + count +
                ", score=" + score +
                ", state=" + state +
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

    public State getState() {
        return state;
    }
}

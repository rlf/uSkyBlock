package us.talabrek.ultimateskyblock.island;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

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

    private final ItemStack block;
    private final int count;
    private final double score;
    private final State state;

    public BlockScore(ItemStack block, int count, double score, State state) {
        this.block = block;
        this.count = count;
        this.score = score;
        this.state = state;
    }

    @Override
    public String toString() {
        return "BlockScore{" +
                "block=" + block +
                ", count=" + count +
                ", score=" + score +
                ", state=" + state +
                '}';
    }

    public ItemStack getBlock() {
        return block;
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

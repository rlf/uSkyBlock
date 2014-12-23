package us.talabrek.ultimateskyblock.island;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.VaultHandler;

/**
 * How much of your score is calculated based on a specific blockId.
 */
public class BlockScore {
    public enum State { GOOD(ChatColor.GREEN), NORMAL(ChatColor.AQUA), DIMINISHING(ChatColor.YELLOW), LIMIT(ChatColor.RED);
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
    private final String name;

    public BlockScore(ItemStack block, int count, double score, State state) {
        this(block, count, score, state, null);
    }

    public BlockScore(ItemStack block, int count, double score, State state, String name) {
        this.block = block;
        this.count = count;
        this.score = score;
        this.state = state;
        this.name = name != null ? name : VaultHandler.getItemName(getBlock());
    }

    @Override
    public String toString() {
        return "BlockScore{" +
                "name=" + name +
                ", block=" + block +
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

    public String getName() {
        return name;
    }
}

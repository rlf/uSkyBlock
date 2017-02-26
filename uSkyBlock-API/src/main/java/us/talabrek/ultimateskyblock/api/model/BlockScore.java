package us.talabrek.ultimateskyblock.api.model;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * How much of your score is calculated based on a specific blockId.
 *
 * @since v2.1.2
 */
public interface BlockScore {
    /**
     * The type of block.
     *
     * @return The type of block.
     * @since v2.1.2
     */
    ItemStack getBlock();

    /**
     * The number of blocks of this type found on the island.
     *
     * @return number of blocks of this type found on the island.
     * @since v2.1.2
     */
    int getCount();

    /**
     * The score contribution from this block.
     *
     * @return score contribution from this block.
     * @since v2.1.2
     */
    double getScore();

    /**
     * The current state of this block.
     *
     * @return The current state of this block.
     * @since v2.1.2
     */
    State getState();

    /**
     * User displayable name of the block.
     * <br>
     * I.e. "Diamond Block".
     *
     * @return User displayable name of the block.
     * @since v2.1.2
     */
    String getName();

    /**
     * The possible states of a BlockScore.
     * <br>
     * <dl>
     * <dt>NORMAL</dt>
     * <dd>No special restrictions, each block added to the island will increase the score.</dd>
     * <dt>DIMINISHING</dt>
     * <dd>The upper limit has been reached, so additional blocks will only contribute a fraction to the score.</dd>
     * <dt>LIMIT</dt>
     * <dd>The hard-limit has been reached, so additional blocks will have no effect on the score.</dd>
     * </dl>
     *
     * @since v2.1.2
     */
    enum State {
        NORMAL(ChatColor.AQUA),
        DIMINISHING(ChatColor.YELLOW),
        LIMIT(ChatColor.RED),
        /**
         * @since v2.7.4
         */
        NEGATIVE(ChatColor.DARK_PURPLE);
        private final ChatColor color;

        State(ChatColor color) {
            this.color = color;
        }

        /**
         * Returns the chat-color associated with this state.
         *
         * @return the chat-color associated with this state.
         * @since v2.1.2
         */
        public ChatColor getColor() {
            return color;
        }
    }
}

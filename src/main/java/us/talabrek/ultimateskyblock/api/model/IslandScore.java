package us.talabrek.ultimateskyblock.api.model;

import us.talabrek.ultimateskyblock.island.BlockScore;

import java.util.List;

/**
 * The summary of island calculation.
 * @since v2.1.2
 */
public interface IslandScore {
    /**
     * Returns the calculated score.
     * @since v2.1.2
     */
    double getScore();
    /**
     * Returns an ordered list of the BlockScore influencing the score-calculation.
     * @param num The number of entries to return.
     * @return an ordered list of the BlockScore influencing the score-calculation.
     * @since v2.1.2
     */
    List<BlockScore> getTop(int num);
}

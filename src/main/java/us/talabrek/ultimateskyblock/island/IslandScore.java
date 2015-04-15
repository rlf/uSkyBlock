package us.talabrek.ultimateskyblock.island;

import java.util.*;

/**
 * The summary of island calculation.
 */
public class IslandScore implements us.talabrek.ultimateskyblock.api.model.IslandScore {
    private final double score;
    private final List<BlockScore> top;
    private boolean isSorted = false;

    public IslandScore(double score, List<BlockScore> top) {
        this.score = score;
        this.top = joinTop(top);
    }

    /**
     * Consolidates the top, so scores with the same name is combined.
     */
    private List<BlockScore> joinTop(List<BlockScore> top) {
        Map<String,BlockScore> scoreMap = new HashMap<>();
        for (BlockScore score : top) {
            BlockScore existing = scoreMap.get(score.getName());
            if (existing == null) {
                scoreMap.put(score.getName(), score);
            } else {
                scoreMap.put(score.getName(), add(score, existing));
            }
        }
        return new ArrayList<>(scoreMap.values());
    }

    private BlockScore add(BlockScore score, BlockScore existing) {
        BlockScore.State state = score.getState();
        if (score.getState().ordinal() > existing.getState().ordinal()) {
            state = existing.getState();
        }
        return new BlockScore(existing.getBlock(),
                score.getCount() + existing.getCount(),
                score.getScore() + existing.getScore(), state, score.getName());
    }


    @Override
    public double getScore() {
        return score;
    }

    @Override
    public List<BlockScore> getTop(int num) {
        return getTop(0, num);
    }

    @Override
    public List<BlockScore> getTop(int offset, int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("Number must be a positive integer.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be a non-negative integer.");
        }
        if (!isSorted) {
            Collections.sort(top, new BlockScoreComparator());
            isSorted = true;
        }
        return top.subList(offset, Math.min(offset+num, top.size()));
    }

    @Override
    public int getSize() {
        return top.size();
    }
}

package us.talabrek.ultimateskyblock.handler.asyncworldedit;

import com.boydti.fawe.object.FaweQueue;
import com.boydti.fawe.object.RunnableVal2;

/**
 * Progress tracker for FAWE integration
 */
public class FAWEProgressTracker extends RunnableVal2<FaweQueue.ProgressType, Integer> {
    private final PlayerProgressTracker tracker;
    private int queued = 64;
    private int placed = 0;
    private volatile boolean done = false;


    public FAWEProgressTracker(PlayerProgressTracker tracker) {
        this.tracker = tracker;
        tracker.addTracker(this);
    }

    @Override
    public void run(FaweQueue.ProgressType progressType, Integer amount) {
        switch (progressType) {
            case DISPATCH:
                placed = amount;
                break;
            case QUEUE:
                queued = amount;
                done = false;
                break;
            case DONE:
                done = true;
                break;
        }
        tracker.tick();
    }

    public int getTotal() {
        return placed + queued;
    }

    public int getCompleted() {
        return placed;
    }

    public boolean isDone() {
        return done;
    }
}
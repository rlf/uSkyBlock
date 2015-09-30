package us.talabrek.ultimateskyblock.async;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Responsible for holding ongoing jobs, and recording status.
 */
public enum JobManager {;
    private static final ConcurrentMap<String,Stats> jobStats = new ConcurrentHashMap<>();

    public static void addJob(IncrementalRunnable runnable) {
        String jobName = runnable.getClass().getSimpleName();
        if (!jobStats.containsKey(jobName)) {
            jobStats.put(jobName, new Stats());
        }
        jobStats.get(jobName).add(runnable);
    }

    public static void completeJob(IncrementalRunnable runnable) {
        String jobName = runnable.getClass().getSimpleName();
        if (!jobStats.containsKey(jobName)) {
            jobStats.put(jobName, new Stats());
        }
        jobStats.get(jobName).complete(runnable);
    }

    public static Map<String,Stats> getStats() {
        return Collections.unmodifiableMap(jobStats);
    }

    public static class Stats {
        private int jobs;
        private int jobsRunning;
        private long ticks;
        private double timeActive;
        private double timeElapsed;

        public Stats() {
        }

        public synchronized void add(IncrementalRunnable runnable) {
            jobsRunning++;
            jobs++;
        }

        public synchronized void complete(IncrementalRunnable runnable) {
            jobsRunning--;
            ticks += runnable.getTicks();
            timeActive += runnable.getTimeUsed();
            timeElapsed += runnable.getTimeElapsed();
        }

        public int getJobs() {
            return jobs;
        }

        public long getTicks() {
            return ticks;
        }

        public double getTimeActive() {
            return timeActive;
        }

        public double getTimeElapsed() {
            return timeElapsed;
        }

        public double getAvgMsActivePerTick() {
            return timeActive / (ticks > 0 ? ticks : 1);
        }

        public double getAvgMsActivePerJob() {
            return timeActive / (jobs > 0 ? jobs : 1);
        }

        public double getAvgMsElapsedPerJob() {
            return timeElapsed *1d / (jobs > 0 ? jobs : 1);
        }

        public int getRunningJobs() {
            return jobsRunning;
        }
    }
}

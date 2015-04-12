package us.talabrek.ultimateskyblock.util;

public enum TimeUtil {;
    private static final long SEC = 1000;
    private static final long MIN = 60*SEC;
    private static final long HOUR = 60*MIN;
    private static final long DAYS = 24*HOUR;

    public static String millisAsString(long millis) {
        long d = millis / DAYS;
        long h = (millis % DAYS) / HOUR;
        long m = (millis % HOUR) / MIN;
        long s = (millis % MIN) / SEC;
        String str = "";
        if (d > 0) {
            str += d + "d";
        }
        if (h > 0) {
            str += " " + h + "h";
        }
        if (m > 0) {
            str += " " + m + "m";
        }
        if (s > 0 || str.isEmpty()) {
            str += " " + s + "s";
        }
        return str.trim();
    }

    public static String ticksAsString(int ticks) {
        return millisAsString(ticks * 50);
    }

    public static long secondsAsTicks(int secs) {
        // 20 ticks per second = 50 ms per tick
        return (secs * 100) / 5;
    }
}

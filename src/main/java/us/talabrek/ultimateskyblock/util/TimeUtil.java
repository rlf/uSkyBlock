package us.talabrek.ultimateskyblock.util;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

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
            str += d + tr("d");
        }
        if (h > 0) {
            str += " " + h + tr("h");
        }
        if (m > 0) {
            str += " " + m + tr("m");
        }
        if (s > 0 || str.isEmpty()) {
            str += " " + s + tr("s");
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

    public static long secondsAsMillis(long timeout) {
        return timeout * 1000;
    }

    public static int millisAsSeconds(long millis) {
        return (int) Math.ceil(millis / 1000f);
    }

    public static long millisAsTicks(int ms) {
        return ms/50;
    }
}

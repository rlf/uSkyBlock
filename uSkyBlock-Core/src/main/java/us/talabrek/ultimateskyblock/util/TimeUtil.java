package us.talabrek.ultimateskyblock.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public enum TimeUtil {
    ;
    private static final Pattern TIME_PATTERN = Pattern.compile("((?<d>[0-9]+)d)?\\s*((?<h>[0-9]+)h)?\\s*((?<m>[0-9]+)m)?\\s*((?<s>[0-9]+)s)?\\s*((?<ms>[0-9]+)ms)?");
    private static final long SEC = 1000;
    private static final long MIN = 60 * SEC;
    private static final long HOUR = 60 * MIN;
    private static final long DAYS = 24 * HOUR;

    public static long stringAsMillis(String s) {
        Matcher m = TIME_PATTERN.matcher(s);
        if (m.matches()) {
            long t = 0;
            if (m.group("d") != null) {
                t += Integer.parseInt(m.group("d"), 10) * DAYS;
            }
            if (m.group("h") != null) {
                t += Integer.parseInt(m.group("h"), 10) * HOUR;
            }
            if (m.group("m") != null) {
                t += Integer.parseInt(m.group("m"), 10) * MIN;
            }
            if (m.group("s") != null) {
                t += Integer.parseInt(m.group("s"), 10) * SEC;
            }
            if (m.group("ms") != null) {
                t += Integer.parseInt(m.group("ms"), 10);
            }
            return t;
        }
        return -1;
    }

    public static String millisAsString(long millis) {
        long d = millis / DAYS;
        long h = (millis % DAYS) / HOUR;
        long m = (millis % HOUR) / MIN;
        long s = (millis % MIN) / SEC;
        String str = "";
        if (d > 0) {
            str += " " + d + tr("d");
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

    public static String millisAsShort(long millis) {
        long m = millis / MIN;
        long s = (millis % MIN) / SEC;
        long ms = millis % SEC;
        return String.format(tr("{0,number,0}:{1,number,00}.{2,number,000}", m, s, ms));
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
        return ms / 50;
    }
}

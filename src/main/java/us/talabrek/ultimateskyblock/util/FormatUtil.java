package us.talabrek.ultimateskyblock.util;

/**
 * Format utility
 */
public enum FormatUtil {;
    public static String stripFormatting(String format) {
        if (format == null || format.trim().isEmpty()) {
            return "";
        }
        return format.replaceAll("(\u00a7|&)[0-9a-fklmor]", "");
    }

    public static String normalize(String format) {
        if (format == null || format.trim().isEmpty()) {
            return "";
        }
        return format.replaceAll("(\u00a7|&)([0-9a-fklmor])", "\u00a7$2");
    }
}

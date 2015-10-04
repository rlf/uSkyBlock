package us.talabrek.ultimateskyblock.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Format utility
 */
public enum FormatUtil {;
    private static final Pattern FORMATTING = Pattern.compile("^(?<format>(\u00a7[0-9a-fklmor])+).*");
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

    public static List<String> wordWrap(String s, int firstSegment, int lineSize) {
        Matcher m = FORMATTING.matcher(s);
        String format = "";
        if (m.matches() && m.group("format") != null) {
            format = m.group("format");
        }
        List<String> words = new ArrayList<>();
        int ix = firstSegment;
        int jx = 0;
        while (ix < s.length()) {
            ix = s.indexOf(' ', ix);
            if (ix != -1) {
                String subString = s.substring(jx, ix).trim();
                if (!subString.isEmpty()) {
                    words.add(format + subString);
                }
            } else {
                break;
            }
            jx = ix + 1;
            ix += lineSize;
        }
        words.add(format + s.substring(jx));
        return words;
    }
}

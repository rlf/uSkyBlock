package dk.lockfuglsang.minecraft.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Format utility
 */
public enum FormatUtil {;
    private static final Pattern FORMATTING = Pattern.compile("^.*(?<format>(\u00a7[0-9a-fklmor])+).*");
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

    public static List<String> wordWrap(String s, int lineSize) {
        return wordWrap(s, lineSize, lineSize);
    }

    /**
     * Wraps the string rather lazyly around the linesize (over).
     *
     * I.e.
     * <pre>
     *   this is a line of words
     * </pre>
     * Will break into the following with linesize of 11:
     * <pre>
     *   this is a line
     *   of words
     * </pre>
     * Note that the first line is longer than 11 (14).
     */
    public static List<String> wordWrap(String s, int firstSegment, int lineSize) {
        String format = getFormat(s);
        if (format == null || !s.startsWith(format)) {
            format = "";
        }
        List<String> words = new ArrayList<>();
        int numChars = firstSegment;
        int ix = 0;
        int jx = 0;
        while (ix < s.length()) {
            ix = s.indexOf(' ', ix+1);
            if (ix != -1) {
                String subString = s.substring(jx, ix).trim();
                String f = getFormat(subString);
                int chars = stripFormatting(subString).length() + 1; // remember the space
                if (chars >= numChars) {
                    if (f != null) {
                        format = f;
                    }
                    if (!subString.isEmpty()) {
                        words.add(withFormat(format, subString));
                        numChars = lineSize;
                        jx = ix + 1;
                    }
                }
            } else {
                break;
            }
        }
        words.add(withFormat(format, s.substring(jx).trim()));
        return words;
    }

    public static List<String> wordWrapStrict(String s, int lineLength) {
        List<String> lines = new ArrayList<>();
        String format = getFormat(s);
        if (format == null || !s.startsWith(format)) {
            format = "";
        }
        String[] words = s.split(" ");
        String line = "";
        for (String word: words) {
            String test = stripFormatting(line + " " + word).trim();
            if (test.length() <= lineLength) {
                // add word
                line += (line.isEmpty() ? "" : " ") + word;
            } else if (line.isEmpty() || stripFormatting(word).length() > lineLength) {
                // add word truncated
                String f = getFormat(word);
                String strip = stripFormatting(word);
                do {
                    int len = Math.min(strip.length(), lineLength-line.length()-1);
                    lines.add(withFormat(format, line + (line.isEmpty() ? "" : " ") + strip.substring(0, len)));
                    strip = strip.substring(len);
                    if (f != null) {
                        format = f;
                    }
                } while (strip.length() > lineLength);
                line = strip;
            } else {
                // add line, then start a new
                lines.add(withFormat(format, line));
                String f = getFormat(line);
                if (f != null) {
                    format = f;
                }
                line = word;
            }
        }
        if (!line.isEmpty()) {
            lines.add(withFormat(format, line));
        }
        return lines;
    }

    private static String withFormat(String format, String subString) {
        String sf = null;
        if (!subString.startsWith("\u00a7")) {
            sf = format + subString;
        } else {
            sf = subString;
        }
        return sf;
    }

    private static String getFormat(String s) {
        Matcher m = FORMATTING.matcher(s);
        String format = null;
        if (m.matches() && m.group("format") != null) {
            format = m.group("format");
        }
        return format;
    }

    public static String join(List<String> list, String separator) {
        String joined = "";
        for (String s : list) {
            joined += s + separator;
        }
        joined = !list.isEmpty() ? joined.substring(0, joined.length() - separator.length()) : joined;
        return joined;
    }

    public static List<String> prefix(List<String> list, String prefix) {
        List<String> prefixed = new ArrayList<>(list.size());
        for (String s : list) {
            prefixed.add(prefix + s);
        }
        return prefixed;
    }

    public static String camelcase(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("[ _]")) {
            sb.append(Character.toUpperCase(part.charAt(0)));
            sb.append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}

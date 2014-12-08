package us.talabrek.ultimateskyblock.menu;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simply, takes strings that might contain <code>%KEYWORD%</code> and substitutes the values
 * from a provided map.
 */
public class ParameterEvaluator {
    // TODO: 07/12/2014 - R4zorax: Eventually, get this from some i18n enabled config
    public static final String UNKNOWN = "\u00a7cUnknown";
    private static final Pattern KEY_PATTERN = Pattern.compile("\\$[A-Za-z.0-9_]+");
    private final Map<String, String> paramMap;

    public ParameterEvaluator(Map<String,String> paramMap) {
        this.paramMap = paramMap;
    }

    public String eval(String string) {
        if (string == null || string.trim().isEmpty()) {
            return "";
        }
        if (paramMap == null || paramMap.isEmpty()) {
            return string;
        }
        StringBuffer result = new StringBuffer();
        Matcher matcher = KEY_PATTERN.matcher(string);
        while (matcher.find()) {
            String key = matcher.group();
            matcher.appendReplacement(result, paramMap.containsKey(key) ? paramMap.get(key) : UNKNOWN);
        }
        matcher.appendTail(result);
        return result.toString();
    }
}

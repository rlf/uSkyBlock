package dk.lockfuglsang.minecraft.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Formatting Util
 * @since 1.9
 */
public class JSONUtil {
    private static final Map<String, String> FORMAT_MAP = new HashMap<>();
    static {
        // Colors
        FORMAT_MAP.put("black", "\u00a70");
        FORMAT_MAP.put("dark_blue", "\u00a71");
        FORMAT_MAP.put("dark_green", "\u00a72");
        FORMAT_MAP.put("dark_aqua", "\u00a73");
        FORMAT_MAP.put("dark_red", "\u00a74");
        FORMAT_MAP.put("dark_purple", "\u00a75");
        FORMAT_MAP.put("gold", "\u00a76");
        FORMAT_MAP.put("gray", "\u00a77");
        FORMAT_MAP.put("dark_gray", "\u00a78");
        FORMAT_MAP.put("blue", "\u00a79");
        FORMAT_MAP.put("green", "\u00a7a");
        FORMAT_MAP.put("aqua", "\u00a7b");
        FORMAT_MAP.put("red", "\u00a7c");
        FORMAT_MAP.put("light_purple", "\u00a7d");
        FORMAT_MAP.put("yellow", "\u00a7e");
        FORMAT_MAP.put("white", "\u00a7f");
        // Formatting
        FORMAT_MAP.put("obfuscated", "\u00a7k");
        FORMAT_MAP.put("bold", "\u00a7l");
        FORMAT_MAP.put("strikethrough", "\u00a7m");
        FORMAT_MAP.put("underline", "\u00a7n");
        FORMAT_MAP.put("italic", "\u00a7o");
        FORMAT_MAP.put("reset", "\u00a7r");
    }
    private static final List<String> FORMAT_FLAGS = Arrays.asList(
            "obfuscated", "bold", "strikethrough", "underline", "italic", "reset"
    );

    /**
     *
     * @param jsonMsg
     * @return
     */
    public static String json2String(String jsonMsg) {
        String msg = "";
        JSONParser parser = new JSONParser();
        try {
            Object jsonObj = parser.parse(new StringReader(jsonMsg));
            if (jsonObj instanceof JSONArray) {
                msg += json2String((JSONArray) jsonObj);
            } else if (jsonObj instanceof JSONObject) {
                msg += json2String((JSONObject) jsonObj);
            }
        } catch (ParseException | IOException e) {
            throw new IllegalArgumentException("Invalid JSON " + jsonMsg);
        }
        return msg;
    }

    public static String json2String(JSONArray jsonArray) {
        String msg = "";
        for (Object o : jsonArray) {
            if (o instanceof JSONArray) {
                msg += json2String((JSONArray) o);
            } else if (o instanceof JSONObject) {
                msg += json2String((JSONObject) o);
            }
        }
        return msg;
    }

    public static String json2String(JSONObject jsonObject) {
        String msg = "";
        if (jsonObject.containsKey("color")) {
            msg += FORMAT_MAP.get(jsonObject.get("color"));
        }
        for (String flag : FORMAT_FLAGS) {
            if (jsonObject.containsKey(flag) && Boolean.TRUE == jsonObject.get(flag)) {
                msg += FORMAT_MAP.get(flag);
            }
        }
        String text = (String) jsonObject.get("text");
        if (text != null) {
            msg += text;
        }
        if (jsonObject.get("extra") instanceof JSONArray) {
            msg += json2String((JSONArray) jsonObject.get("extra"));
        }
        return msg;
    }
}

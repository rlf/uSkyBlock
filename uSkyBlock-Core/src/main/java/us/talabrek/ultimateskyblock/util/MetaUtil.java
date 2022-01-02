package us.talabrek.ultimateskyblock.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public enum MetaUtil {;
    private static final Gson gson = new Gson();

    public static @NotNull Map<String, Object> createMap(String mapString) {
        if (mapString == null || mapString.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(mapString, mapType);
        } catch (JsonSyntaxException ex) {
            throw new IllegalArgumentException("Not a valid map: " + mapString, ex);
        }
    }
}

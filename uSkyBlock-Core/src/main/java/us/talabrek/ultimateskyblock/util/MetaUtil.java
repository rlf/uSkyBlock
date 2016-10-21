package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.nbt.NBTUtil;
import org.bukkit.inventory.ItemStack;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;

/**
 */
public enum MetaUtil {;

    public static Map<String,Object> createMap(String mapString) {
        if (mapString == null || mapString.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Object parse = new JSONParser().parse(new StringReader(mapString));
            if (parse instanceof Map) {
                return (Map<String,Object>)parse;
            }
        } catch (IOException | ParseException e) {
            throw new IllegalArgumentException("Not a valid map: " + mapString, e);
        }
        throw new IllegalArgumentException("Not a map: " + mapString);
    }

}

package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.util.EntityUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Data object holding values for matching an entity against a challenge.
 */
public class EntityMatch {
    /**
     * @see @link http://minecraft.gamepedia.com/Data_values#Wool.2C_Stained_Clay.2C_Stained_Glass_and_Carpet
     */
    private static final byte[] DV_2_COLOR_MAP = {
            0xf, // 0 -> white
            0x6, // 1 -> orange (gold)
            0xd, // 2 -> magenta (light purple)
            0x9, // 3 -> light blue
            0xe, // 4 -> yellow
            0xa, // 5 -> lime
            0xc, // 6 -> pink (red)
            0x8, // 7 -> gray (dark gray)
            0x7, // 8 -> light gray (gray)
            0xb, // 9 -> cyan (aqua)
            0x5, // 10 -> purple (dark purple)
            0x1, // 11 -> blue (dark blue)
            0x6, // 12 -> brown (no brown! we re-use gold)
            0x2, // 13 -> green (dark green)
            0x4, // 14 -> red (dark red)
            0x0  // 15 -> black
    };
    private static final String[] COLOR_KEYS = {"Color", "color"};
    private final EntityType type;
    private final Map<String,Object> meta;
    private final int count;

    public EntityMatch(EntityType type, Map<String, Object> meta, int count) {
        this.type = type;
        this.meta = meta != null ? meta : new HashMap<String,Object>();
        this.count = count;
    }

    public EntityType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public boolean matches(Entity entity) {
        if (entity != null && entity.getType() == type) {
            for (String key : meta.keySet()) {
                if (!matchFieldGetter(entity, key, meta.get(key))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean matchFieldGetter(Entity entity, String key, Object value) {
        try {
            Method method = entity.getClass().getMethod("get" + key, null);
            Object entityValue = method.invoke(entity);
            return matchValues(entityValue, value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            // Ignore
        } catch (NoSuchMethodException e) {
            return matchField(entity, key, value);
        }
        return false;
    }

    private boolean matchField(Entity entity, String key, Object value) {
        try {
            Field field = entity.getClass().getDeclaredField(key);
            boolean wasAccessible = field.isAccessible();
            if (!wasAccessible) {
                field.setAccessible(true);
            }
            Object entityValue = field.get(entity);
            boolean matchResult = matchValues(entityValue, value);
            if (!wasAccessible) {
                field.setAccessible(false);
            }
            return matchResult;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // Ignore
        }
        return false;
    }

    private boolean matchValues(Object entityValue, Object value) {
        if (value instanceof Number && entityValue instanceof Enum) {
            return ((Number) value).intValue() == ((Enum) entityValue).ordinal();
        } else if (value instanceof String && entityValue instanceof Enum) {
            return ((String) value).equalsIgnoreCase(((Enum) entityValue).name());
        }
        return ("" + entityValue).equalsIgnoreCase("" + value);
    }

    @Override
    public String toString() {
        return type.name() + (meta.isEmpty() ? "" : ":" + JSONObject.toJSONString(meta));
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        String name = EntityUtil.getEntityDisplayName(type);
        String color = getColorCode(meta);
        sb.append(color);
        sb.append(name);
        Map<String,Object> extra = new HashMap<>(meta);
        for (String key : COLOR_KEYS) {
            extra.remove(key);
        }
        if (!extra.isEmpty()) {
            sb.append(":").append(JSONObject.toJSONString(extra));
        }
        return sb.toString();
    }

    private String getColorCode(Map<String, Object> meta) {
        // TODO: 30/01/2016 - R4zorax: Support more entities?
        for (String key : COLOR_KEYS) {
            if (meta.containsKey(key)) {
                try {
                    int colorcode = Integer.parseInt("" + meta.get(key));
                    return  dataValueToFormattingCode(colorcode);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return "";
    }

    private String dataValueToFormattingCode(int colorcode) {
        return "\u00a7" + Integer.toHexString(DV_2_COLOR_MAP[colorcode & 0xf]);
    }
}

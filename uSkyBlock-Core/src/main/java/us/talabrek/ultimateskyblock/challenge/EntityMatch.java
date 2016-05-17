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
                    int textcolor = 0;
		    switch (colorcode)
			{
			case 0: textcolor =  15; break;    //white
			case 1: textcolor =  6;  break;    // orange -> gold
                        case 2: textcolor =  13; break;    //magenta->light purple
                        case 3: textcolor =  11; break;    //light blue -> aqua
                        case 4: textcolor =  14; break;    //yellow
                        case 5: textcolor =  10; break;    //lime -> green
                        case 6: textcolor =  12; break;    //pink -> lt red
                        case 7: textcolor =  8;  break;      //grey-> dark grey
                        case 8: textcolor =  7; break;    //lt grey -> grey
                        case 9: textcolor =  3; break;    //cyan -> dark aqua
                        case 10: textcolor = 5; break;    //purple
                        case 11: textcolor = 9; break;    //blue 
                        case 12: textcolor = 9; break;    //brown -> dark blue
                        case 13: textcolor = 2; break;    //green ->dark green
                        case 14: textcolor = 4; break;    //red -> dark red
                        case 15: textcolor = 0; break;    //black
			}
                    return "\u00a7" + Integer.toHexString(textcolor);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return "";
    }
}

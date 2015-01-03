package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Data object holding values for matching an entity against a challenge.
 */
public class EntityMatch {
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
}

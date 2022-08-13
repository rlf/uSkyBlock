package us.talabrek.ultimateskyblock.challenge;

import com.google.gson.Gson;
import org.apache.commons.text.WordUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Colorable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.uSkyBlock;
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
    private static final Gson gson = new Gson();
    private final uSkyBlock server = uSkyBlock.getInstance();

    private static final String[] COLOR_KEYS = {"Color", "color"};
    private final EntityType type;
    private final Map<String, Object> meta;
    private final int count;

    public EntityMatch(EntityType type, Map<String, Object> meta, int count) {
        this.type = type;
        this.meta = meta != null ? meta : new HashMap<>();
        this.count = count;
    }

    public EntityType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    /**
     * Try to match the given {@link Entity} to this EntityMatch configuration.
     * @param entity Entity to match.
     * @return True if matches, false otherwise.
     */
    public boolean matches(@Nullable Entity entity) {
        if (entity == null || entity.getType() != type) {
            return false;
        }

        for (String key : meta.keySet()) {
            if (key.equalsIgnoreCase("color")) {
                if (entity instanceof Colorable) {
                    return ((Colorable) entity).getColor() == getColor(meta.get(key));
                }
                return true;
            }

            if (!matchFieldGetter(entity, key, meta.get(key))) {
                return false;
            }
        }
        return true;
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
        return type.name() + (meta.isEmpty() ? "" : ":" + gson.toJson(meta));
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> extra = new HashMap<>(meta);
        for (String key : COLOR_KEYS) {
            if (meta.containsKey(key)) {
                String color = WordUtils.capitalizeFully(getColor(meta.get(key)).toString().replace("_", " "));
                sb.append(color).append(" ");
                extra.remove(key);
            }
        }

        String name = EntityUtil.getEntityDisplayName(type);
        sb.append(name);

        if (!extra.isEmpty()) {
            sb.append(":").append(gson.toJson(extra));
        }
        return sb.toString();
    }

    /**
     * Converts the given String to the corresponding {@link DyeColor}.
     * Defaults to {@link DyeColor#WHITE} on invalid or NULL input.
     * @param input DyeColor enum value.
     * @return Corresponding DyeColor, defaults to WHITE on invalid or NULL input.
     */
    private @NotNull DyeColor getColor(@Nullable String input) {
        try {
            return DyeColor.valueOf(input);
        } catch (IllegalArgumentException ex) {
            server.getLogger().warning("Invalid DyeColor value: " + input);
            server.getLogger().warning("See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html#enum-constant-summary for valid colors.");
        }

        return DyeColor.WHITE;
    }

    /**
     * Converts the given legacy integer value to the corresponsing {@link DyeColor}.
     * Defaults to {@link DyeColor#WHITE} on invalid or NULL input.
     * @param input Legacy DyeColor integer value.
     * @return Corresponding DyeColor, defaults to WHITE on invalid or NULL input.
     * @deprecated To be used for legacy challenge configs only, use {@link EntityMatch#getColor(String)}.
     */
    private @NotNull DyeColor getColor(@Nullable Number input) {
        if (input == null) {
            return DyeColor.WHITE;
        }

        return switch (input.intValue()) {
            case 0 -> DyeColor.WHITE;
            case 1 -> DyeColor.ORANGE;
            case 2 -> DyeColor.MAGENTA;
            case 3 -> DyeColor.LIGHT_BLUE;
            case 4 -> DyeColor.YELLOW;
            case 5 -> DyeColor.LIME;
            case 6 -> DyeColor.PINK;
            case 7 -> DyeColor.GRAY;
            case 8 -> DyeColor.LIGHT_GRAY;
            case 9 -> DyeColor.CYAN;
            case 10 -> DyeColor.PURPLE;
            case 11 -> DyeColor.BLUE;
            case 12 -> DyeColor.BROWN;
            case 13 -> DyeColor.GREEN;
            case 14 -> DyeColor.RED;
            case 15 -> DyeColor.BLACK;
            default -> DyeColor.WHITE;
        };
    }

    /**
     * Convenience method to translate a meta String or Number to the corresponding {@link DyeColor}.
     * See {@link EntityMatch#getColor(String)} for more info.
     * @param input Meta String or Number.
     * @return Corresponding DyeColor, defaults to WHITE.
     */
    private @NotNull DyeColor getColor(@Nullable Object input) {
        if (input instanceof String) {
            return getColor((String) input);
        } else if (input instanceof Number) {
            return getColor((Number) input);
        }

        return DyeColor.WHITE;
    }
}

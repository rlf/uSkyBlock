package us.talabrek.ultimateskyblock.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Utility for handling UUIDs.
 */
public enum UUIDUtil {;
    /**
     * Returns a {@link UUID} object from a UUID {@link String}. Accepts short Strings (withouth dashes). Returns null
     * if the given String is not a valid UUID.
     * @param id String to convert to a UUID.
     * @return UUID from the given String, or null if input is null or invalid.
     */
    @Nullable
    public static UUID fromString(@Nullable String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.length() == 32) {
            return UUID.fromString(getLongFromShort(id));
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ignore) {
            // ignore, do logging elsewhere
        }
        return null;
    }

    /**
     * Returns the given {@link UUID} as {@link String}. Returns an empty string if the given UUID is null.
     * @param uuid UUID to convert to string.
     * @return Given UUID as String, or an empty String if the given UUID is null.
     */
    @NotNull
    public static String asString(@Nullable UUID uuid) {
        if (uuid == null) {
            return "";
        }
        return uuid.toString();
    }

    /**
     * Convert the given short UUID {@link String} to a long UUID String.
     * @param id Short UUID String to convert.
     * @return Long UUID String.
     */
    @NotNull
    private static String getLongFromShort(@NotNull String id) {
        return id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" +
                id.substring(16, 20) + "-" + id.substring(20, 32);
    }
}

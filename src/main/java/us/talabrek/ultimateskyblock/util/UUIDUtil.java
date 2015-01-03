package us.talabrek.ultimateskyblock.util;

import java.util.UUID;

/**
 * Utility for handling UUIDs.
 */
public enum UUIDUtil {;
    public static UUID fromString(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return UUID.fromString(s);
    }

    public static String asString(UUID uuid) {
        if (uuid == null) {
            return "";
        }
        return uuid.toString();
    }
}

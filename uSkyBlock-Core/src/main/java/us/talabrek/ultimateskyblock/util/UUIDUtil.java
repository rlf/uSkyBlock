package us.talabrek.ultimateskyblock.util;

import java.util.UUID;

/**
 * Utility for handling UUIDs.
 */
public enum UUIDUtil {;
    public static UUID fromString(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.length() == 32) {
            return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
        }
        return UUID.fromString(id);
    }

    public static String asString(UUID uuid) {
        if (uuid == null) {
            return "";
        }
        return uuid.toString();
    }
}

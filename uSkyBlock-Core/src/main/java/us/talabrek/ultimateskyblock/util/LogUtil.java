package us.talabrek.ultimateskyblock.util;

import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.logging.Level;

public enum LogUtil {
    ;

    public static void log(Level level, String message, Throwable t) {
        uSkyBlock.getInstance().getLogger().log(level, message, t);
    }

    public static void log(Level level, String message) {
        uSkyBlock.getInstance().getLogger().log(level, message);
    }
}

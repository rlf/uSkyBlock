package us.talabrek.ultimateskyblock.util;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import us.talabrek.ultimateskyblock.Settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Convenience util for supporting static imports.
 */
public enum I18nUtil {;
    public static String tr(String s) {
        return getI18n().tr(s);
    }
    public static String tr(String s, Object... args) {
        return getI18n().tr(s, args);
    }

    public static I18n getI18n() {
        try {
            return I18nFactory.getI18n(I18nUtil.class, getLocale());
        } catch (MissingResourceException e) {
            Settings.locale = Locale.ENGLISH;
            return I18nFactory.getI18n(I18nUtil.class, getLocale());
        }
    }

    public static Locale getLocale() {
        return Settings.locale;
    }

    public static void clearCache() {
        try {
            Method clearCache = I18nFactory.class.getMethod("clearCache");
            if (!clearCache.isAccessible()) {
                clearCache.setAccessible(true);
            }
            clearCache.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Ignore - at least we tried
        }
    }
}

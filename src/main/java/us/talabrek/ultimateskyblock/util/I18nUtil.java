package us.talabrek.ultimateskyblock.util;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Locale;
import java.util.logging.Logger;

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

    private static I18n getI18n() {
        return I18nFactory.getI18n(I18nUtil.class, getLocale());
    }

    public static Locale getLocale() {
        return Settings.locale;
    }
}

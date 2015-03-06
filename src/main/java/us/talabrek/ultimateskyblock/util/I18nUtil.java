package us.talabrek.ultimateskyblock.util;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Convenience util for supporting static imports.
 */
public enum I18nUtil {;
    private static final I18n i18n = I18nFactory.getI18n(I18nUtil.class);

    public static String tr(String s) {
        return i18n.tr(s);
    }
    public static String tr(String s, Object... args) {
        return i18n.tr(s, args);
    }
}

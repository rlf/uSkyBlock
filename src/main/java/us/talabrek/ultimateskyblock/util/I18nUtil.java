package us.talabrek.ultimateskyblock.util;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Convenience util for supporting static imports.
 */
public enum I18nUtil {;
    private static final Logger log = Logger.getLogger(I18nUtil.class.getName());
    private static I18n i18n = I18nFactory.getI18n(I18nUtil.class, getLocale());

    private static Locale getLocale() {
        Locale locale = new Locale(uSkyBlock.getInstance().getConfig().getString("language", "en"));
        return locale;
    }

    public static void setLocale(Locale locale) {
        log.info("Setting uSkyBlock language to " + locale);
        i18n = I18nFactory.getI18n(I18nUtil.class, locale); // Cache
    }

    public static String tr(String s) {
        return i18n.tr(s);
    }
    public static String tr(String s, Object... args) {
        return i18n.tr(s, args);
    }
}

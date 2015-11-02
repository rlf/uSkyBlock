package us.talabrek.ultimateskyblock.util;

import dk.lockfuglsang.minecraft.po.POParser;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Convenience util for supporting static imports.
 */
public enum I18nUtil {;
    private static final Logger log = Logger.getLogger(I18nUtil.class.getName());
    private static I18n i18n;
    public static String tr(String s) {
        return getI18n().tr(s);
    }
    public static String tr(String s, Object... args) {
        return getI18n().tr(s, args);
    }

    /**
     * Just used for marking translations (dynamic ones) for .po files.
     */
    public static String marktr(String key) {
        return key;
    }

    public static I18n getI18n() {
        if (i18n == null) {
            i18n = new I18n(getLocale());
        }
        return i18n;
    }

    public static Locale getLocale() {
        return Settings.locale;
    }

    public static void clearCache() {
        i18n = null;
    }

    public static Locale getLocale(String lang) {
        if (lang != null) {
            // Why is this not just standard Java Locale??
            String[] parts = lang.split("[_\\-]");
            if (parts.length >= 3) {
                return new Locale(parts[0], parts[1], parts[2]);
            } else if (parts.length == 2) {
                return new Locale(parts[0], parts[1]);
            } else {
                return new Locale(parts[0]);
            }
        }
        return null;
    }

    /**
     * Proxy between uSkyBlock and org.xnap.commons.i18n.I18n
     */
    public static class I18n  {
        private final Locale locale;
        private List<Properties> props;

        I18n(Locale locale) {
            this.locale = locale;
            props = new ArrayList<>();
            addPropsFromPropertiesFile();
            addPropsFromPluginFolder();
            addPropsFromJar();
        }

        private void addPropsFromPropertiesFile() {
            Properties messages = FileUtil.readProperties("messages.properties");
            if (messages != null) {
                props.add(messages);
            }
        }

        private void addPropsFromJar() {
            // We zip the .po files, since they are currently half the footprint of the jar.
            try (
                    InputStream in = getClass().getClassLoader().getResourceAsStream("i18n.zip");
                    ZipInputStream zin = in != null ? new ZipInputStream(in, Charset.forName("UTF-8")) : null
            ) {
                ZipEntry nextEntry = null;
                do {
                    nextEntry = zin != null ? zin.getNextEntry() : null;
                    if (nextEntry != null && nextEntry.getName().equalsIgnoreCase(locale + ".po")) {
                        Properties i18nProps = POParser.asProperties(zin);
                        if (i18nProps != null && !i18nProps.isEmpty()) {
                            props.add(i18nProps);
                        }
                    }
                } while (nextEntry != null);
            } catch (IOException e) {
                log.info("Unable to load translations from i18n.zip!" + locale + ".po: "+ e);
            }
        }

        private void addPropsFromPluginFolder() {
            File poFile = new File(uSkyBlock.getInstance().getDataFolder(), "i18n/" + locale + ".po");
            if (poFile.exists()) {
                try (InputStream in = new FileInputStream(poFile)) {
                    Properties i18nProps = POParser.asProperties(in);
                    if (i18nProps != null && !i18nProps.isEmpty()) {
                        props.add(i18nProps);
                    }
                } catch (IOException e) {
                    log.info("Unable to load translations from i18n/" + locale + ".po: " + e);
                }
            }
        }

        public String tr(String key, Object... args) {
            for (Properties prop : props) {
                if (prop != null && prop.containsKey(key)) {
                    if (args.length > 0) {
                        return new MessageFormat(prop.getProperty(key), getLocale()).format(args);
                    } else {
                        return prop.getProperty(key);
                    }
                }
            }
            if (args.length > 0) {
                return new MessageFormat(key, getLocale()).format(args);
            } else {
                return key;
            }
        }

        public Locale getLocale() {
            return locale;
        }
    }
}

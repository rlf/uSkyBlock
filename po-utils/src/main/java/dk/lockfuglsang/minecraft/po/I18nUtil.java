package dk.lockfuglsang.minecraft.po;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Convenience util for supporting static imports.
 */
public enum I18nUtil {
    ;
    private static final Logger log = Logger.getLogger(I18nUtil.class.getName());
    private static I18n i18n;
    private static Locale locale;
    private static File dataFolder = new File(".");

    /**
     * Translates the given {@link String} to the configured language. Returns the given String if no translation is
     * available. Returns an empty String if the given key is null or empty.
     * @param s String to translate.
     * @return Translated String.
     */
    @NotNull
    public static String tr(@Nullable String s) {
        return getI18n().tr(s);
    }

    /**
     * Translates the given {@link String} to the configured language. Formats with the given {@link Object}. Returns
     * the given String if no translation is available. Returns an empty String if the given key is null or empty.
     * @param s String to translate.
     * @param args Arguments to format.
     * @return Translated String.
     */
    @NotNull
    public static String tr(@Nullable String s, @Nullable Object... args) {
        return getI18n().tr(s, args);
    }

    /**
     * Marks the given {@link String} for translation for the .po files.
     * @param key String to mark.
     * @return Input String.
     */
    @Contract("null -> null")
    public static String marktr(@Nullable String key) {
        return key;
    }

    /**
     * Formats the given {@link String} without translating. Returns an empty String if the given String is
     * null or empty.
     * @param s String to format.
     * @param args Arguments for formatting.
     * @return Formatted String.
     */
    @NotNull
    public static String pre(@Nullable String s, @Nullable Object... args) {
        if (s != null && !s.isEmpty()) {
            new MessageFormat(s, getLocale());
            return MessageFormat.format(s, args);
        }
        return "";
    }

    /**
     * Gets the {@link I18n} instance representing the configured {@link Locale}. Lazy-loads if necessary.
     * @return I18n instance for the configured locale.
     */
    public static I18n getI18n() {
        if (i18n == null) {
            i18n = new I18n(getLocale());
        }
        return i18n;
    }

    /**
     * Returns the configured {@link Locale} or the default if unset.
     * @return Configured Locale.
     */
    @NotNull
    public static Locale getLocale() {
        return locale != null ? locale : Locale.ENGLISH;
    }

    /**
     * Sets the {@link Locale}. Resets to the default locale if NULL is given.
     * @param locale Locale to set.
     */
    public static void setLocale(@Nullable Locale locale) {
        I18nUtil.locale = locale;
        clearCache();
    }

    /**
     * Sets the datafolder that is used to look for .po files.
     * @param folder Location of the datafolder.
     */
    public static void setDataFolder(@NotNull File folder) {
        dataFolder = folder;
        clearCache();
    }

    /**
     * Clears the I18n cache, forces a reload of the .po files the next time that {@link I18nUtil#getLocale()} is
     * accessed.
     */
    public static void clearCache() {
        i18n = null;
    }

    /**
     * Converts the given {@link String} to a {@link Locale}.
     * @param lang Language code..
     * @return Locale based on the given string.
     */
    @Contract("null -> null")
    public static Locale getLocale(@Nullable String lang) {
        if (lang != null) {
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
    public static class I18n {
        private final Locale locale;
        private final Properties translations = new Properties();

        I18n(Locale locale) {
            this.locale = locale;

            // Order of these calls is important here, because it specifies the priority of the files (git rlf/1233).
            addPropertiesFromZipInJar().ifPresent(properties -> {
                translations.putAll(properties);
                log.log(Level.INFO, "Added {0} translations from ZIP inside JAR.", properties.size());
            });
            addPropertiesFromJar().ifPresent(properties -> {
                translations.putAll(properties);
                log.log(Level.INFO, "Added {0} translations from the JAR.", properties.size());
            });
            addPropertiesFromPluginFolder().ifPresent(properties -> {
                translations.putAll(properties);
                log.log(Level.INFO, "Added {0} translations from the plugin directory.", properties.size());
            });

            log.log(Level.INFO, "Loaded {0} translations.", translations.size());
        }

        private Optional<Properties> addPropertiesFromJar() {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("po/" + locale + ".po")) {
                if (in == null) {
                    return Optional.empty();
                }
                Properties properties = POParser.asProperties(in);
                return Optional.ofNullable(properties);
            } catch (IOException e) {
                log.info("Unable to read translations from po/" + locale + ".po: " + e);
            }
            return Optional.empty();
        }

        private Optional<Properties> addPropertiesFromZipInJar() {
            // We zip the .po files, since they are currently half the footprint of the jar.
            try (
                    InputStream in = getClass().getClassLoader().getResourceAsStream("i18n.zip");
                    ZipInputStream zin = in != null ? new ZipInputStream(in, StandardCharsets.UTF_8) : null
            ) {
                ZipEntry nextEntry;
                do {
                    nextEntry = zin != null ? zin.getNextEntry() : null;
                    if (nextEntry != null && nextEntry.getName().equalsIgnoreCase(locale + ".po")) {
                        Properties properties = POParser.asProperties(zin);
                        return Optional.ofNullable(properties);
                    }
                } while (nextEntry != null);
            } catch (IOException e) {
                log.info("Unable to load translations from i18n.zip!" + locale + ".po: " + e);
            }
            return Optional.empty();
        }

        private Optional<Properties> addPropertiesFromPluginFolder() {
            File poFile = new File(dataFolder, "i18n" + File.separator + locale + ".po");
            if (poFile.exists()) {
                try (InputStream in = new FileInputStream(poFile)) {
                    Properties properties = POParser.asProperties(in);
                    return Optional.ofNullable(properties);
                } catch (IOException e) {
                    log.info("Unable to load translations from i18n" + File.separator + locale + ".po: " + e);
                }
            }
            return Optional.empty();
        }

        public String tr(String key, Object... args) {
            if (key == null || key.trim().isEmpty()) {
                return "";
            }
            String propKey = translations.getProperty(key);
            if (propKey != null && !propKey.trim().isEmpty()) {
                return format(propKey, args);
            }
            return format(key, args);
        }

        private String format(String propKey, Object[] args) {
            try {
                return new MessageFormat(propKey, getLocale()).format(args);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Problem with: '" + propKey + "'", e);
            }
        }

        public Locale getLocale() {
            return locale;
        }
    }
}

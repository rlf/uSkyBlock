package dk.lockfuglsang.minecraft.po;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Parses a simple PO file, and returns the result as a Properties object.
 */
public class POParser {
    public static Properties asProperties(InputStream in) throws IOException {
        return new POParser().readPOAsProperties(in);
    }

    public Properties readPOAsProperties(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        Properties props = new Properties();
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line = null;
            String key = null;
            String value = null;
            int lineNo = 0;
            while ((line = rdr.readLine()) != null) {
                line = line.trim();
                lineNo++;
                if (line.startsWith("#")) {
                    // skip - comment
                    continue;
                }
                if (line.startsWith("msgid \"") && line.endsWith("\"")) {
                    if (key != null && value == null) {
                        throw new IOException("Malformed po, msgid without msgstr! at line " + lineNo);
                    }
                    addTranslation(props, key, value);
                    value = null;
                    key = line.substring(7, line.length()-1);
                } else if (line.startsWith("msgstr \"") && line.endsWith("\"")) {
                    if (key == null) {
                        throw new IOException("Malformed po, msgstr wihtout msgid! at line " + lineNo);
                    }
                    if (value != null) {
                        throw new IOException("Malformed po, msgstr before msgid! at line " + lineNo);
                    }
                    value = line.substring(8, line.length()-1);
                } else if (value != null && line.startsWith("\"") && line.endsWith("\"")) {
                    value += line.substring(1, line.length()-1);
                } else if (key != null && line.startsWith("\"") && line.endsWith("\"")) {
                    key += line.substring(1, line.length()-1);
                } else if (!line.isEmpty()) {
                    throw new IOException("Malformed po, did not expect '" + line + "' at line " + lineNo);
                }
            }
            addTranslation(props, key, value);
        }
        return props;
    }

    private void addTranslation(Properties props, String key, String value) {
        if (key != null && value != null) {
            props.put(normalize(key), normalize(value));
        }
    }

    private String normalize(String key) {
        return key.replaceAll("\\\\n", "\n");
    }
}

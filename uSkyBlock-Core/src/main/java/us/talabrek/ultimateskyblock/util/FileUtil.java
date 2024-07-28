package us.talabrek.ultimateskyblock.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Common file-utilities.
 */
public enum FileUtil {;

    public static String getBasename(File file) {
        return getBasename(file.getName());
    }

    public static String getBasename(String file) {
        if (file != null && file.lastIndexOf('.') != -1) {
            return file.substring(0, file.lastIndexOf('.'));
        }
        return file;
    }

    public static String getExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            return fileName.substring(getBasename(fileName).length()+1);
        }
        return "";
    }

    public static void copy(InputStream stream, File file) throws IOException {
        if (stream == null || file == null) {
            throw new IOException("Invalid resource for " + file);
        }
        Files.copy(stream, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }
}

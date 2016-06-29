package us.talabrek.ultimateskyblock.imports;

import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;

/**
 * Interface for importers.
 */
public interface USBImporter {
    /**
     * Identifies the importer.
     * @return The name of the importer
     */
    String getName();

    void init(uSkyBlock plugin);

    /**
     * Imports the player into the existing data structure.
     * @param file The file to import.
     * @return <code>true</code> iff the import was successful.
     */
    Boolean importFile(File file);

    /**
     * Returns the candidates for import.
     * @return
     */
    File[] getFiles();

    void completed(int success, int failed, int skipped);
}

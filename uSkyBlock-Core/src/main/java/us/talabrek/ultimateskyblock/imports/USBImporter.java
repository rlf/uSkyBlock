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

    /**
     * Imports the player into the existing data structure.
     * @param plugin        The USB plugin
     * @param file The file to import.
     * @return <code>true</code> iff the import was successful.
     */
    boolean importFile(uSkyBlock plugin, File file);

    /**
     * Imports various files from the data-folder (i.e. orphans etc.).
     * @param plugin        The USB plugin
     * @param configFolder  The base-folder of the plugin
     * @return <code>true</code> if anything was imported, false otherwise.
     */
    int importOrphans(uSkyBlock plugin, File configFolder);

    /**
     * Returns the candidates for import.
     * @param plugin
     * @return
     */
    File[] getFiles(uSkyBlock plugin);

    void completed(uSkyBlock plugin, int success, int failed);
}

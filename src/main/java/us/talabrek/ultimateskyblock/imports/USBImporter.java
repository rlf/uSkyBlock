package us.talabrek.ultimateskyblock.imports;

import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;

/**
 * Interface for importers.
 */
public interface USBImporter {
    /**
     * Imports the player into the existing data structure.
     * @param plugin        The USB plugin
     * @param playerFile The file to import.
     * @return <code>true</code> iff the import was successful.
     */
    boolean importPlayer(uSkyBlock plugin, File playerFile);

    /**
     * Imports various files from the data-folder (i.e. orphans etc.).
     * @param plugin        The USB plugin
     * @param configFolder  The base-folder of the plugin
     * @return <code>true</code> if anything was imported, false otherwise.
     */
    int importOrphans(uSkyBlock plugin, File configFolder);
}

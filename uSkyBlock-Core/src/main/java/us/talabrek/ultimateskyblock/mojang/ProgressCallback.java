package us.talabrek.ultimateskyblock.mojang;

/**
 * A simple progress callback interface.
 */
public interface ProgressCallback {
    void progress(int progress, int failed, int total, String message);

    void complete(boolean success);

    void error(String message);
}

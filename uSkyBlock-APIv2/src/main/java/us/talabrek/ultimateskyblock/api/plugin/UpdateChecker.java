package us.talabrek.ultimateskyblock.api.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public interface UpdateChecker {
    URI URL_RELEASE = URI.create("https://www.uskyblock.ovh/versions/release.json");
    URI URL_STAGING = URI.create("https://www.uskyblock.ovh/versions/staging.json");

    /**
     * Compares the current version and latest version (if available) to see if there is a new version available.
     * @return True if new version is available, false otherwise (no new version available or version info unavailable).
     */
    boolean isUpdateAvailable();

    /**
     * Gets the latest release of uSkyBlock. Returns NULL if the version info hasn't been fetched yet or is unavailable.
     * @return Latest release of uSkyBlock, NULL when unavailable.
     */
    @Nullable String getLatestVersion();

    /**
     * Gets the current version of uSkyBlock running on the server.
     * @return Current version of uSkyBlock.
     */
    @NotNull String getCurrentVersion();

    /**
     * Fetches the latest version info from the uSkyBlock website. Returns a {@link CompletableFuture <String>},
     * completes the HTTP request async. The CompletableFuture will contain NULL when version info cannot be obtained.
     * @param uri URI to use for the HTTP request, official links are
     * {@link UpdateChecker#URL_RELEASE} and {@link UpdateChecker#URL_STAGING}.
     * @return CompletableFuture with the latest version info.
     */
    CompletableFuture<String> fetchLatestVersion(URI uri);

    /**
     * Compares two version numbers. Returns a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object.
     * @see Comparable#compareTo(Object).
     * @param currentVersion Current version number (may contain -SNAPSHOT).
     * @param newVersion New version number (may contain -SNAPSHOT).
     * @return Negative integer, zero, or a positive integer as this object is less than,
     * equal to, or greater than the specified object.
     */
    boolean isNewerVersion(String currentVersion, String newVersion);
}

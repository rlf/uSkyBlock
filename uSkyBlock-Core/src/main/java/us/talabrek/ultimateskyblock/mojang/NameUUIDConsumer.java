package us.talabrek.ultimateskyblock.mojang;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumer for Mojang API queries for Name-UUID pairs.
 * This allows for async. querying, and updating/caching of large data-sets.
 */
public interface NameUUIDConsumer {
    /**
     * Map from name to UUID
     * @param names Map from name to UUID
     */
    void success(Map<String, UUID> names);

    /**
     * A player that was renamed.
     * @param oldName The old-name (the one queried).
     * @param newName The new-name (the current).
     * @param id The UUID.
     */
    void renamed(String oldName, String newName, UUID id);

    /**
     * List of names unknown to Mojang.
     * @param unknownNames List of names unknown to Mojang.
     */
    void unknown(List<String> unknownNames);
}

package us.talabrek.ultimateskyblock.handler.placeholder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.api.IslandRank;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.LimitLogic;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static dk.lockfuglsang.minecraft.po.I18nUtil.pre;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * The actual replacer for placeholders
 */
public class PlaceholderReplacerImpl implements PlaceholderAPI.PlaceholderReplacer {
    private static final Set<String> PLACEHOLDERS = new HashSet<>(Arrays.asList(
            "usb_version",
            "usb_island_level",
            "usb_island_rank",
            "usb_island_leader",
            "usb_island_golems_max",
            "usb_island_monsters_max",
            "usb_island_animals_max",
            "usb_island_villagers_max",
            "usb_island_partysize_max",
            "usb_island_golems",
            "usb_island_monsters",
            "usb_island_animals",
            "usb_island_villagers",
            "usb_island_partysize",
            "usb_island_biome",
            "usb_island_bans",
            "usb_island_members",
            "usb_island_trustees",
            "usb_island_location",
            "usb_island_location_x",
            "usb_island_location_y",
            "usb_island_location_z",
            "usb_island_schematic"
    ));
    private final uSkyBlock plugin;
    private final LoadingCache<CacheEntry, String> cache;

    public PlaceholderReplacerImpl(uSkyBlock plugin) {
        this.plugin = plugin;
        cache = CacheBuilder
                .from(plugin.getConfig().getString("options.advanced.placeholderCache",
                        "maximumSize=200,expireAfterWrite=20s"))
                .build(new CacheLoader<CacheEntry, String>() {
                    @Override
                    public String load(CacheEntry cacheEntry) throws Exception {
                        try {
                            return lookup(cacheEntry);
                        } catch (RuntimeException e) {
                            throw new ExecutionException(e.getMessage(), e);
                        }
                    }
                });
    }

    private String lookup(CacheEntry entry) {
        String placeholder = entry.getPlaceholder();
        if (placeholder.startsWith("usb_island")) {
            PlayerInfo playerInfo = plugin.getPlayerLogic().getPlayerInfo(entry.getUuid());
            IslandInfo islandInfo = plugin.getIslandLogic().getIslandInfo(playerInfo);
            if (playerInfo == null || islandInfo == null) {
                return tr("N/A");
            }
            return lookup(islandInfo, placeholder);
        } else if (placeholder.startsWith("usb_")) {
            return lookup(placeholder);
        }
        throw new IllegalArgumentException("Unsupported placeholder " + placeholder);
    }

    private String lookup(String placeholder) {
        switch (placeholder) {
            case "usb_version": return plugin.getDescription().getVersion();
        }
        throw new IllegalArgumentException("Unsupported placeholder " + placeholder);
    }

    private String lookup(IslandInfo islandInfo, String placeholder) {
        switch (placeholder) {
            case "usb_island_level": return pre("{0,number,##.#}", islandInfo.getLevel());
            case "usb_island_rank": return getRank(islandInfo);
            case "usb_island_leader": return islandInfo.getLeader();
            case "usb_island_golems_max": return "" + islandInfo.getMaxGolems();
            case "usb_island_monsters_max": return "" + islandInfo.getMaxMonsters();
            case "usb_island_animals_max": return "" + islandInfo.getMaxAnimals();
            case "usb_island_villagers_max": return "" + islandInfo.getMaxVillagers();
            case "usb_island_partysize_max": return "" + islandInfo.getMaxPartySize();
            case "usb_island_golems": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.GOLEM);
            case "usb_island_monsters": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.MONSTER);
            case "usb_island_animals": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.ANIMAL);
            case "usb_island_villagers": return "" + plugin.getLimitLogic().getCreatureCount(islandInfo).get(LimitLogic.CreatureType.VILLAGER);
            case "usb_island_partysize": return "" + islandInfo.getPartySize();
            case "usb_island_biome": return islandInfo.getBiome();
            case "usb_island_bans": return ""+islandInfo.getBans();
            case "usb_island_members": return ""+islandInfo.getMembers();
            case "usb_island_trustees": return ""+islandInfo.getTrustees();
            case "usb_island_location": return LocationUtil.asString(islandInfo.getIslandLocation());
            case "usb_island_location_x": return pre("{0,number,#}", islandInfo.getIslandLocation().getBlockX());
            case "usb_island_location_y": return pre("{0,number,#}", islandInfo.getIslandLocation().getBlockY());
            case "usb_island_location_z": return pre("{0,number,#}", islandInfo.getIslandLocation().getBlockZ());
            case "usb_island_schematic": return islandInfo.getSchematicName();
        }
        throw new IllegalArgumentException("Unsupported placeholder " + placeholder);
    }

    private String getRank(IslandInfo islandInfo) {
        IslandRank rank = plugin.getIslandLogic().getRank(islandInfo.getName());
        if (rank != null) {
            return pre("{0,number,#}", rank.getRank());
        } else {
            return tr("N/A");
        }
    }

    @Override
    public Set<String> getPlaceholders() {
        return PLACEHOLDERS;
    }

    @Override
    public String replace(OfflinePlayer offlinePlayer, Player player, String placeholder) {
        if (placeholder == null || !placeholder.startsWith("usb_")) {
            return null;
        }
        UUID uuid = player != null ? player.getUniqueId() : null;
        if (uuid == null && offlinePlayer != null) {
            uuid = offlinePlayer.getUniqueId();
        }
        if (uuid == null) {
            return null;
        }
        CacheEntry cacheKey = new CacheEntry(uuid, placeholder);
        try {
            return cache.get(cacheKey);
        } catch (ExecutionException e) {
            return null;
        }
    }

    private static class CacheEntry {
        private final UUID uuid;
        private final String placeholder;

        private CacheEntry(UUID uuid, String placeholder) {
            this.uuid = uuid;
            this.placeholder = placeholder;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheEntry that = (CacheEntry) o;

            if (!placeholder.equals(that.placeholder)) return false;
            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + placeholder.hashCode();
            return result;
        }
    }
}

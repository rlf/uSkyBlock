package us.talabrek.ultimateskyblock.player;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandGenerator;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Responsible for calculating player specific perks based on permissions.
 */
public class PerkLogic {
    private final uSkyBlock plugin;
    private final LoadingCache<UUID, Perk> cache;
    private final Perk defaultPerk;
    private Map<String, Perk> donorPerks;

    public PerkLogic(uSkyBlock plugin, IslandGenerator islandGenerator) {
        this.plugin = plugin;
        defaultPerk = new Perk(ItemStackUtil.createItemList(""), Settings.general_maxPartySize,
                plugin.getConfig().getInt("options.island.spawn-limits.animals", 30),
                plugin.getConfig().getInt("options.island.spawn-limits.monsters", 50),
                plugin.getConfig().getInt("options.island.spawn-limits.villagers", 16),
                0,
                0,
                null);
        donorPerks = new ConcurrentHashMap<>();
        addDonorPerks(null, plugin.getConfig().getConfigurationSection("donor-perks"));
        addExtraPermissionPerks(plugin.getConfig().getConfigurationSection("options.island.extraPermissions"));
        addPartyPermissionPerks(null, plugin.getConfig().getConfigurationSection("options.party.maxPartyPermissions"));
        addHungerPerms();
        addDonorRewardPerks();
        addSchemePerks(islandGenerator.getSchemeNames());
        this.cache = CacheBuilder
                .from(plugin.getConfig().getString("options.advanced.perkCache", "maximumSize=200,expireAfterWrite=15m,expireAfterAccess=10m"))
                .build(new CacheLoader<UUID, Perk>() {
                    @Override
                    public Perk load(UUID uuid) throws Exception {
                        return createPerk(Bukkit.getPlayer(uuid));
                    }
                });
    }

    public Perk getDefaultPerk() {
        return defaultPerk;
    }

    public Perk getPerk(Player player) {
        try {
            return cache.get(player.getUniqueId());
        } catch (ExecutionException e) {
            return defaultPerk;
        }
    }

    private Perk createPerk(Player player) {
        PerkBuilder builder = new PerkBuilder(defaultPerk);
        for (String perm : donorPerks.keySet()) {
            if (VaultHandler.checkPerm(player, perm, plugin.getWorld())) {
                builder.combine(donorPerks.get(perm));
            }
        }
        return builder.build();
    }

    private void addDonorPerks(String perm, ConfigurationSection config) {
        if (config == null) {
            return;
        }
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                addDonorPerks((perm != null ? perm + "." : "") + key, config.getConfigurationSection(key));
            } else {
                // Read leaf
                donorPerks.put(perm, new Perk(
                        ItemStackUtil.createItemList(config.getString("extraItems", "")),
                        config.getInt("maxPartySize", defaultPerk.getMaxPartySize()),
                        config.getInt("animals", defaultPerk.getAnimals()),
                        config.getInt("monsters", defaultPerk.getMonsters()),
                        config.getInt("villagers", defaultPerk.getVillagers()),
                        config.getDouble("rewBonus", defaultPerk.getRewBonus()),
                        config.getDouble("hungerReduction", defaultPerk.getHungerReduction()),
                        config.getStringList("schematics")));
            }
        }
    }

    private void addExtraPermissionPerks(ConfigurationSection config) {
        if (config == null) {
            return;
        }
        for (String key : config.getKeys(false)) {
            List<ItemStack> items = ItemStackUtil.createItemList(config.getString(key, ""));
            if (items != null && !items.isEmpty()) {
                String perm = "usb." + key;
                donorPerks.put(perm, new PerkBuilder(donorPerks.get(perm))
                        .extraItems(items)
                        .build());
            }
        }
    }

    private void addPartyPermissionPerks(String perm, ConfigurationSection config) {
        int[] values = {5, 6, 7, 8};
        String[] perms = {"usb.extra.partysize1","usb.extra.partysize2","usb.extra.partysize3","usb.extra.partysize"};
        for (int i = 0; i < values.length; i++) {
            donorPerks.put(perms[i],
                    new PerkBuilder(donorPerks.get(perms[i]))
                            .maxPartySize(values[i])
                            .build());
        }

        if (config == null) {
            return;
        }
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                addPartyPermissionPerks((perm != null ? perm + "." : "") + key, config.getConfigurationSection(key));
            } else if (config.isInt(key)) {
                // Read leaf
                donorPerks.put(perm, new PerkBuilder(donorPerks.get(perm))
                        .maxPartySize(config.getInt(key, 0))
                        .build());
            }
        }
    }

    private void addHungerPerms() {
        double[] values = {1, 0.75, 0.50, 0.25};
        String[] perms = {"usb.extra.hunger4", "usb.extra.hunger3", "usb.extra.hunger2", "usb.extra.hunger"};
        for (int i = 0; i < values.length; i++) {
            donorPerks.put(perms[i],
                    new PerkBuilder(donorPerks.get(perms[i]))
                            .hungerReduction(values[i])
                            .build());
        }
    }

    private void addDonorRewardPerks() {
        // Note: This is NOT the same as before, but it's trying to be as close as possible.
        double[] values = {0.05, 0.10, 0.15, 0.20, 0.30, 0.50};
        String[] perms = {"group.memberplus", "usb.donor.all", "usb.donor.25", "usb.donor.50", "usb.donor.75", "usb.donor.100"};
        for (int i = 0; i < values.length; i++) {
            donorPerks.put(perms[i],
                    new PerkBuilder(donorPerks.get(perms[i]))
                            .rewBonus(values[i])
                            .build());
        }
    }

    private void addSchemePerks(List<String> schemeNames) {
        if (schemeNames == null) {
            return;
        }
        for (String scheme : schemeNames) {
            String perm = "usb.scheme." + scheme;
            donorPerks.put(perm, new PerkBuilder(donorPerks.get(perm))
                    .schematics(scheme)
                    .build());
        }
    }

    public Map<String, Perk> getPerkMap() {
        return Collections.unmodifiableMap(donorPerks);
    }

    public static class PerkBuilder {
        private Perk perk;

        public PerkBuilder() {
            perk = new Perk(null, 0, 0, 0, 0, 0, 0, null);
        }

        public PerkBuilder(Perk basePerk) {
            perk = basePerk != null ? basePerk : new Perk(null, 0, 0, 0, 0, 0, 0, null);
        }

        public PerkBuilder extraItems(List<ItemStack> items) {
            perk = perk.combine(new Perk(items, 0, 0, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder maxPartySize(int max) {
            perk = perk.combine(new Perk(null, max, 0, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder animals(int animals) {
            perk = perk.combine(new Perk(null, 0, animals, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder monsters(int monsters) {
            perk = perk.combine(new Perk(null, 0, 0, monsters, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder villagers(int villagers) {
            perk = perk.combine(new Perk(null, 0, 0, 0, villagers, 0, 0, null));
            return this;
        }

        public PerkBuilder rewBonus(double bonus) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, bonus, 0, null));
            return this;
        }

        public PerkBuilder hungerReduction(double reduction) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, 0, reduction, null));
            return this;
        }

        public PerkBuilder schematics(String... schemes) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, 0, 0, Arrays.asList(schemes)));
            return this;
        }

        public PerkBuilder combine(Perk other) {
            if (other != null) {
                perk = perk.combine(other);
            }
            return this;
        }

        public Perk build() {
            return perk;
        }
    }
}

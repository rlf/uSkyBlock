package us.talabrek.ultimateskyblock.player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandGenerator;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for calculating player specific perks based on permissions.
 */
public class PerkLogic {
    private final uSkyBlock plugin;
    private final Perk defaultPerk;
    private Map<String, Perk> donorPerks;
    private Map<String, Perk> islandPerks;

    public PerkLogic(uSkyBlock plugin, IslandGenerator islandGenerator) {
        this.plugin = plugin;
        defaultPerk = new Perk(ItemStackUtil.createItemList(""), Settings.general_maxPartySize,
                plugin.getConfig().getInt("options.island.spawn-limits.animals", 30),
                plugin.getConfig().getInt("options.island.spawn-limits.monsters", 50),
                plugin.getConfig().getInt("options.island.spawn-limits.villagers", 16),
                plugin.getConfig().getInt("options.island.spawn-limits.golems", 4),
                0,
                0,
                null);
        donorPerks = new ConcurrentHashMap<>();
        addDonorPerks(null, plugin.getConfig().getConfigurationSection("donor-perks"));
        addExtraPermissionPerks(plugin.getConfig().getConfigurationSection("options.island.extraPermissions"));
        addPartyPermissionPerks(null, plugin.getConfig().getConfigurationSection("options.party.maxPartyPermissions"));
        addHungerPerms();
        addDonorRewardPerks();
        List<String> schemeNames = islandGenerator.getSchemeNames();
        addSchemePerks(schemeNames);
        islandPerks = new ConcurrentHashMap<>();
        for (String schemeName : schemeNames) {
            islandPerks.put("usb.schematic." + schemeName, new PerkBuilder(defaultPerk).schematics(schemeName).build());
        }
        ConfigurationSection islandSchemes = plugin.getConfig().getConfigurationSection("island-schemes");
        if (islandSchemes != null) {
            for (String schemeName : islandSchemes.getKeys(false)) {
                ConfigurationSection config = islandSchemes.getConfigurationSection(schemeName);
                String perm = config.getString("permission", "usb.schematic." + schemeName);
                Perk perk = islandPerks.containsKey(perm) ? islandPerks.get(perm) : defaultPerk;
                perk = new PerkBuilder(perk).schematics(schemeName)
                        .maxPartySize(config.getInt("maxPartySize", 0))
                        .animals(config.getInt("animals", 0))
                        .monsters(config.getInt("monsters", 0))
                        .villagers(config.getInt("villagers", 0))
                        .golems(config.getInt("golems", 0))
                        .rewBonus(config.getInt("rewardBonus", 0))
                        .hungerReduction(config.getInt("hungerReduction", 0))
                        .build();
                islandPerks.put(perm, perk);
            }
        }
    }

    public Perk getDefaultPerk() {
        return defaultPerk;
    }

    public Perk getPerk(Player player) {
        return createPerk(player);
    }

    public Set<String> getSchemes(Player player) {
        Set<String> schemes = new LinkedHashSet<>();
        for (String perm : islandPerks.keySet()) {
            if (player.hasPermission(perm)) {
                schemes.addAll(islandPerks.get(perm).getSchematics());
            }
        }
        return schemes;
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
                        config.getInt("golems", defaultPerk.getGolems()),
                        config.getDouble("rewardBonus", defaultPerk.getRewBonus()),
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
            String perm = "usb.schematic." + scheme;
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
            perk = new Perk(null, 0, 0, 0, 0, 0, 0, 0, null);
        }

        public PerkBuilder(Perk basePerk) {
            perk = basePerk != null ? basePerk : new Perk(null, 0, 0, 0, 0, 0, 0, 0, null);
        }

        public PerkBuilder extraItems(List<ItemStack> items) {
            perk = perk.combine(new Perk(items, 0, 0, 0, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder maxPartySize(int max) {
            perk = perk.combine(new Perk(null, max, 0, 0, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder animals(int animals) {
            perk = perk.combine(new Perk(null, 0, animals, 0, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder monsters(int monsters) {
            perk = perk.combine(new Perk(null, 0, 0, monsters, 0, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder villagers(int villagers) {
            perk = perk.combine(new Perk(null, 0, 0, 0, villagers, 0, 0, 0, null));
            return this;
        }

        public PerkBuilder golems(int golems) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, golems, 0, 0, null));
            return this;
        }

        public PerkBuilder rewBonus(double bonus) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, 0, bonus, 0, null));
            return this;
        }

        public PerkBuilder hungerReduction(double reduction) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, 0, 0, reduction, null));
            return this;
        }

        public PerkBuilder schematics(String... schemes) {
            perk = perk.combine(new Perk(null, 0, 0, 0, 0, 0, 0, 0, Arrays.asList(schemes)));
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

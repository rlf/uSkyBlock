package us.talabrek.ultimateskyblock.command.island;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.task.SetBiomeTask;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashMap;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;
import static us.talabrek.ultimateskyblock.util.BiomeUtil.getBiome;

public class BiomeCommand extends RequireIslandCommand {
    public static final Map<String, Biome> BIOMES = new HashMap<String, Biome>() {
        {
            put("ocean", Biome.OCEAN);
            put("jungle", Biome.JUNGLE);
            put("hell", Biome.NETHER);
            put("sky", Biome.THE_END);
            put("mushroom", Biome.MUSHROOM_FIELDS);
            put("swampland", Biome.SWAMP);
            put("taiga", Biome.SNOWY_TAIGA);
            put("desert", Biome.DESERT);
            put("forest", Biome.FOREST);
            put("plains", Biome.PLAINS);
            put("extreme_hills", Biome.DARK_FOREST_HILLS);
            put("deep_ocean", Biome.DEEP_OCEAN);
            Biome b = getBiome("ICE_PLAINS");
            if (b != null) {
                put("ice_plains", b);
            }
            b = getBiome("FLOWER_FOREST");
            if (b != null) {
                put("flower_forest", b);
            }
        }
    };
    private final SkyBlockMenu menu;

    public BiomeCommand(uSkyBlock plugin, SkyBlockMenu menu) {
        super(plugin, "biome|b", null, "biome ?radius", marktr("change the biome of the island"));
        this.menu = menu;
        addFeaturePermission("usb.exempt.cooldown.biome", tr("exempt player from biome-cooldown"));
        for (String biome : BIOMES.keySet()) {
            addFeaturePermission("usb.biome." + biome, tr("Let the player change their islands biome to {0}", biome.toUpperCase()));
        }
    }

    @Override
    protected boolean doExecute(String alias, final Player player, PlayerInfo pi, final IslandInfo island, Map<String, Object> data, final String... args) {
        if (args.length == 0) {
            if (!island.hasPerm(player, "canChangeBiome")) {
                player.sendMessage(tr("\u00a7cYou do not have permission to change the biome of your current island."));
            } else {
                Map.Entry<Inventory, String> inv = menu.displayBiomeGUI(player);
                menu.getInventoryManager().createInventory(player, inv.getKey(), inv.getValue());
            }
        }
        if (args.length >= 1) {
            final String biome = args[0].toLowerCase();
            if (!island.hasPerm(player, "canChangeBiome")) {
                player.sendMessage(tr("\u00a74You do not have permission to change the biome of this island!"));
                return true;
            }
            Location location = player.getLocation();
            ProtectedRegion region = WorldGuardHandler.getIslandRegionAt(location);
            if (!plugin.playerIsOnOwnIsland(player) || region == null) {
                player.sendMessage(tr("\u00a7eYou must be on your island to change the biome!"));
                return true;
            }
            if (!biomeExists(biome)) {
                player.sendMessage(tr("\u00a7cYou have misspelled the biome name. Must be one of {0}", BIOMES.keySet()));
                return true;
            }
            int cooldown = plugin.getCooldownHandler().getCooldown(player, "biome");
            if (cooldown > 0) {
                player.sendMessage(tr("\u00a7eYou can change your biome again in {0,number,#} minutes.", cooldown / 60));
                return true;
            }
            if (!player.hasPermission("usb.biome." + biome.toLowerCase())) {
                player.sendMessage(tr("\u00a7cYou do not have permission to change your biome to that type."));
                return true;
            }
            BlockVector3 minP = region.getMinimumPoint();
            BlockVector3 maxP = region.getMaximumPoint();
            if (args.length == 2 && args[1].matches("[0-9]+")) {
                int radius = Integer.parseInt(args[1], 10);
                Location loc = location.clone().add(-radius, 0, -radius);
                if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                    minP = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                }
                loc = location.clone().add(radius, 0, radius);
                if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                    maxP = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                }
                player.sendMessage(tr("\u00a77The pixies are busy changing the biome near you to \u00a79{0}\u00a77, be patient.", biome));
            } else if (args.length == 2 && args[1].equalsIgnoreCase("chunk")) {
                Chunk chunk = location.clone().getChunk();
                minP = BlockVector3.at(chunk.getX() << 4, 0, chunk.getZ() << 4);
                maxP = BlockVector3.at((chunk.getX() << 4) + 15, location.getWorld().getMaxHeight(), (chunk.getZ() << 4) + 15);
                player.sendMessage(tr("\u00a77The pixies are busy changing the biome in your current chunk to \u00a79{0}\u00a77, be patient.", biome));
            } else if (args.length < 2 || args[1].equalsIgnoreCase("all")) {
                player.sendMessage(tr("\u00a77The pixies are busy changing the biome of your island to \u00a79{0}\u00a77, be patient.", biome));
            }
            Biome biomeEnum = BIOMES.get(biome);
            if (biomeEnum == null) {
                player.sendMessage(tr("\u00a7eInvalid biome {0} supplied!", biome));
                return true;
            }
            new SetBiomeTask(plugin, player.getWorld(), minP, maxP, biomeEnum, () -> {
                if (args.length == 1) {
                    island.setBiome(biome);
                    player.sendMessage(tr("\u00a7aYou have changed your island''s biome to {0}", biome.toUpperCase()));
                    island.sendMessageToIslandGroup(true, marktr("{0} changed the island biome to {1}"), player.getName(), biome.toUpperCase());
                    plugin.getCooldownHandler().resetCooldown(player, "biome", Settings.general_biomeChange);
                } else {
                    player.sendMessage(tr("\u00a7aYou have changed {0} blocks around you to the {1} biome", args[1], biome.toUpperCase()));
                    island.sendMessageToIslandGroup(true, marktr("{0} created an area with {1} biome"), player.getName(), biome.toUpperCase());
                }
            }).runTask(plugin);
        }
        return true;
    }

    public static boolean biomeExists(String biomeName) {
        if (biomeName == null) {
            return false;
        }
        return BIOMES.containsKey(biomeName.toLowerCase());
    }
}

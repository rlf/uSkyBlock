package us.talabrek.ultimateskyblock.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.util.logging.Logger;

import static us.talabrek.ultimateskyblock.util.LocationUtil.isSafeLocation;

/**
 * Responsible for world-related tasks.
 */
public class WorldLogic {
    private final uSkyBlock plugin;
    private final Logger logger;

    public WorldLogic(uSkyBlock plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void createSpawn(World world, Location worldSpawn) {
        File spawnSchematic = new File(plugin.getDataFolder() + File.separator + "schematics" + File.separator + "spawn.schematic");
        if (plugin.getConfig().getInt("options.general.spawnSize", 0) > 32 && spawnSchematic.exists()) {
            AsyncWorldEditHandler.loadIslandSchematic(spawnSchematic, worldSpawn, null);
        } else {
            Block spawnBlock = world.getBlockAt(worldSpawn).getRelative(BlockFace.DOWN);
            spawnBlock.setType(Material.GOLD_BLOCK);
            Block air1 = spawnBlock.getRelative(BlockFace.UP);
            air1.setType(Material.AIR);
            air1.getRelative(BlockFace.UP).setType(Material.AIR);
        }
    }

    public void setupWorld(World world, int island_height) {
        if (Settings.general_spawnSize > 0) {
            if (LocationUtil.isEmptyLocation(world.getSpawnLocation())) {
                world.setSpawnLocation(0, island_height, 0);
            }
            Location worldSpawn = world.getSpawnLocation();
            if (!isSafeLocation(worldSpawn)) {
                createSpawn(world, worldSpawn);
            }
        }
    }

    /**
     * Get the chunk generator for generating Skyworld-chunks.
     *
     * @return ChunkGenerator instance for normal islands world.
     */
    public ChunkGenerator getGenerator() {
        try {
            String genClass = plugin.getConfig().getString("options.advanced.chunk-generator", "us.talabrek.ultimateskyblock.world.SkyBlockChunkGenerator");
            if (genClass.equals("us.talabrek.ultimateskyblock.SkyBlockChunkGenerator")) {
                // Convert old values:
                plugin.getConfig().set("options.advanced.chunk-generator", "us.talabrek.ultimateskyblock.world.SkyBlockChunkGenerator");
                plugin.saveConfig();
            }

            Object generator = Class.forName(genClass).newInstance();
            if (generator instanceof ChunkGenerator) {
                return (ChunkGenerator) generator;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.warning("Invalid chunk-generator configured: " + e);
        }
        return new SkyBlockChunkGenerator();
    }

    /**
     * Get the chunk generator for generating Skyworld nether-chunks.
     *
     * @return ChunkGenerator instance for nether islands world.
     */
    public ChunkGenerator getNetherGenerator() {
        try {
            String genClass = plugin.getConfig().getString("nether.chunk-generator", "us.talabrek.ultimateskyblock.world.SkyBlockNetherChunkGenerator");
            if (genClass.equals("us.talabrek.ultimateskyblock.SkyBlockNetherChunkGenerator")) {
                // Convert old values:
                plugin.getConfig().set("nether.chunk-generator", "us.talabrek.ultimateskyblock.world.SkyBlockNetherChunkGenerator");
                plugin.saveConfig();
            }

            Object generator = Class.forName(genClass).newInstance();
            if (generator instanceof ChunkGenerator) {
                return (ChunkGenerator) generator;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.warning("Invalid chunk-generator configured: " + e);
        }
        return new SkyBlockNetherChunkGenerator();
    }

    /**
     * Checks if the given world is the Skyworld in use by the plugin.
     *
     * @param world World to check.
     * @return True if world is skyworld, false otherwise.
     */
    public boolean isSkyWorld(World world) {
        if (world == null) {
            return false;
        }
        return uSkyBlock.getSkyBlockWorld().getName().equalsIgnoreCase(world.getName());
    }

    /**
     * Checks if the given world is the Skynether in use by the plugin.
     *
     * @param world World to check.
     * @return True if world is skynether, false otherwise.
     */
    public boolean isSkyNether(World world) {
        World netherWorld = plugin.getSkyBlockNetherWorld();
        return world != null && netherWorld != null && world.getName().equalsIgnoreCase(netherWorld.getName());
    }

    @SuppressWarnings("unused")
    public void shutdown() {
        // Empty placeholder for now.
    }
}

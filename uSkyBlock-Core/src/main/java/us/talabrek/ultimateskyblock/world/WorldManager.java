package us.talabrek.ultimateskyblock.world;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.io.File;
import java.util.Arrays;

public class WorldManager {
    private final uSkyBlock plugin;

    public WorldManager(@NotNull uSkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the {@link ChunkRegenerator} for the given {@link World}.
     * @param world World to get the ChunkRegenerator for.
     * @return ChunkRegenerator for the given world.
     */
    @NotNull
    public ChunkRegenerator getChunkRegenerator(@NotNull World world) {
        return new ChunkRegenerator(world);
    }

    /**
     * Removes all unnamed {@link Monster}'s at the given {@link Location}.
     * @param target Location to remove unnamed monsters.
     */
    public void removeCreatures(@Nullable final Location target) {
        if (!Settings.island_removeCreaturesByTeleport || target == null || target.getWorld() == null) {
            return;
        }

        final int px = target.getBlockX();
        final int py = target.getBlockY();
        final int pz = target.getBlockZ();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                Chunk chunk = target.getWorld().getChunkAt(
                        new Location(target.getWorld(), (px + x * 16), py, (pz + z * 16)));

                Arrays.stream(chunk.getEntities())
                        .filter(entity -> entity instanceof Monster)
                        .filter(entity -> entity.getCustomName() == null)
                        .forEach(Entity::remove);
            }
        }
    }

    /**
     * Sets the spawn location for the given {@link World} if currently unset. Creates a safe spawn location if
     * necessary by calling {@link WorldManager#createSpawn(Location)}.
     * @param world World to setup.
     * @param islandHeight Height at which islands will be created.
     */
    public void setupWorld(@NotNull World world, int islandHeight) {
        Validate.notNull(world, "World cannot be null");

        if (LocationUtil.isEmptyLocation(world.getSpawnLocation())) {
            world.setSpawnLocation(0, islandHeight, 0);
        }

        Location spawnLocation = world.getSpawnLocation();
        if (!LocationUtil.isSafeLocation(spawnLocation)) {
            createSpawn(spawnLocation);
        }
    }

    /**
     * Creates the world spawn at the given {@link Location}. Places the spawn schematic if
     * configured and when it exists on disk. Places a gold block with two air above it otherwise.
     * @param spawnLocation Location to create the spawn at.
     */
    private void createSpawn(@NotNull Location spawnLocation) {
        Validate.notNull(spawnLocation, "SpawnLocation cannot be null");
        Validate.notNull(spawnLocation.getWorld(), "SpawnLocation#world cannot be null");

        File schematic = new File(plugin.getDataFolder() + File.separator + "schematics" +
                File.separator + "spawn.schem");
        World world = spawnLocation.getWorld();

        if (plugin.getConfig().getInt("options.general.spawnSize", 0) > 32 && schematic.exists()) {
            AsyncWorldEditHandler.loadIslandSchematic(schematic, spawnLocation, null);
        } else {
            Block spawnBlock = world.getBlockAt(spawnLocation).getRelative(BlockFace.DOWN);
            spawnBlock.setType(Material.GOLD_BLOCK);
            Block air = spawnBlock.getRelative(BlockFace.UP);
            air.setType(Material.AIR);
            air.getRelative(BlockFace.UP).setType(Material.AIR);
        }
    }
}

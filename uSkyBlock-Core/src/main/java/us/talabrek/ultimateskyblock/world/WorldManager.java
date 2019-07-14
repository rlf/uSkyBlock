package us.talabrek.ultimateskyblock.world;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.talabrek.ultimateskyblock.Settings;

import java.util.Arrays;

public class WorldManager {
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
}

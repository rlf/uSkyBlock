package us.talabrek.ultimateskyblock.world;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class WorldManager {
    @NotNull
    public ChunkRegenerator getChunkRegenerator(@NotNull World world) {
        return new ChunkRegenerator(world);
    }
}

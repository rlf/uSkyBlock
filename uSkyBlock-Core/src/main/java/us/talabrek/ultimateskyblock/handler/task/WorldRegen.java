package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.math.BlockVector2;
import org.bukkit.Chunk;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Incremental runnable for regenerating chunks.
 */
public class WorldRegen extends IncrementalRunnable {
    private final uSkyBlock plugin;
    private final World world;
    private final List<BlockVector2> chunks;

    public WorldRegen(uSkyBlock plugin, World world, Set<BlockVector2> chunks, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.plugin = plugin;
        this.world = world;
        this.chunks = new ArrayList<>(chunks);
        plugin.getLogger().log(Level.FINE, "Planning regen of chunks: " + chunks);
    }

    @Override
    protected boolean execute() {
        while (!chunks.isEmpty()) {
            BlockVector2 chunk = chunks.remove(0);
            try {
                Chunk bukkitChunk = world.getChunkAt(chunk.getBlockX(), chunk.getBlockZ());
                plugin.getWorldManager().getChunkRegenerator(world).regenerateChunk(bukkitChunk);
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Exception trying to regenerate chunk " + chunk, e);
                chunks.add(chunk);
                break;
            }
            if (!tick()) {
                break;
            }
        }
        return chunks.isEmpty();
    }
}

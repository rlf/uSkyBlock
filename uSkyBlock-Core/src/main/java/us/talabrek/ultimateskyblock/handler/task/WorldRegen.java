package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.Vector2D;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Incremental runnable for regenerating chunks.
 */
public class WorldRegen extends IncrementalRunnable {
    private static final Logger log = Logger.getLogger(WorldRegen.class.getName());
    private final World world;
    private final List<Vector2D> chunks;

    public WorldRegen(uSkyBlock plugin, World world, Set<Vector2D> chunks, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.world = world;
        this.chunks = new ArrayList<>(chunks);
        log.log(Level.FINE, "Planning regen of chunks: " + chunks);
    }

    @Override
    protected boolean execute() {
        while (!chunks.isEmpty()) {
            Vector2D chunk = chunks.remove(0);
            world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
            if (!tick()) {
                break;
            }
        }
        return chunks.isEmpty();
    }
}

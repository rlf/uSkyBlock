package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.Vector2D;
import org.bukkit.World;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LogUtil;

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
    private boolean firstRun = true;

    public WorldRegen(uSkyBlock plugin, World world, Set<Vector2D> chunks, Runnable onCompletion) {
        super(plugin, onCompletion);
        this.world = world;
        this.chunks = new ArrayList<>(chunks);
        log.log(Level.FINE, "Planning regen of chunks: " + chunks);
    }

    @Override
    protected boolean execute() {
        if (firstRun) {
            firstRun = false;
            chunks.stream().forEach(c -> world.unloadChunk(c.getBlockX(), c.getBlockZ(), false));
        }
        while (!chunks.isEmpty()) {
            Vector2D chunk = chunks.remove(0);
            try {
                if (!world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ())) {
                    LogUtil.log(Level.WARNING, "Unable to regenerate chunk " + chunk);
                    chunks.add(chunk);
                    return false; // yield
                }
            } catch (Exception e) {
                LogUtil.log(Level.WARNING, "Exception trying to regenerate chunk " + chunk, e);
                return false; // yield
            }
            if (!tick()) {
                break;
            }
        }
        return chunks.isEmpty();
    }
}

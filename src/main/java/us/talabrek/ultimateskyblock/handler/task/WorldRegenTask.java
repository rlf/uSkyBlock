package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.Vector2D;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * A task that regenerates a predefined number of chunks.
 */
public class WorldRegenTask implements IncrementalTask {
    private final World world;
    private final List<Vector2D> chunks;
    private int index = 0;

    public WorldRegenTask(World world, Set<Vector2D> chunks) {
        this.world = world;
        this.chunks = new ArrayList<>(chunks);
        log.log(Level.FINE, "Planning regen of chunks: " + chunks);
    }

    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        log.log(Level.FINE, "Executing WorldRegen of chunks " + offset + "-" + (offset+length) + " of " + chunks.size() + " chunks");
        this.index = offset + length;
        for (int i = offset; i < (offset+length) && i < chunks.size(); i++) {
            Vector2D chunk = chunks.get(i);
            world.regenerateChunk(chunk.getBlockX(), chunk.getBlockZ());
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return chunks.size();
    }

    @Override
    public boolean isComplete() {
        return index >= (chunks.size()-1);
    }
}

package us.talabrek.ultimateskyblock.island.task;

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Incremental task for snapshotting chunks.
 */
public class ChunkSnapShotTask extends IncrementalRunnable {
    private final Location location;
    private final List<Vector2D> chunks;
    private List<ChunkSnapshot> snapshots = new ArrayList<>();

    public ChunkSnapShotTask(uSkyBlock plugin, Location location, ProtectedRegion region, final Callback<List<ChunkSnapshot>> callback) {
        super(plugin, callback);
        this.location = location;
        if (region != null) {
            chunks = new ArrayList<>(WorldEditHandler.getChunks(new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint())));
        } else {
            chunks = new ArrayList<>();
        }
        callback.setState(snapshots);
    }

    @Override
    protected boolean execute() {
        while (!chunks.isEmpty()) {
            Vector2D chunkVector = chunks.remove(0);
            Chunk chunk = location.getWorld().getChunkAt(chunkVector.getBlockX(), chunkVector.getBlockZ());
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            snapshots.add(chunk.getChunkSnapshot(false, false, false));
            if (!tick()) {
                break;
            }
        }
        return chunks.isEmpty();
    }

}

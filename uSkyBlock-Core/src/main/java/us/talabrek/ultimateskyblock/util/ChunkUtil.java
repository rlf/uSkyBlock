package us.talabrek.ultimateskyblock.util;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for snapshotting and similar of chunks
 */
public enum ChunkUtil {;
    public static Chunks getSnapshots(Location loc, int radius) {
        if (loc == null) {
            return null;
        }
        int cx = loc.getBlockX() >> 4;
        int cz = loc.getBlockZ() >> 4;
        Map<String,ChunkSnapshot> snapshots = new HashMap<>();
        for (int x = cx - radius; x <= cx + radius; ++x) {
            for (int z = cz - radius; z <= cz + radius; ++z) {
                ChunkSnapshot snapshot = loc.getWorld().getChunkAt(x, z).getChunkSnapshot(false, false, false);
                snapshots.put("" + x + "," + z, snapshot);
            }
        }
        return new Chunks(snapshots);
    }

    public static Chunks getSnapshots4x4(Location loc) {
        if (loc == null) {
            return null;
        }
        int cx = loc.getBlockX() >> 4;
        int cz = loc.getBlockZ() >> 4;
        Map<String,ChunkSnapshot> snapshots = new HashMap<>();
        snapshots.put("" + cx +"," + cz, loc.getWorld().getChunkAt(cx, cz).getChunkSnapshot(false, false, false));
        snapshots.put("" + (cx-1) +"," + cz, loc.getWorld().getChunkAt(cx-1, cz).getChunkSnapshot(false, false, false));
        snapshots.put("" + cx +"," + (cz-1), loc.getWorld().getChunkAt(cx, cz-1).getChunkSnapshot(false, false, false));
        snapshots.put("" + (cx-1) +"," + (cz-1), loc.getWorld().getChunkAt(cx-1, cz-1).getChunkSnapshot(false, false, false));
        return new Chunks(snapshots);
    }

    public static final class Chunks {
        private final Map<String, ChunkSnapshot> snapshots;

        private Chunks(Map<String, ChunkSnapshot> snapshots) {
            this.snapshots = snapshots;
        }

        public ChunkSnapshot getChunkAt(int cx, int cz) {
            return snapshots.get("" + cx + "," + cz);
        }

        public Material getBlockTypeAt(int x, int y, int z) {
            ChunkSnapshot chunkAt = getChunkAt(x >> 4, z >> 4);
            if (chunkAt != null) {
                int cx = x & 0xF;
                int cz = z & 0xF;
                return Material.getMaterial(chunkAt.getBlockTypeId(cx, y, cz));
            }
            return null;
        }
    }
}

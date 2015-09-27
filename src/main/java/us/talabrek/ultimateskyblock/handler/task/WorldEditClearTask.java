package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.TimeUtil;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/**
 * A task that chunks up the clearing of a region.
 * Not as fast as WorldEditRegenTask, but more versatile.
 */
public class WorldEditClearTask extends BukkitRunnable implements IncrementalTask {
    private static final BaseBlock AIR = new BaseBlock(0);

    private final Set<Region> borderRegions;
    private final Set<Vector2D> innerChunks;
    private final int chunks;
    private final uSkyBlock plugin;
    private final CommandSender commandSender;
    private final Region region;
    private final String format;
    private final BukkitWorld bukkitWorld;
    private final int minY;
    private final int maxY;
    private final int maxBlocks;

    public WorldEditClearTask(uSkyBlock plugin, CommandSender commandSender, Region region, String format) {
        this.plugin = plugin;
        this.commandSender = commandSender;
        this.region = region;
        this.format = format;
        innerChunks = WorldEditHandler.getInnerChunks(region);
        borderRegions = WorldEditHandler.getBorderRegions(region);
        chunks = innerChunks.size() + borderRegions.size();
        bukkitWorld = new BukkitWorld(plugin.getWorld());
        minY = Math.min(region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());
        maxY = Math.max(region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());
        maxBlocks = 2*Math.max(region.getLength(), region.getWidth())*16*(maxY-minY);
    }

    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        Iterator<Vector2D> inner = innerChunks.iterator();
        Iterator<Region> border = borderRegions.iterator();
        for (int i = 0; i < length; i++) {
            EditSession editSession = AsyncWorldEditHandler.createEditSession(bukkitWorld, maxBlocks);
            editSession.setFastMode(true);
            editSession.enableQueue();
            if (inner.hasNext()) {
                Vector2D chunk = inner.next();
                inner.remove();
                try {
                    int x = chunk.getBlockX() << 4;
                    int z = chunk.getBlockZ() << 4;
                    editSession.setBlocks(new CuboidRegion(bukkitWorld,
                                    new BlockVector(x, minY, z),
                                    new BlockVector(x + 15, maxY, z + 15)),
                            AIR);
                } catch (MaxChangedBlocksException e) {
                    plugin.getLogger().log(Level.WARNING, "Unable to clear flat-land", e);
                }
            } else if (border.hasNext()) {
                Region borderRegion = border.next();
                border.remove();
                try {
                    editSession.setBlocks(borderRegion, AIR);
                } catch (MaxChangedBlocksException e) {
                    plugin.getLogger().log(Level.WARNING, "Unable to clear flat-land", e);
                }
            }
            editSession.flushQueue();
        }
        return isComplete();
    }

    @Override
    public int getLength() {
        return chunks;
    }

    @Override
    public boolean isComplete() {
        return innerChunks.isEmpty() && borderRegions.isEmpty();
    }

    @Override
    public void run() {
        final long tStart = System.currentTimeMillis();
        plugin.getExecutor().execute(plugin, this, new Runnable() {
            @Override
            public void run() {
                String duration = TimeUtil.millisAsString(System.currentTimeMillis() - tStart);
                plugin.log(Level.INFO, String.format("Region %s was cleared in %s", region.toString(), duration));
                commandSender.sendMessage(String.format(format, duration));
            }
        }, 0.5f, 1);
    }
}

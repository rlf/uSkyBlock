package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import dk.lockfuglsang.minecraft.util.TimeUtil;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.async.IncrementalRunnable;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.handler.WorldEditHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import static us.talabrek.ultimateskyblock.util.LogUtil.log;

/**
 * A task that chunks up the clearing of a region.
 * Not as fast as WorldEditRegenTask, but more versatile.
 */
public class WorldEditClearFlatlandTask extends IncrementalRunnable {
   private static final BlockState AIR = BlockTypes.AIR.getDefaultState();

    private final Set<Region> borderRegions;
    private final Set<Vector2D> innerChunks;
    private final uSkyBlock plugin;
    private final BukkitWorld bukkitWorld;
    private final int minY;
    private final int maxY;
    private final int maxBlocks;

    public WorldEditClearFlatlandTask(final uSkyBlock plugin, final CommandSender commandSender, final Region region, final String format) {
        super(plugin);
        setOnCompletion(new Runnable() {
            @Override
            public void run() {
                String duration = TimeUtil.millisAsString(WorldEditClearFlatlandTask.this.getTimeElapsed());
                log(Level.INFO, String.format("Region %s was cleared in %s", region.toString(), duration));
                commandSender.sendMessage(String.format(format, duration));
            }
        });
        this.plugin = plugin;
        innerChunks = WorldEditHandler.getInnerChunks(region);
        borderRegions = WorldEditHandler.getBorderRegions(region);
        bukkitWorld = new BukkitWorld(plugin.getWorld());
        minY = Math.min(region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());
        maxY = Math.max(region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());
        maxBlocks = 2*Math.max(region.getLength(), region.getWidth())*16*(maxY-minY);
    }

    @Override
    public boolean execute() {
        Iterator<Vector2D> inner = innerChunks.iterator();
        Iterator<Region> border = borderRegions.iterator();
        while (!isComplete()) {
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
            if (!tick()) {
                break;
            }
        }
        return isComplete();
    }

    public boolean isComplete() {
        return innerChunks.isEmpty() && borderRegions.isEmpty();
    }

}

package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.async.IncrementalTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Do WorldEdit stuff in increments
 */
public class WorldEditRegenTask implements IncrementalTask {
    private static final BaseBlock AIR = new BaseBlock(0);
    private static final BaseBlock GLASS = new BaseBlock(20);
    private final EditSession editSession;
    private final BukkitWorld bukkitWorld;
    private final List<Region> regions;
    private final int size;
    private int index = 0;

    public WorldEditRegenTask(World world, Set<Region> borderRegions) {
        bukkitWorld = new BukkitWorld(world);
        editSession = new EditSession(bukkitWorld, 255 * Settings.island_protectionRange * Settings.island_protectionRange);
        editSession.enableQueue();
        regions = new ArrayList<>(borderRegions);
        size = regions.size();
        log.log(Level.FINE, "Planning regen of regions: " + regions);
    }
    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        log.log(Level.FINE, "Executing WorldEditRegen of regions " + offset + "-" + (offset+length) + " of " + regions.size() + " regions");
        for (int i = 0; i < length && !regions.isEmpty(); i++) {
            Region region = regions.remove(0);
            editSession.enableQueue();
            /*
            try {
                editSession.setBlocks(region, GLASS);
                editSession.flushQueue();
            } catch (MaxChangedBlocksException e) {
                plugin.getLogger().log(Level.WARNING, "Unable to clear region " + region);
            }
            */
            bukkitWorld.regenerate(region, editSession);
            editSession.flushQueue();
        }
        this.index = offset + length;
        boolean complete = isComplete();
        if (complete) {
            editSession.flushQueue();
            editSession.commit(); // is this the heavy part?
        }
        return complete;
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return regions.isEmpty();
    }
}

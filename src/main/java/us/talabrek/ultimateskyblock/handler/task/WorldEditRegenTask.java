package us.talabrek.ultimateskyblock.handler.task;

import com.sk89q.worldedit.EditSession;
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

    private final EditSession editSession;
    private final BukkitWorld bukkitWorld;
    private final List<Region> regions;
    private int index = 0;

    public WorldEditRegenTask(World world, Set<Region> borderRegions) {
        bukkitWorld = new BukkitWorld(world);
        editSession = new EditSession(bukkitWorld, 255 * Settings.island_protectionRange * Settings.island_protectionRange);
        editSession.enableQueue();
        regions = new ArrayList<>(borderRegions);
        log.log(Level.FINE, "Planning regen of regions: " + regions);
    }
    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        log.log(Level.FINE, "Executing WorldEditRegen of regions " + offset + "-" + (offset+length) + " of " + regions.size() + " regions");
        for (int i = offset; i < offset+length && i < getLength(); i++) {
            Region region = regions.get(i);
            editSession.enableQueue();
            bukkitWorld.regenerate(region, editSession);
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
        return regions.size();
    }

    @Override
    public boolean isComplete() {
        return index >= (regions.size()-1);
    }
}

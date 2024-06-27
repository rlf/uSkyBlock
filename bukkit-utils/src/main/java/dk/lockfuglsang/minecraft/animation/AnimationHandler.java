package dk.lockfuglsang.minecraft.animation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles particles and per-player block-animations
 */
public class AnimationHandler {
    private final Map<UUID, Set<Animation>> animations = new ConcurrentHashMap<>();
    private final Map<UUID, AnimationTask> animationTasks = new ConcurrentHashMap<>();
    private final Plugin plugin;

    private int animTick;

    public AnimationHandler(Plugin plugin) {
        this.plugin = plugin;
        animTick = plugin.getConfig().getInt("animations.tick", 20);
    }

    public void setAnimTick(int animTick) {
        this.animTick = animTick;
        plugin.getConfig().set("animations.tick", animTick);
    }

    public synchronized void addAnimation(Animation animation) {
        if (!animations.containsKey(animation.getPlayer().getUniqueId())) {
            animations.put(animation.getPlayer().getUniqueId(), new HashSet<Animation>());
        }
        animations.get(animation.getPlayer().getUniqueId()).add(animation);
        start();
    }

    public synchronized boolean removeAnimations(Player player) {
        Set<Animation> animSet = animations.remove(player.getUniqueId());
        if (animSet == null) {
            return false;
        }
        for (Animation animation : animSet) {
            animation.hide();
        }
        return true;
    }

    public synchronized void start() {
        for (UUID uuid : animations.keySet()) {
            AnimationTask animationTask = animationTasks.get(uuid);
            if (animationTask == null && animations.get(uuid) != null && !animations.get(uuid).isEmpty()) {
                animationTask = new AnimationTask(uuid);
                animationTask.runTaskTimerAsynchronously(plugin, 0, animTick);
                animationTasks.put(uuid, animationTask);
            }
        }
    }

    public synchronized void stop() {
        for (UUID uuid :  animations.keySet()) {
            AnimationTask animationTask = animationTasks.get(uuid);
            if (animationTask != null) {
                animationTask.cancel();
                animationTasks.remove(uuid);
            }
        }
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    Collection<Set<Animation>> anims = new ArrayList<>(animations.values());
                    for (Set<Animation> animSet : anims) {
                        for (Animation animation : animSet) {
                            animation.hide();
                        }
                    }
                }
            });
        }
    }

    private class AnimationTask extends BukkitRunnable {
        private final UUID uniqueId;
        public AnimationTask(UUID uniqueId) {
            this.uniqueId = uniqueId;
        }

        @Override
        public void run() {
            // Copy - to avoid ConcurrentModificationException
            Set<Animation> animSet = animations.get(uniqueId);
            Set<Animation> animCopy = (animSet != null) ? new HashSet<>(animSet) : Collections.<Animation>emptySet();
            for (Animation animation : animCopy) {
                if (!animation.show()) {
                    UUID uuid = animation.getPlayer().getUniqueId();
                    animations.get(uuid).remove(animation);
                    if (animations.get(uuid).isEmpty()) {
                        animations.remove(uuid);
                    }
                }
            }
            if (animations.get(uniqueId) == null) {
                cancel();
                animationTasks.remove(uniqueId);
            }
        }
    }
}

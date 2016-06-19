package dk.lockfuglsang.minecraft.animation;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
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
    private final Plugin plugin;
    private AnimationTask animationTask;

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
        if (animationTask == null && !animations.isEmpty()) {
            animationTask = new AnimationTask();
            animationTask.runTaskTimerAsynchronously(plugin, 0, animTick);
        }
    }

    public synchronized void stop() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        Collection<Set<Animation>> anims = animations.values();
        for (Set<Animation> animSet : anims) {
            for (Animation animation : animSet) {
                animation.hide();
            }
        }
    }

    private class AnimationTask extends BukkitRunnable {
        @Override
        public void run() {
            Collection<Set<Animation>> anims = animations.values();
            for (Set<Animation> animSet : anims) {
                for (Animation animation : animSet) {
                    if (!animation.show()) {
                        UUID uuid = animation.getPlayer().getUniqueId();
                        animations.get(uuid).remove(animation);
                        if (animations.get(uuid).isEmpty()) {
                            animations.remove(uuid);
                        }
                    }
                }
            }
        }
    }
}

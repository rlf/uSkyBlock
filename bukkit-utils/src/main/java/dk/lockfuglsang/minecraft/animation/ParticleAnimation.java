package dk.lockfuglsang.minecraft.animation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * An animation using particles (requires refreshes).
 */
public class ParticleAnimation implements Animation {
    private final Player player;
    private final Particle particle;
    private final List<Location> points;
    private final int animCount;

    public ParticleAnimation(Player player, List<Location> points, Particle particle, int animCount) {
        this.player = player;
        this.particle = particle;
        this.points = points;
        this.animCount = animCount;
    }

    @Override
    public boolean show() {
        if (!player.isOnline()) {
            return false;
        }
        for (Location loc : points) {
            if (!PlayerHandler.spawnParticle(player, particle, loc, animCount)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hide() {
        return true;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}

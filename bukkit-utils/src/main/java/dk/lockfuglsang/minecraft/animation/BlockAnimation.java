package dk.lockfuglsang.minecraft.animation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Sends (bogus) block-info to the player
 */
public class BlockAnimation implements Animation {
    private final Player player;
    private final List<Location> points;
    private final Material material;
    private final byte data;
    private volatile boolean shown;

    public BlockAnimation(Player player, List<Location> points, Material material, byte data) {
        this.player = player;
        this.points = points;
        this.material = material;
        this.data = data;
        shown = false;
    }

    @Override
    public boolean show() {
        if (shown) {
            return true;
        }
        if (!player.isOnline()) {
            return false;
        }
        for (Location loc : points) {
            if (!PlayerHandler.sendBlockChange(player, loc, material, data)) {
                return false;
            }
        }
        shown = true;
        return true;
    }

    @Override
    public boolean hide() {
        try {
            if (shown) {
                for (Location loc : points) {
                    if (!PlayerHandler.sendBlockChange(player, loc, loc.getBlock().getType(), loc.getBlock().getData())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } finally {
            shown = false;
        }
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}

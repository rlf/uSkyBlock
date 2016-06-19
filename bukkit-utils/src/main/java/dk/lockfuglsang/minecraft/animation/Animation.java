package dk.lockfuglsang.minecraft.animation;

import org.bukkit.entity.Player;

/**
 * Common interface for animations
 */
public interface Animation {
    boolean show();

    boolean hide();

    Player getPlayer();
}

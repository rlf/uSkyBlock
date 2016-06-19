package dk.lockfuglsang.minecraft.animation;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Common handler across servers for sending particles and other packages to a player.
 */
public enum PlayerHandler {;
    private static final Logger log = Logger.getLogger(PlayerHandler.class.getName());

    public static boolean spawnParticle(Player player, Particle particle, Location loc, int count) {
        try {
            Method playMethod = getMethod(player, "spawnParticle", new Class<?>[]{Particle.class, Location.class, Integer.TYPE});
            if (playMethod != null) {
                playMethod.invoke(player, particle, loc, count);
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.finest("Unable to spawnParticle for player " + player + ": " + e);
        }
        return false;
    }

    public static boolean sendBlockChange(Player player, Location location, Material material, byte data) {
        try {
            Method method = getMethod(player, "sendBlockChange", new Class<?>[]{Location.class, Material.class, Byte.TYPE});
            if (method != null) {
                method.invoke(player, location, material, data);
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.finest("Unable to sendBlockChange to player " + player + ": " + e);
        }
        return false;
    }

    public static boolean playEffect(Player player, Location loc, Effect effect, int data) {
        try {
            Method playMethod = getMethod(player, "playEffect", new Class<?>[]{Location.class, Effect.class, Integer.TYPE});
            if (playMethod != null) {
                playMethod.invoke(player, loc, effect, data);
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.finest("Unable to playEffect for player " + player + ": " + e);
        }
        return false;
    }

    private static Method getMethod(Object player, String methodName, Class<?>[] paramClasses) throws NoSuchMethodException {
        return player.getClass().getMethod(methodName, paramClasses);
    }
}

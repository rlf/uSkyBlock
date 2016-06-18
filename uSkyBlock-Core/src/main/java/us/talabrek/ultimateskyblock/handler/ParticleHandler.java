package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Common handler across servers for sending particles to a player.
 */
public enum ParticleHandler {;
    private static final Logger log = Logger.getLogger(ParticleHandler.class.getName());

    public static boolean spawnParticle(Player player, Particle particle, Location loc, int count) {
        try {
            Method playMethod = getMethod(player, "spawnParticle", new Class<?>[]{Particle.class, Location.class, Integer.TYPE});
            if (playMethod != null) {
                playMethod.invoke(player, particle, loc, count);
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.finest("Unable to playEffect for player " + player + ": " + e);
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

package dk.lockfuglsang.minecraft.reflection;

import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper methods that allow accesss to reflection for backward compatible code.
 * @since 2.7.2
 */
public class ReflectionUtil {
    private static final Logger log = Logger.getLogger(ReflectionUtil.class.getName());

    /**
     * Returns the current version of the Bukkit implementation
     * @return the current version of the Bukkit implementation
     * @since 2.7.2
     */
    public static String getCraftBukkitVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    /**
     * Returns the current version of the net.minecraft.server implementation
     * @param nmsObject A native object from nms namespace
     * @return the current version of the net.minecraft.server implementation
     * @since 2.7.2
     */
    public static String getNMSVersion(Object nmsObject) {
        return nmsObject != null ? nmsObject.getClass().getPackage().getName().split("\\.")[3] : "";
    }

    /**
     * Returns the packagename of the given object.
     * @param nmsObject An object
     * @return the packagename of the given object.
     * @since 2.7.2
     */
    public static String getPackageName(Object nmsObject) {
        return nmsObject != null ? nmsObject.getClass().getPackage().getName() : "";
    }

    /**
     * Returns the corresponding Bukkit class, given a CraftBukkit implementation object.
     * @param craftObject A CraftBukkit implementation of a Bukkit class.
     * @return the corresponding Bukkit class, given a CraftBukkit implementation object.
     * @since 2.7.2
     */
    public static Class<?> getBukkitClass(Object craftObject) {
        Class clazz = craftObject != null ? craftObject.getClass() : null;
        while (clazz != null && clazz.getCanonicalName().contains(".craftbukkit.")) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }

    /**
     * Uses reflection to execute the named method on the supplied class giving the arguments.
     * Sinks all exceptions, but log an entry and returns <code>null</code>
     * @param clazz         The class on which to invoke the method
     * @param methodName    The name of the method to invoke
     * @param args          The arguments to supply to the method
     * @return <code>null</code> or the return-object from the method.
     * @since 2.7.2
     */
    public static <T> T execStatic(Class<?> clazz, String methodName, Object... args) {
        try {
            Class[] argTypes = new Class[args.length];
            int ix = 0;
            for (Object arg : args) {
                argTypes[ix++] = getBukkitClass(arg);
            }
            Method method = clazz.getDeclaredMethod(methodName, argTypes);
            boolean wasAccessible = method.isAccessible();
            method.setAccessible(true);
            try {
                return (T) method.invoke(null, args);
            } finally {
                method.setAccessible(wasAccessible);
            }
        } catch (NoSuchMethodException e) {
            log.info("Unable to locate method " + methodName + " on " + clazz);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.log(Level.INFO, "Calling " + methodName + " on " + clazz + " threw an exception", e);
        }
        return null;
    }

    /**
     * Uses reflection to execute the named method on the supplied class giving the arguments.
     * Sinks all exceptions, but log an entry and returns <code>null</code>
     * @param obj           The object on which to invoke the method
     * @param methodName    The name of the method to invoke
     * @param argTypes      An array of argument-types (classes).
     * @param args          The arguments to supply to the method
     * @return <code>null</code> or the return-object from the method.
     * @since 2.7.2
     */
    public static <T> T exec(Object obj, String methodName, Class[] argTypes, Object... args) {
        if (obj == null) {
            return null;
        }
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, argTypes);
            boolean wasAccessible = method.isAccessible();
            method.setAccessible(true);
            try {
                return (T) method.invoke(obj, args);
            } finally {
                method.setAccessible(wasAccessible);
            }
        } catch (NoSuchMethodException e) {
            log.info("Unable to locate method " + methodName + "(" + Arrays.asList(argTypes) + ") on " + obj.getClass());
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.log(Level.INFO, "Calling " + methodName + " on " + obj + " threw an exception", e);
        }
        return null;
    }

    /**
     * Uses reflection to execute the named method on the supplied class giving the arguments.
     * Sinks all exceptions, but log an entry and returns <code>null</code>
     * @param obj           The object on which to invoke the method
     * @param methodName    The name of the method to invoke
     * @param args          The arguments to supply to the method
     * @return <code>null</code> or the return-object from the method.
     * @since 2.7.2
     */
    public static <T> T exec(Object obj, String methodName, Object... args) {
        if (obj == null) {
            return null;
        }
        Class[] argTypes = new Class[args.length];
        int ix = 0;
        for (Object arg : args) {
            argTypes[ix++] = arg != null ? arg.getClass() : null;
        }
        return exec(obj, methodName, argTypes, args);
    }
}

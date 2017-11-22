package dk.lockfuglsang.minecraft.reflection;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper methods that allow accesss to reflection for backward compatible code.
 *
 * @since 1.8
 */
public class ReflectionUtil {
    private static final Logger log = Logger.getLogger(ReflectionUtil.class.getName());

    /**
     * Returns the current version of the Bukkit implementation
     *
     * @return the current version of the Bukkit implementation
     * @since 1.8
     */
    public static String getCraftBukkitVersion() {
        return cb().split("\\.")[3];
    }

    /**
     * Returns the current version of the net.minecraft.server implementation
     *
     * @param nmsObject A native object from nms namespace
     * @return the current version of the net.minecraft.server implementation
     * @since 1.8
     */
    public static String getNMSVersion(Object nmsObject) {
        return nmsObject != null ? nmsObject.getClass().getPackage().getName().split("\\.")[3] : "";
    }

    /**
     * Returns the NMS version.
     * @return the NMS version (i.e. "v1_10").
     * @since 1.9
     */
    public static String getNMSVersion() {
        return nms().split("\\.")[3];
    }

    /**
     * Returns the real packagename for the net.minecraft.server.
     * @return the real packagename for the net.minecraft.server.
     * @since 1.9
     */
    public static String nms() {
        Object nmsServer = exec(Bukkit.getServer(), "getServer");
        return nmsServer != null ? nmsServer.getClass().getPackage().getName() : "net.minecraft.server";
    }

    /**
     * Returns the real packagename for the org.bukkit.craftbukkit package
     * @return the real packagename for the org.bukkit.craftbukkit package
     * @since 1.9
     */
    public static String cb() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    /**
     * Returns the packagename of the given object.
     *
     * @param nmsObject An object
     * @return the packagename of the given object.
     * @since 1.8
     */
    public static String getPackageName(Object nmsObject) {
        return nmsObject != null ? nmsObject.getClass().getPackage().getName() : "";
    }

    /**
     * Returns the corresponding Bukkit class, given a CraftBukkit implementation object.
     *
     * @param craftObject A CraftBukkit implementation of a Bukkit class.
     * @return the corresponding Bukkit class, given a CraftBukkit implementation object.
     * @since 1.8
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
     *
     * @param clazz      The class on which to invoke the method
     * @param methodName The name of the method to invoke
     * @param args       The arguments to supply to the method
     * @return <code>null</code> or the return-object from the method.
     * @since 1.8
     */
    public static <T> T execStatic(Class<?> clazz, String methodName, Object... args) {
        try {
            Class[] argTypes = new Class[args.length];
            int ix = 0;
            for (Object arg : args) {
                argTypes[ix++] = getBukkitClass(arg);
            }
            Method method = getMethod(clazz, methodName, argTypes);
            boolean wasAccessible = method.isAccessible();
            method.setAccessible(true);
            try {
                return (T) method.invoke(null, args);
            } finally {
                method.setAccessible(wasAccessible);
            }
        } catch (NoSuchMethodException e) {
            log.fine("Unable to locate method " + methodName + " on " + clazz);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.log(Level.INFO, "Calling " + methodName + " on " + clazz + " threw an exception", e);
        }
        return null;
    }

    /**
     * Uses reflection to execute the named method on the supplied class giving the arguments.
     * Sinks all exceptions, but log an entry and returns <code>null</code>
     *
     * @param obj        The object on which to invoke the method
     * @param methodName The name of the method to invoke
     * @param argTypes   An array of argument-types (classes).
     * @param args       The arguments to supply to the method
     * @return <code>null</code> or the return-object from the method.
     * @since 1.8
     */
    public static <T> T exec(Object obj, String methodName, Class[] argTypes, Object... args) {
        if (obj == null) {
            return null;
        }
        Class<?> aClass = obj.getClass();
        try {
            Method method = getMethod(aClass, methodName, argTypes);
            boolean wasAccessible = method.isAccessible();
            method.setAccessible(true);
            try {
                return (T) method.invoke(obj, args);
            } finally {
                method.setAccessible(wasAccessible);
            }
        } catch (NoSuchMethodException | AbstractMethodError e) {
            log.fine("Unable to locate method " + methodName + "(" + Arrays.asList(argTypes) + ") on " + aClass);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.log(Level.INFO, "Calling " + methodName + " on " + obj + " threw an exception", e);
        }
        return null;
    }

    private static Method getMethod(Class<?> aClass, String methodName, Class[] argTypes) throws NoSuchMethodException {
        try {
            // Declared gives access to non-public
            return aClass.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            return aClass.getMethod(methodName, argTypes);
        }
    }

    /**
     * Uses reflection to execute the named method on the supplied class giving the arguments.
     * Sinks all exceptions, but log an entry and returns <code>null</code>
     *
     * @param obj        The object on which to invoke the method
     * @param methodName The name of the method to invoke
     * @param args       The arguments to supply to the method
     * @return <code>null</code> or the return-object from the method.
     * @since 1.8
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

    /**
     * Returns the value of a field on the object.
     * @param obj           The object
     * @param fieldName     The name of the field
     * @param <T>           The type of field
     * @return the value or <code>null</code>
     * @since 1.9
     */
    public static <T> T getField(Object obj, String fieldName) {
        try {
            Field field = getFieldInternal(obj, fieldName);
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            try {
                return (T) field.get(obj);
            } finally {
                field.setAccessible(wasAccessible);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.fine("Unable to find field " + fieldName + " on " + obj);
        }
        return null;
    }

    private static Field getFieldInternal(Object obj, String fieldName) throws NoSuchFieldException {
        return getFieldFromClass(obj.getClass(), fieldName);
    }

    private static Field getFieldFromClass(Class<?> aClass, String fieldName) throws NoSuchFieldException {
        if (aClass == null) {
            throw new NoSuchFieldException("Unable to locate field " + fieldName);
        }
        try {
            return aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Ignored
        }
        try {
            return aClass.getField(fieldName);
        } catch (NoSuchFieldException e) {
            // Ignore
        }
        return getFieldFromClass(aClass.getSuperclass(), fieldName);
    }

    /**
     * Sets the value of a field on the object.
     * @param obj           The object
     * @param fieldName     The name of the field
     * @param field         The value to set
     * @param <T>           The type of field
     * @since 1.9
     */
    public static <T> void setField(Object obj, String fieldName, T field) {
        try {
            Field declaredField = getFieldInternal(obj, fieldName);
            boolean wasAccessible = declaredField.isAccessible();
            declaredField.setAccessible(true);
            try {
                declaredField.set(obj, field);
            } finally {
                declaredField.setAccessible(wasAccessible);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.fine("Unable to find field " + fieldName + " on " + obj);
        }
    }

    /**
     * Instantiates an object.
     * @param className The name of the class
     * @param argTypes  An array of argument-types
     * @param args      An array of arguments
     * @param <T>       Return-type
     * @return the object, or <code>null</code>.
     * @since 1.9
     */
    public static <T> T newInstance(String className, Class<?>[] argTypes, Object... args) {
        try {
            Class<?> aClass = Class.forName(className);
            Constructor<?> constructor = aClass.getDeclaredConstructor(argTypes);
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.fine("Unable to instantiate object of type " + className + ":" + e);
        }
        return null;
    }

    /**
     * Instantiates an object.
     * @param className The name of the class
     * @param args      An array of arguments
     * @param <T>       Return-type
     * @return the object, or <code>null</code>.
     * @since 1.9
     */
    public static <T> T newInstance(String className, Object... args) {
        Class[] argTypes = new Class[args.length];
        int ix = 0;
        for (Object arg : args) {
            argTypes[ix++] = arg != null ? arg.getClass() : null;
        }
        return newInstance(className, argTypes, args);
    }
}

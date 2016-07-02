package dk.lockfuglsang.minecraft.nbt;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for setting NBTTag data on Bukkit items without NMS (using reflection).
 * @since 1.7
 */
public enum NBTUtil { ;
    private static final Logger log = Logger.getLogger(NBTUtil.class.getName());

    /**
     * Returns the NBTTag of the <code>itemStack</code> as a string, or the empty-string if none was found.
     * @param itemStack A Bukkit ItemStack
     * @return the NBTTag
     * @since 1.7
     */
    public static String getNBTTag(ItemStack itemStack) {
        if (itemStack == null) {
            return "";
        }
        Object nmsItem = execStatic(getCraftItemStackClass(), "asNMSCopy", itemStack);
        Object nbtTag = exec(nmsItem, "getTag");
        return nbtTag != null ? "" + nbtTag : "";
    }

    /**
     * Returns a copy of the <code>itemStack</code> with the supplied <code>nbtTagString</code> applied.
     * @param itemStack     A Bukkit ItemStack
     * @param nbtTagString  A valid NBTTag string
     * @return a copy of the <code>itemStack</code>
     * @since 1.7
     */
    public static ItemStack setNBTTag(ItemStack itemStack, String nbtTagString) {
        if (itemStack == null || nbtTagString == null || nbtTagString.isEmpty()) {
            return itemStack;
        }
        Object nmsItem = execStatic(getCraftItemStackClass(), "asNMSCopy", itemStack);
        Object nbtTag = execStatic(getNBTTagParser(nmsItem), "parse", nbtTagString);
        exec(nmsItem, "setTag", nbtTag);
        Object item = execStatic(getCraftItemStackClass(), "asBukkitCopy", nmsItem);
        if (item instanceof ItemStack) {
            return (ItemStack) item;
        }
        return itemStack;
    }

    /**
     * Returns a copy of the <code>itemStack</code> with the supplied <code>nbtTagString</code> applied.
     * @param itemStack     A Bukkit ItemStack
     * @param nbtTagString  A valid NBTTag string
     * @return a copy of the <code>itemStack</code>
     * @since 1.7
     */
    public static ItemStack addNBTTag(ItemStack itemStack, String nbtTagString) {
        if (itemStack == null || nbtTagString == null || nbtTagString.isEmpty()) {
            return itemStack;
        }
        Object nmsItem = execStatic(getCraftItemStackClass(), "asNMSCopy", itemStack);
        Object nbtTag = exec(nmsItem, "getTag");
        Object nbtTagNew = execStatic(getNBTTagParser(nmsItem), "parse", nbtTagString);
        nbtTag = merge(nbtTagNew, nbtTag);
        exec(nmsItem, "setTag", nbtTag);
        Object item = execStatic(getCraftItemStackClass(), "asBukkitCopy", nmsItem);
        if (item instanceof ItemStack) {
            return (ItemStack) item;
        }
        return itemStack;
    }

    /**
     * Merges two NBTTagCompound objects
     */
    private static Object merge(Object src, Object tgt) {
        if (tgt == null) {
            return src;
        }
        try {
            Field mapField = src.getClass().getDeclaredField("map");
            boolean wasAccessible = mapField.isAccessible();
            mapField.setAccessible(true);
            Map<String, Object> map = (Map<String, Object>) mapField.get(src);
            mapField.setAccessible(wasAccessible);
            Class<?> NBTBase = Class.forName(getPackageName(tgt) + ".NBTBase");
            for (String key : map.keySet()) {
                Object val = exec(src, "get", new Class[]{String.class}, key);
                exec(tgt, "set", new Class[]{String.class, NBTBase}, key, val);
            }
            return tgt;
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            log.info("Unable to transfer NBTTag from " + src + " to " + tgt + ": " + e);
        }
        return tgt;
    }

    private static Class<?> getNBTTagParser(Object nmsItem) {
        try {
            return Class.forName(getPackageName(nmsItem) + ".MojangsonParser");
        } catch (ClassNotFoundException e) {
            log.info("Unable to instantiate MojangsonParser: " + e);
        }
        return null;
    }

    private static Class<?> getCraftItemStackClass() {
        String version = getCraftBukkitVersion();
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (Exception e) {
            log.info("Unable to find CraftItemStack: " + e);
        }
        return null;
    }

    private static String getCraftBukkitVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private static String getNMSVersion(Object nmsObject) {
        return nmsObject != null ? nmsObject.getClass().getPackage().getName().split("\\.")[3] : "";
    }

    private static String getPackageName(Object nmsObject) {
        return nmsObject != null ? nmsObject.getClass().getPackage().getName() : "";
    }

    private static Class<?> getBukkitClass(Object arg) {
        Class clazz = arg != null ? arg.getClass() : null;
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
     */
    private static <T> T execStatic(Class<?> clazz, String methodName, Object... args) {
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

    private static <T> T exec(Object obj, String methodName, Class[] argTypes, Object... args) {
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

    private static <T> T exec(Object obj, String methodName, Object... args) {
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

package us.talabrek.ultimateskyblock.handler;

import dk.lockfuglsang.minecraft.reflection.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import us.talabrek.ultimateskyblock.util.VersionUtil;

/**
 * Abstraction for spawning entities (cross-version-support)
 */
public class EntitySpawner {
    public Skeleton spawnWitherSkeleton(Location location) {
        String craftBukkitVersion = ReflectionUtil.getCraftBukkitVersion();
        VersionUtil.Version version = VersionUtil.getVersion(craftBukkitVersion);
        Skeleton mob;
        // TODO: R4zorax - 29-12-2016: The deprecated parts might need to be used using reflection *sigh*
        if (version.isGTE("1.11")) {
            mob = (Skeleton) location.getWorld().spawnEntity(location, EntityType.fromId(5));
        } else {
            mob = (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
            mob.setSkeletonType(Skeleton.SkeletonType.WITHER);
        }
        mob.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD, 1));
        return mob;
    }

    public Blaze spawnBlaze(Location location) {
        return (Blaze) location.getWorld().spawnEntity(location, EntityType.BLAZE);
    }

    public Skeleton spawnSkeleton(Location location) {
        return (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
    }
}

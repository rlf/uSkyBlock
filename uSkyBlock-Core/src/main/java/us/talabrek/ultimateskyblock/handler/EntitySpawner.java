package us.talabrek.ultimateskyblock.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;

/**
 * Abstraction for spawning entities (cross-version-support)
 */
@SuppressWarnings("UnusedReturnValue")
public class EntitySpawner {
    public WitherSkeleton spawnWitherSkeleton(Location location) {
        WitherSkeleton mob = (WitherSkeleton) location.getWorld().spawnEntity(location, EntityType.WITHER_SKELETON);
        mob.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD, 1));
        return mob;
    }

    public Blaze spawnBlaze(Location location) {
        return (Blaze) location.getWorld().spawnEntity(location, EntityType.BLAZE);
    }

    public Skeleton spawnSkeleton(Location location) {
        return (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
    }
}

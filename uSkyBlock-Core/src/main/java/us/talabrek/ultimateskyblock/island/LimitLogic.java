package us.talabrek.ultimateskyblock.island;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.lockfuglsang.minecraft.po.I18nUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class LimitLogic {
    public enum CreatureType { UNKNOWN, ANIMAL, MONSTER, VILLAGER, GOLEM }
    static {
        marktr("UNKNOWN");
        marktr("ANIMAL");
        marktr("MONSTER");
        marktr("VILLAGER");
        marktr("GOLEM");
    }

    private final uSkyBlock plugin;

    public LimitLogic(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    public Map<CreatureType, Integer> getCreatureCount(IslandInfo islandInfo) {
        Map<CreatureType, Integer> mapCount = new HashMap<>();
        for (CreatureType type : CreatureType.values()) {
            mapCount.put(type, 0);
        }
        Location islandLocation = islandInfo.getIslandLocation();
        ProtectedRegion islandRegionAt = WorldGuardHandler.getIslandRegionAt(islandLocation);
        // Nether and Overworld regions are more or less equal (same x,z coords)
        List<Creature> creatures = WorldGuardHandler.getCreaturesInRegion(plugin.getWorld(), islandRegionAt);
        World nether = plugin.getSkyBlockNetherWorld();
        if (nether != null) {
            creatures.addAll(WorldGuardHandler.getCreaturesInRegion(nether, islandRegionAt));
        }
        for (Creature creature : creatures) {
            CreatureType key = getCreatureType(creature);
            if (!mapCount.containsKey(key)) {
                mapCount.put(key, 0);
            }
            mapCount.put(key, mapCount.get(key) + 1);
        }
        return mapCount;
    }

    public Map<CreatureType, Integer> getCreatureMax(IslandInfo islandInfo) {
        Map<CreatureType, Integer> max = new LinkedHashMap<>();
        for (CreatureType creatureType : CreatureType.values()) {
            max.put(creatureType, getMax(islandInfo, creatureType));
        }
        return max;
    }

    public CreatureType getCreatureType(Creature creature) {
        if (creature instanceof Monster || creature instanceof WaterMob) {
            return CreatureType.MONSTER;
        } else if (creature instanceof Animals) {
            return CreatureType.ANIMAL;
        } else if (creature instanceof Villager) {
            return CreatureType.VILLAGER;
        } else if (creature instanceof Golem) {
            return CreatureType.GOLEM;
        }
        return CreatureType.UNKNOWN;
    }

    public CreatureType getCreatureType(EntityType entityType) {
        if (Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.MONSTER;
        } else if (Animals.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.ANIMAL;
        } else if (Villager.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.VILLAGER;
        } else if (Golem.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.GOLEM;
        }
        return CreatureType.UNKNOWN;
    }

    public boolean canSpawn(EntityType entityType, IslandInfo islandInfo) {
        Map<CreatureType, Integer> creatureCount = getCreatureCount(islandInfo);
        CreatureType creatureType = getCreatureType(entityType);
        int max = getMax(islandInfo, creatureType);
        if (creatureCount.containsKey(creatureType) && creatureCount.get(creatureType) >= max) {
            return false;
        }
        return true;
    }

    private int getMax(IslandInfo islandInfo, CreatureType creatureType) {
        switch (creatureType) {
            case ANIMAL: return islandInfo.getMaxAnimals();
            case MONSTER: return islandInfo.getMaxMonsters();
            case VILLAGER: return islandInfo.getMaxVillagers();
            case GOLEM: return islandInfo.getMaxGolems();
        }
        return 0;
    }

    public String getSummary(IslandInfo islandInfo) {
        Map<LimitLogic.CreatureType, Integer> creatureMax = getCreatureMax(islandInfo);
        Map<LimitLogic.CreatureType, Integer> count = getCreatureCount(islandInfo);
        StringBuilder sb = new StringBuilder();
        for (LimitLogic.CreatureType key : creatureMax.keySet()) {
            if (key == CreatureType.UNKNOWN) {
                continue; // Skip
            }
            int cnt = count.containsKey(key) ? count.get(key) : 0;
            int max = creatureMax.get(key);
            sb.append(tr("\u00a77{0}: \u00a7a{1}\u00a77 (max. {2})", tr(key.name()), cnt >= max ? tr("\u00a7c{0}",cnt) : cnt, max) + "\n");
        }
        return sb.toString().trim();
    }
}

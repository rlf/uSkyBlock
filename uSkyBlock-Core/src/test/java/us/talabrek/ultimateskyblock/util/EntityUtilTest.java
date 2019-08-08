package us.talabrek.ultimateskyblock.util;

import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EntityUtilTest {
    @Test
    public void testGetEntity() {
        List<Entity> testList = new ArrayList<>();

        ArmorStand fakeArmorStand = mock(ArmorStand.class);
        Cow fakeCow = mock(Cow.class);
        Evoker fakeEvoker = mock(Evoker.class);
        Guardian fakeGuardian = mock(Guardian.class);
        Pig fakePig = mock(Pig.class);
        PigZombie fakePigZombie = mock(PigZombie.class);
        Pillager fakePillager = mock(Pillager.class);
        Sheep fakeSheep = mock(Sheep.class);
        Skeleton fakeSkeleton = mock(Skeleton.class);
        Turtle fakeTurtle = mock(Turtle.class);
        Villager fakeVillager = mock(Villager.class);
        WanderingTrader fakeWanderingTrader = mock(WanderingTrader.class);

        testList.add(fakeArmorStand);
        testList.add(fakeCow);
        testList.add(fakeEvoker);
        testList.add(fakeGuardian);
        testList.add(fakePig);
        testList.add(fakePigZombie);
        testList.add(fakePillager);
        testList.add(fakeSheep);
        testList.add(fakeSkeleton);
        testList.add(fakeTurtle);
        testList.add(fakeVillager);
        testList.add(fakeWanderingTrader);

        List<Sheep> sheepList = EntityUtil.getEntity(testList, Sheep.class);
        assertEquals(1, sheepList.size());
        assertEquals(fakeSheep, sheepList.get(0));

        List<Animals> animalsList = EntityUtil.getAnimals(testList);
        assertEquals(4, animalsList.size());
        assertTrue(animalsList.contains(fakeCow));
        assertTrue(animalsList.contains(fakePig));
        assertTrue(animalsList.contains(fakeSheep));
        assertTrue(animalsList.contains(fakeTurtle));

        List<Monster> monsterList = EntityUtil.getMonsters(testList);
        assertEquals(5, monsterList.size());
        assertTrue(monsterList.contains(fakeEvoker));
        assertTrue(monsterList.contains(fakeGuardian));
        assertTrue(monsterList.contains(fakePigZombie));
        assertTrue(monsterList.contains(fakePillager));
        assertTrue(monsterList.contains(fakeSkeleton));

        List<NPC> npcList = EntityUtil.getNPCs(testList);
        assertEquals(2, npcList.size());
        assertTrue(npcList.contains(fakeVillager));
        assertTrue(npcList.contains(fakeWanderingTrader));
    }

    @Test
    public void testGetEntityDisplayName() {
        assertEquals("ArmorStand", EntityUtil.getEntityDisplayName(EntityType.ARMOR_STAND));
        assertEquals("Bat", EntityUtil.getEntityDisplayName(EntityType.BAT));
        assertEquals("CaveSpider", EntityUtil.getEntityDisplayName(EntityType.CAVE_SPIDER));
        assertEquals("WanderingTrader", EntityUtil.getEntityDisplayName(EntityType.WANDERING_TRADER));
    }
}

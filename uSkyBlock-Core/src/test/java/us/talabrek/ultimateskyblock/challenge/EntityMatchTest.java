package us.talabrek.ultimateskyblock.challenge;

import org.bukkit.DyeColor;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.material.Colorable;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EntityMatchTest {
    @Test
    public void testMatchColoredSheep() {
        EntityMatch matcher = new EntityMatch(EntityType.SHEEP, Map.of("Color", "RED"), 1);

        Sheep fakeSheep = mock(Sheep.class, withSettings().extraInterfaces(Colorable.class));
        when(fakeSheep.getColor()).thenReturn(DyeColor.RED);
        when(fakeSheep.getType()).thenReturn(EntityType.SHEEP);

        assertEquals("Red Sheep", matcher.getDisplayName());
        assertEquals(1, matcher.getCount());
        assertTrue(matcher.matches(fakeSheep));

        when(fakeSheep.getColor()).thenReturn(DyeColor.WHITE);
        assertFalse(matcher.matches(fakeSheep));
    }

    @Test
    public void testMatchLegacyColoredSheep() {
        EntityMatch matcher = new EntityMatch(EntityType.SHEEP, Map.of("color", 4.0), 9);

        Sheep fakeSheep = mock(Sheep.class, withSettings().extraInterfaces(Colorable.class));
        when(fakeSheep.getColor()).thenReturn(DyeColor.YELLOW);
        when(fakeSheep.getType()).thenReturn(EntityType.SHEEP);

        assertEquals("Yellow Sheep", matcher.getDisplayName());
        assertEquals(9, matcher.getCount());
        assertTrue(matcher.matches(fakeSheep));

        when(fakeSheep.getColor()).thenReturn(DyeColor.BLACK);
        assertFalse(matcher.matches(fakeSheep));
    }

    @Test
    public void testMatchColoredCow() {
        EntityMatch matcher = new EntityMatch(EntityType.COW, Map.of("Color", "RED"), 1);

        Cow fakeCow = mock(Cow.class);
        when(fakeCow.getType()).thenReturn(EntityType.COW);

        assertEquals("Red Cow", matcher.getDisplayName());
        assertTrue(matcher.matches(fakeCow));
    }
}

package us.talabrek.ultimateskyblock.island;

import org.bukkit.Location;
import org.junit.Test;
import us.talabrek.ultimateskyblock.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class IslandLocatorLogicTest {
    @Test
    public void testNextIslandLocation() throws Exception {
        Settings.island_distance = 1;
        Location p = new Location(null, 0, 0, 0);
        File csvFile = File.createTempFile("newislands", ".csv");
        PrintWriter writer = new PrintWriter(new FileWriter(csvFile));
        for (int i = 0; i < 49; i++) {
            p = IslandLocatorLogic.nextIslandLocation(p);
            writer.println(p.getBlockX() + ";" + p.getBlockZ());
        }
        System.out.println("Wrote first 49 island locations to " + csvFile);
    }
}
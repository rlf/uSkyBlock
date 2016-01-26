package us.talabrek.ultimateskyblock;

import org.bukkit.Location;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;

public class uSkyBlockTest {

    @org.junit.Test
    public void testStripFormatting() throws Exception {
        String text = "&eHello \u00a7bBabe &l&kYou wanna dance&r with somebody";

        assertThat(stripFormatting(text), is("Hello Babe You wanna dance with somebody"));
    }

    @Test
    public void testNextIslandLocation() throws IOException {
        Settings.island_distance = 1;
        Location p = new Location(null, 0, 0, 0);
        File csvFile = File.createTempFile("newislands", ".csv");
        PrintWriter writer = new PrintWriter(new FileWriter(csvFile));
        for (int i = 0; i < 49; i++) {
            p = uSkyBlock.nextIslandLocation(p);
            writer.println(p.getBlockX() + ";" + p.getBlockZ());
        }
        System.out.println("Wrote first 49 island locations to " + csvFile);
    }
}
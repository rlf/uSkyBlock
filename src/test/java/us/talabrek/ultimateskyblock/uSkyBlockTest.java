package us.talabrek.ultimateskyblock;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class uSkyBlockTest {

    @org.junit.Test
    public void testStripFormatting() throws Exception {
        String text = "&eHello \u00a7bBabe &l&kYou wanna dance&r with somebody";

        assertThat(uSkyBlock.stripFormatting(text), is("Hello Babe You wanna dance with somebody"));
    }

    @org.junit.Test
    public void testCorrectFormatting() throws Exception {
        String text = "&eHello \u00a7bBabe &l&kYou wanna dance&r with somebody";
        assertThat(uSkyBlock.correctFormatting(text), is("\u00a7eHello \u00a7bBabe \u00a7l\u00a7kYou wanna dance\u00a7r with somebody"));
    }
}
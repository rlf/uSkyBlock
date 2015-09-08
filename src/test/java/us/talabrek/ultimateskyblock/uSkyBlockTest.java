package us.talabrek.ultimateskyblock;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static us.talabrek.ultimateskyblock.util.FormatUtil.stripFormatting;

public class uSkyBlockTest {

    @org.junit.Test
    public void testStripFormatting() throws Exception {
        String text = "&eHello \u00a7bBabe &l&kYou wanna dance&r with somebody";

        assertThat(stripFormatting(text), is("Hello Babe You wanna dance with somebody"));
    }

}
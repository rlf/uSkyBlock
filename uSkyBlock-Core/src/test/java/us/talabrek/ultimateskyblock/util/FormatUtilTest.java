package us.talabrek.ultimateskyblock.util;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static us.talabrek.ultimateskyblock.util.FormatUtil.capitalize;
import static us.talabrek.ultimateskyblock.util.FormatUtil.normalize;
import static us.talabrek.ultimateskyblock.util.FormatUtil.wordWrap;

public class FormatUtilTest {

    @Test
    public void testNormalize() throws Exception {
        String input = "pre&00&11&22&33&44&55&66&77&88&99&aa&bb&cc&dd&ee&ff&ggpost";
        String expected = "pre§00§11§22§33§44§55§66§77§88§99§aa§bb§cc§dd§ee§ff&ggpost";
        assertThat(normalize(input), is(expected));
    }

    @Test
    public void testWordWrap() throws Exception {
        assertThat(wordWrap("asdadfasd asfasdfasd", 9, 12),
                is(Arrays.asList(
                "asdadfasd", "asfasdfasd")));
        assertThat(wordWrap("§1Hello §2World §3How are you", 4, 10),
                is(Arrays.asList("§1Hello", "§2World §3How", "§3are you")));

        assertThat(wordWrap("§1§2§3§4§5Hello §aWorld §1§2§3§4what happens", 10, 10), is(Arrays.asList(
            "§1§2§3§4§5Hello §aWorld", "§1§2§3§4what happens"
        )));
    }

    @Test
    public void testCapitalize() throws Exception {
        assertThat(capitalize("SAND_CASTLE"), is("SandCastle"));
        assertThat(capitalize("SHEEP"), is("Sheep"));
        assertThat(capitalize("ender chest"), is("EnderChest"));
    }
}
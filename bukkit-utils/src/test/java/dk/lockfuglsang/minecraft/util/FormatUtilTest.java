package dk.lockfuglsang.minecraft.util;

import org.junit.Test;

import java.util.Arrays;

import static dk.lockfuglsang.minecraft.util.FormatUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

        assertThat(wordWrap("this is a short story of a long sentence withaverylongword in the end", 14, 14),
                is(Arrays.asList(
                        "this is a short",
                        "story of a long",
                        "sentence withaverylongword",
                        "in the end")));
    }

    @Test
    public void testWordWrapStrict() {
        assertThat(wordWrapStrict("this is a short story of a long sentence withaverylongword in the end", 14),
                is(Arrays.asList(
                        "this is a",
                        "short story of",
                        "a long",
                        "sentence witha",
                        "verylongword",
                        "in the end")));

        assertThat(wordWrapStrict("§1Hello §2World §3How are you", 10),
                is(Arrays.asList("§1Hello", "§2World §3How", "§3are you")));
    }

    @Test
    public void testCapitalize() throws Exception {
        assertThat(camelcase("SAND_CASTLE"), is("SandCastle"));
        assertThat(camelcase("SHEEP"), is("Sheep"));
        assertThat(camelcase("ender chest"), is("EnderChest"));
    }
}
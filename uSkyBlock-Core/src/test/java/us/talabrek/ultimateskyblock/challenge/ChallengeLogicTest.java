package us.talabrek.ultimateskyblock.challenge;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ChallengeLogicTest {

    @Test
    public void testWordWrap() {
        String test = "a little version that doesn't mean anything, but should be broken on multiple lines";
        List<String> expected = Arrays.asList(
                "a little",
                "version that doesn't",
                "mean anything,",
                "but should be broken",
                "on multiple lines");

        assertThat(ChallengeLogic.wordWrap(test, 8, 15), is(expected));
    }
}
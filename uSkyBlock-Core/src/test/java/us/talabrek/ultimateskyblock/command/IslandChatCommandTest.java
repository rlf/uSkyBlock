package us.talabrek.ultimateskyblock.command;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by R4zorax on 04/07/2016.
 */
public class IslandChatCommandTest {
    @Test
    public void getFormat() throws Exception {
        String format = "\u00a7b{D}: \u00a7a{M}";
        String message = "Hello world $123";
        String msg = format.replaceAll("\\{D\\}", "\u00a7cR4zorax");
        assertThat(msg, is("\u00a7b\u00a7cR4zorax: \u00a7a{M}"));
        msg = msg.replaceAll("\\{M\\}", Matcher.quoteReplacement(message));
        assertThat(msg, is("\u00a7b\u00a7cR4zorax: \u00a7aHello world $123"));
    }

}
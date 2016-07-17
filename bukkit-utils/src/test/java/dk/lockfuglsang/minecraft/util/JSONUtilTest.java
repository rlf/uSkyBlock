package dk.lockfuglsang.minecraft.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test of the JSONUtil
 */
public class JSONUtilTest {
    @Test
    public void json2StringSimple() throws Exception {
        String json = "{\"extra\":[{\"text\":\"hello world\"}]}";
        assertThat(JSONUtil.json2String(json), is("hello world"));
    }

    @Test
    public void json2StringColor() throws Exception {
        String json = "{\"extra\":["
                + "{\"color\":\"black\",\"text\":\" black \"},"
                + "{\"color\":\"dark_blue\",\"text\":\" dark_blue \"},"
                + "{\"color\":\"dark_green\",\"text\":\" dark_green \"},"
                + "{\"color\":\"dark_aqua\",\"text\":\" dark_aqua \"},"
                + "{\"color\":\"dark_red\",\"text\":\" dark_red \"},"
                + "{\"color\":\"dark_purple\",\"text\":\" dark_purple \"},"
                + "{\"color\":\"gold\",\"text\":\" gold \"},"
                + "{\"color\":\"gray\",\"text\":\" gray \"},"
                + "{\"color\":\"dark_gray\",\"text\":\" dark_gray \"},"
                + "{\"color\":\"blue\",\"text\":\" blue \"},"
                + "{\"color\":\"green\",\"text\":\" green \"},"
                + "{\"color\":\"aqua\",\"text\":\" aqua \"},"
                + "{\"color\":\"red\",\"text\":\" red \"},"
                + "{\"color\":\"light_purple\",\"text\":\" light_purple \"},"
                + "{\"color\":\"yellow\",\"text\":\" yellow \"},"
                + "{\"color\":\"white\",\"text\":\" white \"},"
                + "]}";
        assertThat(JSONUtil.json2String(json), is("\u00a70 black \u00a71 dark_blue \u00a72 dark_green "
                +"\u00a73 dark_aqua \u00a74 dark_red \u00a75 dark_purple \u00a76 gold \u00a77 gray "
                +"\u00a78 dark_gray \u00a79 blue \u00a7a green \u00a7b aqua \u00a7c red "
                +"\u00a7d light_purple \u00a7e yellow \u00a7f white "));
    }

    @Test
    public void json2StringFormat() throws Exception {
        // "obfuscated", "bold", "strikethrough", "underline", "italic", "reset"
        String json = "{\"extra\":["
                + "{\"text\":\"hello world \"},"
                + "{\"color\":\"red\",\"obfuscated\":true,\"text\":\"magic\"},"
                + "{\"color\":\"green\",\"bold\":true,\"text\":\" bold\"},"
                + "{\"color\":\"yellow\",\"strikethrough\":true,\"text\":\" strike \"},"
                + "{\"underline\":true,\"color\":\"red\",\"text\":\" underline \"},"
                + "{\"color\":\"yellow\",\"italic\":true,\"text\":\" italic \"},"
                + "{\"color\":\"reset\",\"text\":\" reset \"},"
                + "]}";
        assertThat(JSONUtil.json2String(json), is("hello world "
                +"\u00a7c\u00a7kmagic"
                +"\u00a7a\u00a7l bold"
                +"\u00a7e\u00a7m strike "
                +"\u00a7c\u00a7n underline "
                +"\u00a7e\u00a7o italic "
                +"\u00a7r reset "
        ));
    }

    @Test
    public void json2StringJSONObject() throws Exception {

    }

    @Test
    public void json2StringJSONArray() throws Exception {

    }

}
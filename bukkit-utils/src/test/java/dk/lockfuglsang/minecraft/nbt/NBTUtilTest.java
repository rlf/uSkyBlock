package dk.lockfuglsang.minecraft.nbt;

import org.hamcrest.CoreMatchers;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the NBTUtil
 */
public class NBTUtilTest {

    /**
     * Tests that the JSONMap will return the proper syntax.
     */
    @Test
    public void testJSONMap() throws IOException, ParseException {
        String jsonString = "{\"Potion\":\"minecraft:empty\",\"CustomPotionEffects\":[{\"Id\":1},{\"Id\":2}]}";
        Map<String, Object> map = (Map<String, Object>) new JSONParser().parse(new StringReader(jsonString));
        assertThat(map.get("Potion"), CoreMatchers.<Object>is("minecraft:empty"));
        assertThat(map.get("CustomPotionEffects"), instanceOf(List.class));
        assertThat(((List)map.get("CustomPotionEffects")).get(0), instanceOf(Map.class));
    }

    @Test
    public void testGetGraftBukkitVersion() {
        assertThat("net.minecraft.server.v1_10_R1.NBTTagString".split("\\.")[3], is("v1_10_R1"));
    }
}
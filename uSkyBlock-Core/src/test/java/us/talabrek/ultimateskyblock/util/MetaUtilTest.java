package us.talabrek.ultimateskyblock.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MetaUtilTest {
    @Test
    public void testSingleMapParse() throws Exception {
        String inputMap = "{\"Color\":8}";

        Map<String, Object> outputMap = MetaUtil.createMap(inputMap);
        Assert.assertEquals(1, outputMap.size());
        Assert.assertEquals(8.0, outputMap.get("Color"));
    }
}

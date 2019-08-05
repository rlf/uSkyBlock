package us.talabrek.ultimateskyblock.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class UUIDUtilTest {
    private final String TEST_UUID_SHORT = "98c44c3c563a46b6a0e2209cee164958";
    private final String TEST_UUID_LONG = "98c44c3c-563a-46b6-a0e2-209cee164958";

    @Test
    public void fromStringTest() throws Exception {
        Assert.assertNull(UUIDUtil.fromString(null));
        Assert.assertNull(UUIDUtil.fromString(""));
        Assert.assertNull(UUIDUtil.fromString(TEST_UUID_LONG.substring(30)));

        Assert.assertEquals(TEST_UUID_LONG, UUIDUtil.fromString(TEST_UUID_LONG).toString());
        Assert.assertEquals(TEST_UUID_LONG, UUIDUtil.fromString(TEST_UUID_SHORT).toString());
    }

    @Test
    public void asStringTest() throws Exception {
        Assert.assertEquals("", UUIDUtil.asString(null));
        Assert.assertEquals(TEST_UUID_LONG, UUIDUtil.asString(UUID.fromString(TEST_UUID_LONG)));
    }
}

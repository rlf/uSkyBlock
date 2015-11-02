package dk.lockfuglsang.minecraft.po;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class POParserTest {

    @Test
    public void testReadPOAsProperties() throws Exception {
        verifyProps("valid_da.po");
    }

    @Test
    public void testReadPOAsPropertiesNullInput() throws Exception {
        assertThat(POParser.asProperties(null), nullValue());
    }

    @Test(expected = IOException.class)
    public void testPOWithMissingMsgid() throws Exception {
        POParser.asProperties(getClass().getClassLoader().getResourceAsStream("invalid_missing_msgid.po"));
    }

    @Test(expected = IOException.class)
    public void testPOWithMissingMsgstr() throws Exception {
        POParser.asProperties(getClass().getClassLoader().getResourceAsStream("invalid_missing_msgstr.po"));
    }

    @Test(expected = IOException.class)
    public void testPOWithMissingContent() throws Exception {
        POParser.asProperties(getClass().getClassLoader().getResourceAsStream("invalid_mixed_content.po"));
    }

    @Test
    public void testPOWithEmptyLinesInId() throws Exception {
        verifyProps("valid_emptylines_id.po");
    }

    @Test
    public void testPOWithEmptyLinesInStr() throws Exception {
        verifyProps("valid_emptylines_str.po");
    }

    private void verifyProps(String name) throws IOException {
        POParser parser = new POParser();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
            Properties properties = parser.readPOAsProperties(in);
            assertThat(properties, notNullValue());
            assertThat(properties.size(), is(5));

            String key = "\u00a74Requirements will reset in {0} days.";
            String value = "\u00a74Kravene vil nulstilles om {0} dage.";
            assertThat(properties.getProperty(key), is(value));

            key = "\u00a7eThis challenge\n requires the following:";
            value = "\u00a7eDenne udfordring\n kræver følgende:";
            assertThat(properties.getProperty(key), is(value));

            key = "\u00a74Requirements = will reset in {0} minutes.";
            value = "\u00a74Kravene vil = nulstilles om {0} minutter.";
            assertThat(properties.getProperty(key), is(value));
        }
    }
}
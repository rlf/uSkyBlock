package us.talabrek.ultimateskyblock.imports;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemComponentConverterTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testMinimalChallengeConversion() throws Exception {
        testConfig("test-challenges.yml", "test-challenges-expected.yml", "challenges.yml");
    }

    @Test
    public void testDefaultChallengeConversion() throws Exception {
        testConfig("old-default-challenges.yml", "old-default-challenges-expected.yml", "challenges.yml");
    }

    @Test
    public void testDefaultSettingsConversion() throws Exception {
        testConfig("old-config.yml", "old-config-expected.yml", "config.yml");
    }

    private void testConfig(String originalName, String expectedName, String fileName) throws Exception {
        var testFile = new File(testFolder.getRoot(), fileName);
        try (var reader = Objects.requireNonNull(getClass().getResourceAsStream(originalName))) {
            Files.copy(reader, testFile.toPath());
        }

        var converter = new ItemComponentConverter(Logger.getAnonymousLogger());
        converter.importFile(testFile);

        assertTrue(testFile.exists());
        File backup = new File(testFolder.getRoot(), fileName + ".old");
        assertTrue(backup.isFile());

        YamlConfiguration actual = new YamlConfiguration();
        actual.load(testFile);
        YamlConfiguration expected = new YamlConfiguration();
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
            getClass().getResourceAsStream(expectedName)), StandardCharsets.UTF_8))) {
            expected.load(reader);
        }
        assertConfigsEquals(expected, actual);
    }

    private void assertConfigsEquals(YamlConfiguration expected, YamlConfiguration actual) {
        for (String key : expected.getKeys(true)) {
            assertTrue("Missing key: " + key, actual.contains(key));
            if (expected.isConfigurationSection(key)) {
                assertTrue("Key should be a section: " + key, actual.isConfigurationSection(key));
            } else {
                assertEquals("Items mismatch at key: " + key, expected.get(key), actual.get(key));
            }
            assertThat("Comments mismatch at key: " + key, actual.getComments(key), is(expected.getComments(key)));
            assertThat("Inline comments mismatch at key: " + key, actual.getInlineComments(key), is(expected.getInlineComments(key)));
        }
        assertThat("Headers should be the same", actual.options().getHeader(), is(expected.options().getHeader()));
        assertThat("Footers should be the same", actual.options().getFooter(), is(expected.options().getFooter()));
    }
}

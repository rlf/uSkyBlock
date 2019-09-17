package dk.lockfuglsang.minecraft.po;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class I18nUtilTest {
    @Before
    public void setUp() throws Exception {
        URL dataFolderUrl = getClass().getClassLoader().getResource("");
        I18nUtil.setDataFolder(new File(dataFolderUrl.getFile()));
    }

    @Test
    public void testTr_nullKey() {
        assertThat(I18nUtil.tr(null), is(""));
    }

    @Test
    public void testTr_emptyKey() {
        assertThat(I18nUtil.tr(""), is(""));
    }

    @Test
    public void testTr_existingKey() {
        String TEST_STRING = "\u00a7eYou do not have access to that island-schematic!";
        String TEST_RESULT = "\u00a7eYou have no azzess to the schemz";

        assertThat(I18nUtil.tr(TEST_STRING), is(TEST_RESULT));
    }

    @Test
    public void testTr_nonExistingKey() {
        String TEST_STRING = "\u00a7eYou have no access to that island-schematic!";

        assertThat(I18nUtil.tr(TEST_STRING), is(TEST_STRING));
    }

    @Test
    public void testTr_existingKeyWithFormatting() {
        String TEST_STRING = "\u00a7eNo active cooldowns for \u00a79{0}\u00a7e found.";
        String TEST_ARG = "linksssofrechts";
        String TEST_RESULT = "\u00a74* \u00a77No expired coolupz for \u00a76" + TEST_ARG + "\u00a77 found.";

        assertThat(I18nUtil.tr(TEST_STRING, TEST_ARG), is(TEST_RESULT));
    }

    @Test
    public void testTr_nonExistingKeyWithFormatting() {
        String TEST_STRING = "\u00a7eThis key is unknown to {0}.";
        String TEST_ARG = "Bukkit";
        String TEST_RESULT = "\u00a7eThis key is unknown to " + TEST_ARG + ".";

        assertThat(I18nUtil.tr(TEST_STRING, TEST_ARG), is(TEST_RESULT));
    }

    @Test
    public void testTr_existingKeyNullArgs() {
        String TEST_STRING = "\u00a7eNo active cooldowns for \u00a79{0}\u00a7e found.";
        String TEST_RESULT = "\u00a74* \u00a77No expired coolupz for \u00a76{0}\u00a77 found.";

        assertThat(I18nUtil.tr(TEST_STRING, (Object[]) null), is(TEST_RESULT));
    }

    @Test
    public void testMarktr_nullKey() {
        assertNull(I18nUtil.marktr(null));
    }

    @Test
    public void testMarktr_emptyKey() {
        assertThat(I18nUtil.marktr(""), is(""));
    }

    @Test
    public void testMarktr_withString() {
        String TEST_STRING = "This is a test message for {0}.";

        assertThat(I18nUtil.marktr(TEST_STRING), is(TEST_STRING));
    }

    @Test
    public void testPre_nullString() {
        assertThat(I18nUtil.pre(null), is(""));
    }

    @Test
    public void testPre_emptyString() {
        assertThat(I18nUtil.pre(""), is(""));
    }

    @Test
    public void testPre_withNonFormattedString() {
        String TEST_STRING = "\u00a7eThis is a test string";

        assertThat(I18nUtil.pre(TEST_STRING), is(TEST_STRING));
    }
    
    @Test
    public void testPre_withFormattedString() {
        String TEST_STRING = "\u00a7bThis is a test for {0} regarding {1}.";
        Object[] TEST_ARGS = new String[]{"Jinxert", "Ultimate Skyblock"};
        String TEST_RESULT = "\u00a7bThis is a test for Jinxert regarding Ultimate Skyblock.";

        assertThat(I18nUtil.pre(TEST_STRING, TEST_ARGS), is(TEST_RESULT));
    }

    @Test
    public void testGetLocale_unset() {
        assertThat(I18nUtil.getLocale(), is(Locale.ENGLISH));
    }

    @Test
    public void testGetLocale_setToChina() {
        I18nUtil.setLocale(Locale.CHINA);

        assertThat(I18nUtil.getLocale(), is(Locale.CHINA));
    }

    @Test
    public void testSetLocale_setToNull() {
        I18nUtil.setLocale(null);

        assertThat(I18nUtil.getLocale(), is(Locale.ENGLISH));
    }
}

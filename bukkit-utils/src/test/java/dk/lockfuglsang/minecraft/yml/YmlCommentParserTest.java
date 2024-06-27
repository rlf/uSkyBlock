package dk.lockfuglsang.minecraft.yml;

import org.junit.Test;

import java.io.File;
import java.io.FileReader;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class YmlCommentParserTest {

    @Test
    public void testLoad() throws Exception {
        File simpleYml = new File(getClass().getClassLoader().getResource("yml/simple.yml").toURI());
        YmlCommentParser parser = new YmlCommentParser();
        parser.load(new FileReader(simpleYml));
        assertThat(parser.getCommentMap(), notNullValue());
        assertThat(parser.getComment(null), nullValue());
        assertThat(parser.getComment("root"), is("#\n" +
                "# This is a simple Yml file\n" +
                "#\n" +
                "# with multiple comments\n"));
        assertThat(parser.getComment("root.child node"), is("# child nodes\n"));
        assertThat(parser.getComment("root.a"), nullValue());
        assertThat(parser.getComment("root.a.section"), nullValue());
        assertThat(parser.getComment("root.a.section.deeper"), is("# a comment\n"));
        assertThat(parser.getComment("root.a.section.deeper.b"), is("# b comment\n"));
        assertThat(parser.getComment("root.child node.some-double"), is("# a number\n"));
    }

    @Test
    public void testReplaceAll() {
        String comment = "# comment\n# and comment  \n#  and...\n";
        assertThat(comment.replaceAll("^# ?", "").replaceAll("\n# ?", "\n"), is("comment\nand comment  \n and...\n"));
    }
}
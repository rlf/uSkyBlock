package dk.lockfuglsang.minecraft.file;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * JUnit tests for FileUtil
 */
public class FileUtilTest {
    @Test
    public void testGetExtension() {
        assertThat(FileUtil.getExtension("basename.ext"), is("ext"));
        assertThat(FileUtil.getExtension("my file.with.dot.yml"), is("yml"));
    }

    @Test
    public void testBaseName() {
        assertThat(FileUtil.getBasename("dir/something/filename.txt"), is("filename"));
        assertThat(FileUtil.getBasename("dir\\something\\filename.txt"), is("filename"));
    }
}
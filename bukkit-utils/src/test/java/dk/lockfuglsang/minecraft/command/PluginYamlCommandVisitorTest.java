package dk.lockfuglsang.minecraft.command;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PluginYamlCommandVisitorTest {
    @Test
    public void writeToSimple() throws Exception {
        PluginYamlCommandVisitor visitor = new PluginYamlCommandVisitor();
        BaseCommandExecutor cmd = new BaseCommandExecutor("cmd|c", "plugin.cmd", "player", "some description");
        cmd.add(new CompositeCommand("sub|s", "plugin.sub", "some sub description"));
        cmd.add(new CompositeCommand("other", "plugin.cmd.other", "some other command"));
        BaseCommandExecutor cmd2 = new BaseCommandExecutor("adm|a", "plugin.adm", "hey jude!");
        cmd2.add(new CompositeCommand("subs|ss", "plugin.sub", "some other sub"));
        cmd2.add(new CompositeCommand("t2", "plugin.cmdtest", "?optional mandatory", "test"));
        String expected = String.join(System.lineSeparator(), Files.readAllLines(
                Paths.get(getClass().getClassLoader().getResource("yml/pluginyml_simple.yml").toURI())));

        cmd.accept(visitor);
        cmd2.accept(visitor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        visitor.writeTo(out);
        assertThat(baos.toString(), is(expected));
    }

    @Test
    public void writeToSimpleFeatureMap() throws Exception {
        PluginYamlCommandVisitor visitor = new PluginYamlCommandVisitor();
        BaseCommandExecutor cmd = new BaseCommandExecutor("cmd|c", "plugin.cmd", "some description");
        CompositeCommand sub = new CompositeCommand("sub|s", "plugin.sub", "some sub description");
        cmd.add(sub);
        sub.addFeaturePermission("plugin.feature.a", "enables A");
        sub.addFeaturePermission("plugin.feature.b", "enables B");
        sub.addFeaturePermission("plugin.featuresub", "standalone feature");
        cmd.add(new CompositeCommand("other", "plugin.cmd.other", "some other command"));
        String expected = String.join(System.lineSeparator(), Files.readAllLines(
                Paths.get(getClass().getClassLoader().getResource("yml/pluginyml_featuremap.yml").toURI())));

        cmd.accept(visitor);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        visitor.writeTo(out);
        assertThat(baos.toString(), is(expected));
    }
}
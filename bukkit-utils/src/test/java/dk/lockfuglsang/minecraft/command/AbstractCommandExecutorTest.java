package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandSender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class AbstractCommandExecutorTest {

    StringBuffer messages = new StringBuffer();
    private static AbstractCommandExecutor mycmd;

    @BeforeClass
    public static void setUp() {
        mycmd = new AbstractCommandExecutor("mycmd", "myplugin.perm.mycmd", "main myplugin command");
        mycmd.add(new AbstractCommand("hello|h", "myplugin.perm.hello", "say hello to the player") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage(new String[]{
                        "Hello! and welcome " + sender.getName(),
                        "I was called with : " + alias,
                        "I had " + args.length + " arguments: " + Arrays.asList(args)
                });
                return true;
            }
        });
    }

    @Test
    public void testNoPermissions() {
        CommandSender sender = createCommandSender();
        mycmd.onCommand(sender, null, "mycmd", new String[]{"mycmd", "h", "your", "momma"});
        assertThat(getMessages(), is("§eInvalid command: mycmd\n" +
                "§7Usage: §3mycmd§a [command|help]§7 - §emain myplugin command\n" +
                "  §3hello§7 | h§a§7 - §esay hello to the player"));
    }

    private String getMessages() {
        return messages.toString().trim();
    }

    @Test
    public void testBasic() {
        CommandSender sender = createCommandSender();
        addPerm(sender, "myplugin.perm.mycmd");
        addPerm(sender, "myplugin.perm.hello");
        mycmd.onCommand(sender, null, "mycmd", new String[]{"h", "your", "momma"});
        assertThat(getMessages(), is("Hello! and welcome null\nI was called with : h\nI had 2 arguments: [your, momma]"));
    }

    private void addPerm(CommandSender sender, String s) {
        when(sender.hasPermission(argThat(is(s)))).thenReturn(true);
    }

    private void addOp(CommandSender sender) {
        when(sender.isOp()).thenReturn(true);
    }

    private CommandSender createCommandSender() {
        CommandSender mock = Mockito.mock(CommandSender.class);
        Answer<Void> answer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                for (Object o : invocationOnMock.getArguments()) {
                    if (o != null && o.getClass().isArray()) {
                        for (Object o2 : (Object[]) o) {
                            messages.append("" + o2 + "\n");
                        }
                    } else {
                        messages.append("" + o + "\n");
                    }
                }
                return null;
            }
        };
        doAnswer(answer).when(mock).sendMessage(anyString());
        doAnswer(answer).when(mock).sendMessage(Matchers.<String[]>any());
        return mock;
    }
}
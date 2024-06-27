package dk.lockfuglsang.minecraft.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class CompositeCommandTest {
    private static UUID ownerUUID = UUID.randomUUID();
    private static UUID adminUUID = UUID.randomUUID();
    private static UUID modUUID = UUID.randomUUID();
    private static BaseCommandExecutor executor;

    @BeforeClass
    public static void setupAll() {
        executor = new BaseCommandExecutor("plugin", "plugin", null, "does stuff", ownerUUID);
        CompositeCommand sut = new CompositeCommand("admin", "admin.admin.superadmin", null, "super important admin command", adminUUID) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage("executed admin");
                return super.execute(sender, alias, data, args);
            }
        };
        sut.add(new AbstractCommand("sub", "perm.sub", "some sub-command") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage("from sub");
                return false;
            }
        });
        sut.add(new AbstractCommand("sub2", "perm.sub2", "some other sub-command", "yay", null, modUUID) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage("from sub2");
                return false;
            }
        });
        executor.add(sut);
    }

    @Test
    public void NoPermOnBase() {
        // Arrange
        Player player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(false);

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin",  "sub"});

        verify(player).sendMessage("§eYou do not have access (§4plugin§e)");
    }

    @Test
    public void PermOnBase() {
        // Arrange
        Player player = mock(Player.class);
        when(player.hasPermission(anyString())).thenAnswer((Answer<Boolean>) invocationOnMock ->
                invocationOnMock.getArguments()[0] == "plugin");

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin",  "sub"});

        verify(player).sendMessage("§eYou do not have access (§4admin.admin.superadmin§e)");
    }

    @Test
    public void PermOnAdmin() {
        // Arrange
        Player player = mock(Player.class);
        final List<String> messages = recordMessages(player);
        when(player.hasPermission(anyString())).thenAnswer((Answer<Boolean>) invocationOnMock ->
                invocationOnMock.getArguments()[0] == "plugin"
                || invocationOnMock.getArguments()[0] == "admin.admin.superadmin"
        );

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin",  "sub"});

        verify(player).sendMessage("executed admin");
        assertThat(messages, Matchers.contains(new String[]{"executed admin", "§eYou do not have access (§4perm.sub§e)"}));
    }

    @Test
    public void AllPerms() {
        // Arrange
        Player player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(true);
        final List<String> messages = recordMessages(player);

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin",  "sub"});

        verify(player).sendMessage("executed admin");
        assertThat(messages, Matchers.contains(new String[]{"executed admin", "from sub"}));
    }

    @Test
    public void NoPerm_PermissionOverride() {
        // Arrange
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(ownerUUID);
        final List<String> messages = recordMessages(player);

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin",  "sub"});

        assertThat(messages, Matchers.contains(new String[]{"executed admin", "from sub"}));
    }

    @Test
    public void NoPerm_PermissionSubOverride() {
        // Arrange
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(adminUUID);
        final List<String> messages = recordMessages(player);

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin",  "sub"});

        assertThat(messages, Matchers.contains(new String[]{"§eYou do not have access (§4plugin§e)"}));
    }

    @Test
    public void BasePerm_PermissionCompositeOverride() {
        // Arrange
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(adminUUID);
        when(player.hasPermission(anyString())).thenAnswer((Answer<Boolean>) invocationOnMock ->
                invocationOnMock.getArguments()[0] == "plugin"
        );
        final List<String> messages = recordMessages(player);

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin", "sub"});

        assertThat(messages, Matchers.contains(new String[]{"executed admin", "from sub"}));
    }

    @Test
    public void BasePerm_PermissionAbstractCommandOverride() {
        // Arrange
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(modUUID);
        when(player.hasPermission(anyString())).thenAnswer((Answer<Boolean>) invocationOnMock ->
                invocationOnMock.getArguments()[0] == "plugin"
                        || invocationOnMock.getArguments()[0] == "admin.admin.superadmin"
        );
        final List<String> messages = recordMessages(player);

        // Act
        executor.onCommand(player, null, "alias", new String[]{"admin", "sub2"});

        assertThat(messages, Matchers.contains(new String[]{"executed admin", "from sub2"}));
    }

    private List<String> recordMessages(Player player) {
        final List<String> messages = new ArrayList<>();
        Answer<Void> voidAnswer = i -> {
            messages.add(String.join(" ", Arrays.asList(i.getArguments()).toArray(new String[0])));
            return null;
        };
        doAnswer(voidAnswer).when(player).sendMessage(anyString());
        return messages;
    }

}
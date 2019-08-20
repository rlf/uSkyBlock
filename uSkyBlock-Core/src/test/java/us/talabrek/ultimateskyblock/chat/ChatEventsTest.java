package us.talabrek.ultimateskyblock.chat;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Before;
import org.junit.Test;
import us.talabrek.ultimateskyblock.api.event.IslandChatEvent;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static us.talabrek.ultimateskyblock.api.event.IslandChatEvent.*;

public class ChatEventsTest {
    private ChatEvents listener;
    private ChatLogic fakeLogic;
    private BukkitScheduler fakeScheduler;

    @Before
    public void setUp() {
        uSkyBlock fakePlugin = mock(uSkyBlock.class);
        Server fakeServer = mock(Server.class);
        when(fakePlugin.getServer()).thenReturn(fakeServer);

        fakeScheduler = mock(BukkitScheduler.class);
        when(fakeScheduler.runTask(any(Plugin.class), any(Runnable.class))).thenReturn(mock(BukkitTask.class));
        when(fakeServer.getScheduler()).thenReturn(fakeScheduler);

        fakeLogic = spy(mock(ChatLogic.class));
        doNothing().when(fakeLogic).sendMessage(any(), any(), any());

        listener = new ChatEvents(fakeLogic, fakePlugin);
    }

    @Test
    public void testOnIslandChatEvent_validIslandChat() {
        final String TEST_MESSAGE = "Valid IT message for Ultimate Skyblock";
        Player onlinePlayer = getFakePlayer(true);

        IslandChatEvent event = new IslandChatEvent(onlinePlayer, Type.ISLAND, TEST_MESSAGE);
        listener.onIslandChatEvent(event);
        verify(fakeLogic).sendMessage(onlinePlayer, Type.ISLAND, TEST_MESSAGE);
    }

    @Test
    public void testOnIslandChatEvent_validPartyChat() {
        final String TEST_MESSAGE = "Valid PT message for Ultimate Skyblock";
        Player onlinePlayer = getFakePlayer(true);

        IslandChatEvent event = new IslandChatEvent(onlinePlayer, Type.PARTY, TEST_MESSAGE);
        listener.onIslandChatEvent(event);
        verify(fakeLogic).sendMessage(onlinePlayer, Type.PARTY, TEST_MESSAGE);
    }

    @Test
    public void testOnIslandChatEvent_offlinePlayer() {
        final String TEST_MESSAGE = "This String is useless... the player is offline :(";
        Player offlinePlayer = getFakePlayer(false);

        IslandChatEvent event = new IslandChatEvent(offlinePlayer, Type.ISLAND, TEST_MESSAGE);
        listener.onIslandChatEvent(event);
        verify(fakeLogic, times(0)).sendMessage(any(), any(), any());
    }

    @Test
    public void testOnIslandChatEvent_nullType() {
        final String TEST_MESSAGE = "Why do we even allow the type to be null?";
        Player onlinePlayer = getFakePlayer(true);

        IslandChatEvent event = new IslandChatEvent(onlinePlayer, null, TEST_MESSAGE);
        listener.onIslandChatEvent(event);
        verify(fakeLogic, times(0)).sendMessage(any(), any(), any());
    }

    @Test
    public void testOnIslandChatEvent_nullMessage() {
        Player onlinePlayer = getFakePlayer(true);

        IslandChatEvent event = new IslandChatEvent(onlinePlayer, Type.ISLAND, null);
        listener.onIslandChatEvent(event);
        verify(fakeLogic, times(0)).sendMessage(any(), any(), any());
    }

    @Test
    public void testOnChatEvent_nullToggle() {
        final String TEST_MESSAGE = "This should be ignored";
        Player onlinePlayer = getFakePlayer(true);
        Set<Player> recipients = new HashSet<>();
        doReturn(null).when(fakeLogic).getToggle(onlinePlayer);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, onlinePlayer, TEST_MESSAGE, recipients);
        listener.onChatEvent(event);
        verify(fakeScheduler, times(0)).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    public void testOnChatEvent_islandToggle() {
        final String TEST_MESSAGE = "Island chat message via toggle";
        Player onlinePlayer = getFakePlayer(true);
        Set<Player> recipients = new HashSet<>();
        doReturn(Type.ISLAND).when(fakeLogic).getToggle(onlinePlayer);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, onlinePlayer, TEST_MESSAGE, recipients);
        listener.onChatEvent(event);
        verify(fakeScheduler).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    public void testOnChatEvent_partyToggle() {
        final String TEST_MESSAGE = "Party chat message via toggle";
        Player onlinePlayer = getFakePlayer(true);
        Set<Player> recipients = new HashSet<>();
        doReturn(Type.PARTY).when(fakeLogic).getToggle(onlinePlayer);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, onlinePlayer, TEST_MESSAGE, recipients);
        listener.onChatEvent(event);
        verify(fakeScheduler).runTask(any(Plugin.class), any(Runnable.class));
    }

    private Player getFakePlayer(boolean online) {
        Player fakePlayer = mock(Player.class);
        when(fakePlayer.getUniqueId()).thenReturn(UUID.fromString("9f22a336-8f70-420a-8cf6-1fe747e08e6f"));
        when(fakePlayer.getName()).thenReturn("Muspah");
        when(fakePlayer.isOnline()).thenReturn(online);
        return fakePlayer;
    }
}

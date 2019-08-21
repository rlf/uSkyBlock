package us.talabrek.ultimateskyblock.event;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import us.talabrek.ultimateskyblock.api.async.Callback;
import us.talabrek.ultimateskyblock.api.event.CreateIslandEvent;
import us.talabrek.ultimateskyblock.api.event.IslandInfoEvent;
import us.talabrek.ultimateskyblock.api.event.MemberJoinedEvent;
import us.talabrek.ultimateskyblock.api.event.MemberLeftEvent;
import us.talabrek.ultimateskyblock.api.event.RestartIslandEvent;
import us.talabrek.ultimateskyblock.api.event.uSkyBlockScoreChangedEvent;
import us.talabrek.ultimateskyblock.island.BlockLimitLogic;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.island.level.IslandScore;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.LocationUtil;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class InternalEventsTest {
    private BlockLimitLogic fakeBlockLimitLogic;
    private uSkyBlock fakePlugin;
    private InternalEvents internalEvents;

    @Before
    public void setUp() {
        fakePlugin = spy(mock(uSkyBlock.class));
        internalEvents = new InternalEvents(fakePlugin);

        YamlConfiguration config = new YamlConfiguration();
        config.set("options.party.join-commands", Arrays.asList("lets", "test", "this"));
        config.set("options.party.leave-commands", Arrays.asList("dont", "stop", "me", "now"));
        doReturn(config).when(fakePlugin).getConfig();

        fakeBlockLimitLogic = spy(mock(BlockLimitLogic.class));
        doNothing().when(fakeBlockLimitLogic).updateBlockCount(any(), any());
        doReturn(fakeBlockLimitLogic).when(fakePlugin).getBlockLimitLogic();

        doReturn(true).when(fakePlugin).restartPlayerIsland(any(), any(), any());
        doNothing().when(fakePlugin).createIsland(any(), any());
        doNothing().when(fakePlugin).calculateScoreAsync(any(), any(), any());
    }

    @Test
    public void testOnRestart() {
        Player player = getFakePlayer();
        Location island = new Location(null, 1.00, 2.00, -1.00);
        String schematic = "default";

        RestartIslandEvent event = new RestartIslandEvent(player, island, schematic);
        internalEvents.onRestart(event);
        verify(fakePlugin).restartPlayerIsland(player, island, schematic);
    }

    @Test
    public void testOnCreate() {
        Player player = getFakePlayer();
        String schematic = "default";

        CreateIslandEvent event = new CreateIslandEvent(player, schematic);
        internalEvents.onCreate(event);
        verify(fakePlugin).createIsland(player, schematic);
    }

    @Test
    public void testOnMemberJoin() {
        IslandInfo fakeIslandInfo = mock(IslandInfo.class);
        PlayerInfo fakePlayerInfo = spy(mock(PlayerInfo.class));
        doReturn(true).when(fakePlayerInfo).execCommands(any());

        List<String> commandList = fakePlugin.getConfig().getStringList("options.party.join-commands");

        MemberJoinedEvent event = new MemberJoinedEvent(fakeIslandInfo, fakePlayerInfo);
        internalEvents.onMemberJoin(event);
        verify(fakePlayerInfo).execCommands(commandList);
    }

    @Test
    public void testOnMemberLeft() {
        IslandInfo fakeIslandInfo = mock(IslandInfo.class);
        PlayerInfo fakePlayerInfo = spy(mock(PlayerInfo.class));
        doReturn(true).when(fakePlayerInfo).execCommands(any());

        List<String> commandList = fakePlugin.getConfig().getStringList("options.party.leave-commands");

        MemberLeftEvent event = new MemberLeftEvent(fakeIslandInfo, fakePlayerInfo);
        internalEvents.onMemberLeft(event);
        verify(fakePlayerInfo).execCommands(commandList);
    }

    @Test
    public void testOnScoreChanged() {
        Player fakePlayer = getFakePlayer();
        IslandScore fakeIslandScore = mock(IslandScore.class);
        Location islandLocation = new Location(null, -10.00, 25.00, 10.00);

        uSkyBlockScoreChangedEvent event = new uSkyBlockScoreChangedEvent(fakePlayer, fakePlugin,
                fakeIslandScore, islandLocation);
        internalEvents.onScoreChanged(event);
        verify(fakeBlockLimitLogic).updateBlockCount(islandLocation, fakeIslandScore);
    }

    @Test
    public void testOnInfoEvent() {
        Player fakePlayer = getFakePlayer();
        Location islandLocation = new Location(null, -10.00, 25.00, 10.00);
        Callback<us.talabrek.ultimateskyblock.api.model.IslandScore> callback =
                new Callback<us.talabrek.ultimateskyblock.api.model.IslandScore>() {
                    @Override
                    public void run() {
                        // Do nothing
                    }
                };

        IslandInfoEvent event = new IslandInfoEvent(fakePlayer, islandLocation, callback);
        internalEvents.onInfoEvent(event);
        verify(fakePlugin).calculateScoreAsync(fakePlayer, LocationUtil.getIslandName(islandLocation), callback);
    }

    private Player getFakePlayer() {
        Player fakePlayer = mock(Player.class);
        when(fakePlayer.getUniqueId()).thenReturn(UUID.fromString("29292160-6d49-47a3-ae1c-7c800e14cca3"));
        when(fakePlayer.getName()).thenReturn("linksssofrechts");
        return fakePlayer;
    }
}

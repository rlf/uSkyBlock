package us.talabrek.ultimateskyblock.api.event;

import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

public class EventLogic {
    private final uSkyBlock plugin;

    public EventLogic(uSkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Fires a new async {@link IslandLeaderChangedEvent}.
     *
     * @param islandInfo {@link IslandInfo} for the island in the scope of this event.
     * @param originalLeaderInfo {@link PlayerInfo} for the original island leader.
     * @param newLeaderInfo {@link PlayerInfo} for the new island leader.
     */
    public void fireIslandLeaderChangedEvent(us.talabrek.ultimateskyblock.api.IslandInfo islandInfo,
                                             us.talabrek.ultimateskyblock.api.PlayerInfo originalLeaderInfo,
                                             us.talabrek.ultimateskyblock.api.PlayerInfo newLeaderInfo) {
        plugin.async(() -> plugin.getServer().getPluginManager().callEvent(new IslandLeaderChangedEvent(islandInfo, originalLeaderInfo, newLeaderInfo)));
    }

    /**
     * Fires a new async {@link MemberJoinedEvent}.
     *
     * @param islandInfo {@link IslandInfo} for the island that the member joined.
     * @param playerInfo {@link PlayerInfo} for the joined member.
     */
    public void fireMemberJoinedEvent(us.talabrek.ultimateskyblock.island.IslandInfo islandInfo, us.talabrek.ultimateskyblock.player.PlayerInfo playerInfo) {
        plugin.async(() -> plugin.getServer().getPluginManager().callEvent(new MemberJoinedEvent(islandInfo, playerInfo)));
    }

    /**
     * Fires a new async {@link MemberLeftEvent}.
     *
     * @param islandInfo {@link IslandInfo} for the island that the member left.
     * @param member {@link PlayerInfo} for the left member.
     */
    public void fireMemberLeftEvent(IslandInfo islandInfo, PlayerInfo member) {
        plugin.async(() -> plugin.getServer().getPluginManager().callEvent(new MemberLeftEvent(islandInfo, member)));
    }

    public void shutdown() {
        // Placeholder for now.
    }
}

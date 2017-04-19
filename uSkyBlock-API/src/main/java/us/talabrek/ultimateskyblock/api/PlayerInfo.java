package us.talabrek.ultimateskyblock.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Basic information about a Player in uSkyBlock.
 * @since 2.7.0
 */
public interface PlayerInfo {
    Player getPlayer();

    String getPlayerName();

    UUID getUniqueId();

    boolean getHasIsland();

    Location getIslandLocation();

    Location getIslandNetherLocation();

    Location getHomeLocation();

    String getDisplayName();

    Collection<ChallengeCompletion> getChallenges();

    List<String> getBannedFrom();

    List<String> getTrustedOn();

    IslandInfo getIslandInfo();

    void createIsland(String schematic);
}

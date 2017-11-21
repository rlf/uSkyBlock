package us.talabrek.ultimateskyblock.uuid;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class NullPlayer implements OfflinePlayer {
    public static final NullPlayer INSTANCE = new NullPlayer();
    private NullPlayer() {
    }
    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getName() {
        return PlayerDB.UNKNOWN_PLAYER_NAME;
    }

    @Override
    public UUID getUniqueId() {
        return PlayerDB.UNKNOWN_PLAYER_UUID;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

//    @Override
//    public void setBanned(boolean b) {
//
//    }

    @Override
    public boolean isWhitelisted() {
        return true;
    }

    @Override
    public void setWhitelisted(boolean b) {

    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Override
    public Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean b) {

    }
}

package us.talabrek.ultimateskyblock.island;

import org.bukkit.Location;
import us.talabrek.ultimateskyblock.async.Callback;

public interface LevelLogic {
    void calculateScoreAsync(Location l, Callback<IslandScore> callback);
}

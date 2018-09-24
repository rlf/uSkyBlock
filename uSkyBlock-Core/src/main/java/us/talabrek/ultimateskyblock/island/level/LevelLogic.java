package us.talabrek.ultimateskyblock.island.level;

import org.bukkit.Location;
import us.talabrek.ultimateskyblock.api.async.Callback;

public interface LevelLogic {
    void calculateScoreAsync(Location l, Callback<IslandScore> callback);
}

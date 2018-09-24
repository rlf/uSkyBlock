
package us.talabrek.ultimateskyblock.handler.multiverseinventories;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Adaptor, so we don't need to rely on MVInv on runtime.
 */
public class MultiverseInventoriesAdaptor {
    public static void linkWorlds(World... worlds) {
        WorldGroupManager groupManager = getMVInv().getGroupManager();
        WorldGroup worldGroup = groupManager.getGroup("skyblock");
        if (worldGroup == null) {
            worldGroup = groupManager.newEmptyGroup("skyblock");
            worldGroup.getShares().addAll(Sharables.ALL_DEFAULT);
        }
        for (World world : worlds) {
            worldGroup.addWorld(world);
        }
        groupManager.updateGroup(worldGroup);
    }

    public static MultiverseInventories getMVInv() {
        return (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
    }
}

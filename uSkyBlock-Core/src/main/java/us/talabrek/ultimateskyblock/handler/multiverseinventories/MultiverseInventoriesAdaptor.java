//package us.talabrek.ultimateskyblock.handler.multiverseinventories;
//
//import com.onarandombox.multiverseinventories.MultiverseInventories;
//import com.onarandombox.multiverseinventories.api.GroupManager;
//import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
//import com.onarandombox.multiverseinventories.api.share.Sharables;
//import org.bukkit.Bukkit;
//import org.bukkit.World;
//
///**
// * Adaptor, so we don't need to rely on MVInv on runtime.
// */
//public class MultiverseInventoriesAdaptor {
//    public static void linkWorlds(World... worlds) {
//        GroupManager groupManager = getMVInv().getGroupManager();
//        WorldGroupProfile worldgroup = groupManager.getGroup("skyblock");
//        if (worldgroup == null) {
//            worldgroup = groupManager.newEmptyGroup("skyblock");
//            worldgroup.getShares().addAll(Sharables.ALL_DEFAULT);
//        }
//        for (World world : worlds) {
//            worldgroup.addWorld(world);
//        }
//        groupManager.updateGroup(worldgroup);
//    }
//
//    public static MultiverseInventories getMVInv() {
//        return (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
//    }
//}

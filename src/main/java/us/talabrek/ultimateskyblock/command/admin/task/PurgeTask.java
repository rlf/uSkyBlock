package us.talabrek.ultimateskyblock.command.admin.task;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import us.talabrek.ultimateskyblock.async.IncrementalTask;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.List;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * The PurgeCommand as an IncrementtalTask.
 */
public class PurgeTask implements IncrementalTask {

    private final uSkyBlock skyBlock;
    private final List<String> removeList;
    private final CommandSender sender;
    private final int size;
    private final int feedbackEvery;
    private long lastContact;

    public PurgeTask(uSkyBlock plugin, List<String> removeList, CommandSender sender) {
        this.skyBlock = plugin;
        this.removeList = removeList;
        this.sender = sender;
        size = removeList.size();
        feedbackEvery = plugin.getConfig().getInt("async.feedbackEvery", 5000);
        lastContact = System.currentTimeMillis();
    }

    @Override
    public boolean execute(Plugin plugin, int offset, int length) {
        PlayerInfo pi = null;
        for (int i = 0; i < length && !removeList.isEmpty(); i++) {
            final String islandName = removeList.remove(0);
            IslandInfo islandInfo = skyBlock.getIslandInfo(islandName);
            for (String member : islandInfo.getMembers()) {
                pi = skyBlock.getPlayerInfo(member);
                if (pi != null) {
                    islandInfo.removeMember(pi);
                }
            }
            WorldGuardHandler.removeIslandRegion(islandInfo.getName());
            skyBlock.getIslandLogic().deleteIslandConfig(islandName);
            long now = System.currentTimeMillis();
            if (now >= (lastContact + feedbackEvery)) {
                lastContact = now;
                sender.sendMessage(tr("\u00a7cPURGE:\u00a79 Purged {0}/{1} {2,##,number}", offset + i, size, 1f*(offset+i)/size));
            }
        }
        skyBlock.getOrphanLogic().save();
        return isComplete();
    }

    @Override
    public int getLength() {
        return size;
    }

    @Override
    public boolean isComplete() {
        return removeList.isEmpty();
    }
}

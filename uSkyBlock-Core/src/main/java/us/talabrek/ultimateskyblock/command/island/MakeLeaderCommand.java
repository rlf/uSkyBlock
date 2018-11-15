package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class MakeLeaderCommand extends RequireIslandCommand {
    public MakeLeaderCommand(uSkyBlock plugin) {
        super(plugin, "makeleader|transfer", "usb.island.makeleader", "member", marktr("transfer leadership to another member"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo currentLeader, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            String newLeader = args[0];
            if (!island.getMembers().contains(newLeader)) {
                player.sendMessage(tr("\u00a74You can only transfer ownership to party-members!"));
                return true;
            }
            if (island.getLeader().equals(newLeader)) {
                player.sendMessage(tr("{0}\u00a7e is already leader of your island!", newLeader));
                return true;
            }
            if (!island.isLeader(player)) {
                player.sendMessage(tr("\u00a74Only leader can transfer leadership!"));
                island.sendMessageToIslandGroup(true, marktr("{0} tried to take over the island!"), newLeader);
                return true;
            }
            island.setupPartyLeader(newLeader); // Promote member
            island.setupPartyMember(currentLeader); // Demote leader
            WorldGuardHandler.updateRegion(island);
            PlayerInfo newLeaderInfo = uSkyBlock.getInstance().getPlayerInfo(newLeader);
            uSkyBlock.getInstance().getEventLogic().fireIslandLeaderChangedEvent(island, currentLeader, newLeaderInfo);
            island.sendMessageToIslandGroup(true, tr("\u00a7bLeadership transferred by {0}\u00a7b to {1}", player.getDisplayName(), newLeader));
            return true;
        }
        return false;
    }
}

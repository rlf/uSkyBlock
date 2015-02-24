package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class MakeLeaderCommand extends RequireIslandCommand {
    public MakeLeaderCommand(uSkyBlock plugin) {
        super(plugin, "makeleader|transfer", "usb.island.create", "member", "transfer leadership to another member");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 1) {
            String member = args[0];
            if (!island.getMembers().contains(member)) {
                player.sendMessage(tr("\u00a74You can only transfer ownership to party-members!"));
                return true;
            }
            if (island.getLeader().equals(member)) {
                player.sendMessage(tr("{0}\u00a7e is already leader of your island!", member));
                return true;
            }
            if (!island.isLeader(player)) {
                player.sendMessage(tr("\u00a74Only leader can transfer leadership!"));
                island.sendMessageToIslandGroup(tr("{0} tried to take over the island!", member));
                return true;
            }
            island.setupPartyLeader(member); // Promote member
            island.setupPartyMember(player.getName()); // Demote leader
            WorldGuardHandler.updateRegion(player, island);
            island.sendMessageToIslandGroup(tr("\u00a7bLeadership transferred by {0}\u00a7b to {1}", player.getDisplayName(), member));
            return true;
        }
        return false;
    }
}

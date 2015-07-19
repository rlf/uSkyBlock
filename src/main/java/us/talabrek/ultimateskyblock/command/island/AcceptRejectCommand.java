package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.InviteHandler;

import java.util.Map;

import us.talabrek.ultimateskyblock.uSkyBlock;
import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

public class AcceptRejectCommand extends RequirePlayerCommand {
    private final InviteHandler inviteHandler;

    public AcceptRejectCommand(InviteHandler inviteHandler) {
        super("accept|reject", "usb.party.join", tr("accept/reject an invitation."));
        this.inviteHandler = inviteHandler;
    }

    @Override
    protected boolean doExecute(String alias, Player player, Map<String, Object> data, String... args) {
        if (alias.equalsIgnoreCase("reject")) {
            if (inviteHandler.reject(player)) {
                player.sendMessage(tr("\u00a7eYou have rejected the invitation to join an island."));
            } else {
                player.sendMessage(tr("\u00a74You haven't been invited."));
            }
        } else if (alias.equalsIgnoreCase("accept")) {
            if (inviteHandler.accept(player)) {
                player.sendMessage(tr("\u00a7eYou have accepted the invitation to join an island."));
            } else {
                player.sendMessage(tr("\u00a74You haven't been invited."));
            }
        }
        return true;
    }
}

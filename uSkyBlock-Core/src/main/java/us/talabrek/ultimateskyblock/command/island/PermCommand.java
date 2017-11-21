package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.menu.PartyPermissionMenuItem;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class PermCommand extends RequireIslandCommand {
    public PermCommand(uSkyBlock plugin) {
        super(plugin, "perm", null, "member ?perm", tr("changes a members island-permissions"));
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        String playerName = args.length > 0 ? args[0] : null;
        String perm = args.length > 1 ? args[1] : null;
        if (playerName != null && island.getMembers().contains(playerName) && perm == null) {
            String msg = tr("\u00a7ePermissions for \u00a79{0}\u00a7e:", playerName) + "\n";
            for (String validPerm : getValidPermissions()) {
                boolean permValue = island.hasPerm(playerName, validPerm);
                msg += tr("\u00a77 - \u00a76{0}\u00a77 : {1}", validPerm, permValue ? tr("\u00a7aON") : tr("\u00a7cOFF")) + "\n";
            }
            player.sendMessage(msg.trim().split("\n"));
            return true;
        }
        if (playerName == null || perm == null || perm.isEmpty() || playerName.isEmpty()) {
            return false;
        }
        if (!isValidPermission(perm)) {
            player.sendMessage(tr("\u00a7cInvalid permission {0}. Must be one of {1}", perm, getValidPermissions()));
            return true;
        }
        if (island.togglePerm(playerName, perm)) {
            boolean permValue = island.hasPerm(playerName, perm);
            player.sendMessage(tr("\u00a7eToggled permission \u00a79{0}\u00a7e for \u00a79{1}\u00a7e to {2}", perm, playerName, permValue ? tr("\u00a7aON") : tr("\u00a7cOFF")));
        } else {
            player.sendMessage(tr("\u00a7eUnable to toggle permission \u00a79{0}\u00a7e for \u00a79{1}", perm, playerName));
        }
        return true;
    }

    private boolean isValidPermission(String perm) {
        return getValidPermissions().contains(perm);
    }

    private List<String> getValidPermissions() {
        List<String> list = new ArrayList<>();
        for (PartyPermissionMenuItem item : plugin.getMenu().getPermissionMenuItems()) {
            list.add(item.getPerm());
        }
        return list;
    }
}

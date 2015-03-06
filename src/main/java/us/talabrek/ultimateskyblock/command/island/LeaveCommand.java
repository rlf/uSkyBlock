package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

@SuppressWarnings("deprecation")
public class LeaveCommand extends RequireIslandCommand {
    public LeaveCommand(uSkyBlock plugin) {
        super(plugin, "leave", "usb.party.join", "leave your party");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (player.getWorld().getName().equalsIgnoreCase(uSkyBlock.getSkyBlockWorld().getName())) {
            if (!island.isParty()) {
                player.sendMessage(tr("\u00a74You can't leave your island if you are the only person. Try using /island restart if you want a new one!"));
                return true;
            }
            if (island.isLeader(player)) {
                player.sendMessage(tr("\u00a7eYou own this island, use /island remove <player> instead."));
                return true;
            }
            player.getInventory().clear();
            player.getEquipment().clear();
            plugin.spawnTeleport(player);
            island.removeMember(pi);
            player.sendMessage(tr("\u00a7eYou have left the island and returned to the player spawn."));
            if (Bukkit.getPlayer(island.getLeader()) != null) {
                Bukkit.getPlayer(island.getLeader()).sendMessage(tr("\u00a74{0} has left your island!", player.getName()));
            }
            return true;
        } else {
            player.sendMessage(tr("\u00a74You must be in the skyblock world to leave your party!"));
            return true;
        }
    }
}

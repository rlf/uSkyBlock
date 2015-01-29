package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.island.RequirePlayerCommand;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

/**
 * Allows transfer of leadership to another player.
 */
public class MakeLeaderCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public MakeLeaderCommand(uSkyBlock plugin) {
        super("makeleader|transfer", "usb.admin.island", "island oplayer", "transfer leadership to another player");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        if (args.length == 2) {
            String island = args[0];
            String playerName = args[1];
            PlayerInfo islandPlayer = plugin.getPlayerInfo(island);
            PlayerInfo playerInfo = plugin.getPlayerInfo(playerName);
            if (islandPlayer == null || !islandPlayer.getHasIsland()) {
                sender.sendMessage("\u00a74Player " + island + " has no island to transfer!");
                return false;
            }
            IslandInfo islandInfo = plugin.getIslandInfo(islandPlayer);
            if (islandInfo == null) {
                sender.sendMessage("\u00a74Player " + island + " has no island to transfer!");
                return false;
            }
            if (playerInfo != null && playerInfo.getHasIsland()) {
                sender.sendMessage("\u00a74Player " + playerName + " alread has an island.\u00a7eUse \u00a7d/usb remove <name>\u00a7e to remove him first.");
                return false;
            }
            islandInfo.setupPartyLeader(playerInfo.getPlayerName()); // Promote member
            islandInfo.removeMember(playerInfo); // Remove leader
            WorldGuardHandler.updateRegion(sender, islandInfo);
            islandInfo.sendMessageToIslandGroup("\u00a7bLeadership transferred by " + sender.getName() + "\u00a7b to " + playerName);
            return true;
        }
        return false;
    }
}

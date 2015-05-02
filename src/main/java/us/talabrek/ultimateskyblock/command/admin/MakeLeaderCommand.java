package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.handler.WorldGuardHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

import static us.talabrek.ultimateskyblock.util.I18nUtil.tr;

/**
 * Allows transfer of leadership to another player.
 */
public class MakeLeaderCommand extends AbstractUSBCommand {
    private final uSkyBlock plugin;

    public MakeLeaderCommand(uSkyBlock plugin) {
        super("makeleader|transfer", "usb.admin.makeleader", "leader oplayer", tr("transfer leadership to another player"));
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
                sender.sendMessage(tr("\u00a74Player {0} has no island to transfer!", island));
                return false;
            }
            IslandInfo islandInfo = plugin.getIslandInfo(islandPlayer);
            if (islandInfo == null) {
                sender.sendMessage(tr("\u00a74Player {0} has no island to transfer!", island));
                return false;
            }
            if (playerInfo != null && playerInfo.getHasIsland()) {
                sender.sendMessage(tr("\u00a7ePlayer \u00a7d{0}\u00a7e already has an island.\u00a7eUse \u00a7d/usb island remove <name>\u00a7e to remove him first.", playerName));
                return false;
            }
            playerInfo.setJoinParty(islandInfo.getIslandLocation());
            Location homeLocation = islandPlayer.getHomeLocation();
            islandInfo.removeMember(islandPlayer); // Remove leader
            islandInfo.setupPartyLeader(playerInfo.getPlayerName()); // Promote member
            playerInfo.setHomeLocation(homeLocation);
            islandPlayer.save();
            playerInfo.save();
            WorldGuardHandler.updateRegion(sender, islandInfo);
            islandInfo.sendMessageToIslandGroup(tr("\u00a7bLeadership transferred by {0}\u00a7b to {1}", sender.getName(), playerName));
            return true;
        }
        return false;
    }
}

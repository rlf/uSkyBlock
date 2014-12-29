package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.VaultHandler;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.Map;

public class LockUnlockCommand extends RequireIslandCommand {
    public LockUnlockCommand(uSkyBlock plugin) {
        super(plugin, "lock|unlock", "usb.lock", "lock your island to non-party members.");
    }

    @Override
    protected boolean doExecute(String alias, Player player, PlayerInfo pi, IslandInfo island, Map<String, Object> data, String... args) {
        if (alias.equalsIgnoreCase("lock") && pi.getHasIsland()) {
            if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                if (island.hasPerm(player, "canToggleLock")) {
                    island.lock(player);
                } else {
                    player.sendMessage("\u00a74You do not have permission to lock your island!");
                }
            } else {
                player.sendMessage("\u00a74You don't have access to this command!");
            }
            return true;
        }
        if (alias.equalsIgnoreCase("unlock") && pi.getHasIsland()) {
            if (Settings.island_allowIslandLock && VaultHandler.checkPerk(player.getName(), "usb.lock", player.getWorld())) {
                if (island.hasPerm(player, "canToggleLock")) {
                    island.unlock(player);
                } else {
                    player.sendMessage("\u00a74You do not have permission to unlock your island!");
                }
            } else {
                player.sendMessage("\u00a74You don't have access to this command!");
            }
            return true;
        }
        return false;
    }
}
